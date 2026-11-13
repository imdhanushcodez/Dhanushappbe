package com.developersfab.Dhanushappbe.notes;

import com.developersfab.Dhanushappbe.auth.User;
import com.developersfab.Dhanushappbe.auth.UserRepository;
import com.developersfab.Dhanushappbe.common.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public PageResponse<NoteSummary> listNotes(
            Long userId, String q, String sortStr,
            String addedFromStr, String addedToStr, int page, int size) {

        Sort sort = resolveSort(sortStr);
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, Math.min(size, 100)), sort);

        Instant addedFrom = parseDateStart(addedFromStr);
        Instant addedTo = parseDateEnd(addedToStr);
        String cleanQ = (q != null && !q.isBlank()) ? q.trim() : null;

        Specification<Note> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));

            if (cleanQ != null) {
                String pattern = "%" + cleanQ.toLowerCase() + "%";
                Predicate searchPred = cb.or(
                        cb.like(cb.lower(root.get("topic")), pattern),
                        cb.like(cb.lower(root.get("content")), pattern)
                );
                predicates.add(searchPred);
            }

            if (addedFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), addedFrom));
            }

            if (addedTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), addedTo));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Note> pageResult = noteRepository.findAll(spec, pageable);
        Page<NoteSummary> summaryPage = pageResult.map(this::toSummary);

        return PageResponse.from(summaryPage);
    }

    @Transactional
    public NoteResponse createNote(Long userId, NoteRequest request) {
        User user = getUser(userId);

        Note note = Note.builder()
                .user(user)
                .topic(request.topic().trim())
                .content(request.notes())
                .build();

        Note saved = noteRepository.save(note);
        return toResponse(saved);
    }

    public NoteResponse getNote(Long userId, Long id) {
        Note note = noteRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "Note not found."));
        return toResponse(note);
    }

    @Transactional
    public NoteResponse updateNote(Long userId, Long id, NoteRequest request) {
        Note note = noteRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "Note not found."));

        note.setTopic(request.topic().trim());
        note.setContent(request.notes());

        Note updated = noteRepository.save(note);
        return toResponse(updated);
    }

    @Transactional
    public void deleteNote(Long userId, Long id) {
        Note note = noteRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "Note not found."));
        noteRepository.delete(note);
    }

    public byte[] exportCsv(Long userId) {
        List<Note> notes = noteRepository.findByUserId(userId, Sort.by(Sort.Direction.ASC, "createdAt"));

        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.builder().setHeader("topic", "notes").build())) {
            for (Note n : notes) {
                printer.printRecord(
                        CsvSanitizer.sanitizeForCsv(n.getTopic()),
                        CsvSanitizer.sanitizeForCsv(n.getContent())
                );
            }
            printer.flush();
        } catch (Exception e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR, "Failed to export notes to CSV.");
        }

        return sw.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] getSampleTemplate() {
        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.builder().setHeader("topic", "notes").build())) {
            printer.printRecord("Example topic", "Example note text");
            printer.flush();
        } catch (Exception e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR, "Failed to generate sample template.");
        }
        return sw.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional
    public Object importCsv(Long userId, MultipartFile file, boolean dryRun) {
        if (file == null || file.isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, ErrorCode.IMPORT_INVALID_FILE, "File is missing or empty.");
        }

        if (file.getSize() > 1_048_576) {
            throw new AppException(HttpStatus.PAYLOAD_TOO_LARGE, ErrorCode.FILE_TOO_LARGE, "The file is larger than the allowed size.");
        }

        User user = getUser(userId);
        List<CSVRecord> records;

        try {
            Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
            CSVParser parser = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setIgnoreHeaderCase(true)
                    .setTrim(true)
                    .build()
                    .parse(reader);

            Map<String, Integer> headerMap = parser.getHeaderMap();
            if (headerMap == null || !headerMap.containsKey("topic") || !headerMap.containsKey("notes")) {
                throw new AppException(HttpStatus.BAD_REQUEST, ErrorCode.IMPORT_INVALID_FILE, "CSV must have columns: topic,notes.");
            }

            records = parser.getRecords();
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(HttpStatus.BAD_REQUEST, ErrorCode.IMPORT_INVALID_FILE, "Malformed CSV file.");
        }

        if (records.size() > 1000) {
            throw new AppException(HttpStatus.BAD_REQUEST, ErrorCode.IMPORT_TOO_MANY_ROWS, "The file has more than 1000 rows.");
        }

        List<Note> validNotes = new ArrayList<>();
        List<ImportRowError> errors = new ArrayList<>();
        Set<String> seenInBatch = new HashSet<>();
        int duplicateRows = 0;
        int invalidRows = 0;

        int rowNum = 1; // Header is row 1
        for (CSVRecord record : records) {
            rowNum++;
            String topic = record.isMapped("topic") ? CsvSanitizer.cleanFromImport(record.get("topic")) : "";
            String notesContent = record.isMapped("notes") ? CsvSanitizer.cleanFromImport(record.get("notes")) : "";

            if (topic.isBlank()) {
                invalidRows++;
                errors.add(new ImportRowError(rowNum, "Topic is missing"));
                continue;
            }
            if (topic.length() > 200) {
                invalidRows++;
                errors.add(new ImportRowError(rowNum, "Topic must not exceed 200 characters"));
                continue;
            }
            if (notesContent.isBlank()) {
                invalidRows++;
                errors.add(new ImportRowError(rowNum, "Notes is missing"));
                continue;
            }
            if (notesContent.length() > 10000) {
                invalidRows++;
                errors.add(new ImportRowError(rowNum, "Notes must not exceed 10000 characters"));
                continue;
            }

            String pairKey = topic.toLowerCase() + ":::" + notesContent;
            if (seenInBatch.contains(pairKey)) {
                duplicateRows++;
                continue;
            }

            if (noteRepository.existsByUserIdAndTopicIgnoreCaseAndContent(userId, topic, notesContent)) {
                duplicateRows++;
                continue;
            }

            seenInBatch.add(pairKey);
            validNotes.add(Note.builder()
                    .user(user)
                    .topic(topic)
                    .content(notesContent)
                    .build());
        }

        if (dryRun) {
            return new ImportPreviewResponse(
                    records.size(),
                    validNotes.size(),
                    duplicateRows,
                    invalidRows,
                    errors
            );
        }

        if (!validNotes.isEmpty()) {
            noteRepository.saveAll(validNotes);
        }

        return new ImportResultResponse(validNotes.size(), duplicateRows + invalidRows);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "User not found."));
    }

    private NoteSummary toSummary(Note note) {
        String content = note.getContent();
        String preview = "";
        if (content != null) {
            preview = content.length() > 120 ? content.substring(0, 120) : content;
        }
        return new NoteSummary(
                note.getId(),
                note.getTopic(),
                preview,
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }

    private NoteResponse toResponse(Note note) {
        return new NoteResponse(
                note.getId(),
                note.getTopic(),
                note.getContent(),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }

    private Sort resolveSort(String sortStr) {
        if ("oldest".equalsIgnoreCase(sortStr)) {
            return Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id"));
        } else if ("topicAsc".equalsIgnoreCase(sortStr)) {
            return Sort.by(Sort.Order.asc("topic").ignoreCase(), Sort.Order.asc("id"));
        } else if ("topicDesc".equalsIgnoreCase(sortStr)) {
            return Sort.by(Sort.Order.desc("topic").ignoreCase(), Sort.Order.asc("id"));
        } else if ("updated".equalsIgnoreCase(sortStr)) {
            return Sort.by(Sort.Order.desc("updatedAt"), Sort.Order.desc("id"));
        }
        return Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
    }

    private Instant parseDateStart(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            LocalDate d = LocalDate.parse(dateStr.trim());
            return d.atStartOfDay().toInstant(ZoneOffset.UTC);
        } catch (Exception e) {
            return null;
        }
    }

    private Instant parseDateEnd(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            LocalDate d = LocalDate.parse(dateStr.trim());
            return d.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        } catch (Exception e) {
            return null;
        }
    }
}

