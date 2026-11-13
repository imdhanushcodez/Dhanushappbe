package com.developersfab.Dhanushappbe.vault;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VaultService {

    private final VaultRepository vaultRepository;
    private final UserRepository userRepository;
    private final VaultEncryptionService encryptionService;
    private final PasswordEncoder passwordEncoder;

    public PageResponse<VaultEntrySummaryDto> listVaultEntries(
            Long userId, String q, String sortStr, List<String> sources,
            String addedFromStr, String addedToStr, int page, int size) {

        Sort sort = resolveSort(sortStr);
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, Math.min(size, 100)), sort);

        Instant addedFrom = parseDateStart(addedFromStr);
        Instant addedTo = parseDateEnd(addedToStr);
        String cleanQ = (q != null && !q.isBlank()) ? q.trim() : null;

        List<String> cleanSources = null;
        if (sources != null && !sources.isEmpty()) {
            cleanSources = sources.stream().filter(s -> s != null && !s.isBlank()).map(String::trim).toList();
        }

        final List<String> finalSources = cleanSources;
        Specification<VaultEntry> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));

            if (cleanQ != null) {
                String pattern = "%" + cleanQ.toLowerCase() + "%";
                Predicate searchPred = cb.or(
                        cb.like(cb.lower(root.get("source")), pattern),
                        cb.like(cb.lower(root.get("loginId")), pattern)
                );
                predicates.add(searchPred);
            }

            if (finalSources != null && !finalSources.isEmpty()) {
                predicates.add(root.get("source").in(finalSources));
            }

            if (addedFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), addedFrom));
            }

            if (addedTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), addedTo));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<VaultEntry> pageResult = vaultRepository.findAll(spec, pageable);

        Page<VaultEntrySummaryDto> dtoPage = pageResult.map(this::toSummaryDto);
        return PageResponse.from(dtoPage);
    }

    public List<String> getDistinctSources(Long userId) {
        return vaultRepository.findDistinctSourcesByUserId(userId);
    }

    @Transactional
    public VaultEntrySummaryDto createEntry(Long userId, VaultEntryRequest request) {
        User user = getUser(userId);
        String source = request.source().trim();
        String loginId = request.loginId().trim();

        if (vaultRepository.existsByUserIdAndSourceIgnoreCaseAndLoginIdIgnoreCase(userId, source, loginId)) {
            throw new AppException(
                    HttpStatus.CONFLICT,
                    ErrorCode.VAULT_ENTRY_EXISTS,
                    "You already saved a password for this source and id.",
                    List.of(new FieldErrorItem("loginId", "ALREADY_EXISTS", "This source and id are already saved."))
            );
        }

        String encryptedPassword = encryptionService.encrypt(request.password());

        VaultEntry entry = VaultEntry.builder()
                .user(user)
                .source(source)
                .loginId(loginId)
                .passwordEncrypted(encryptedPassword)
                .build();

        VaultEntry saved = vaultRepository.save(entry);
        return toSummaryDto(saved);
    }

    public VaultEntryDetailDto getEntryDetail(Long userId, Long id) {
        VaultEntry entry = vaultRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "Vault entry not found."));

        String decryptedPassword = encryptionService.decrypt(entry.getPasswordEncrypted());

        return VaultEntryDetailDto.builder()
                .id(entry.getId())
                .source(entry.getSource())
                .loginId(entry.getLoginId())
                .password(decryptedPassword)
                .createdAt(entry.getCreatedAt())
                .updatedAt(entry.getUpdatedAt())
                .build();
    }

    public VaultPasswordResponse getPassword(Long userId, Long id) {
        VaultEntry entry = vaultRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "Vault entry not found."));

        String decrypted = encryptionService.decrypt(entry.getPasswordEncrypted());
        return new VaultPasswordResponse(decrypted);
    }

    @Transactional
    public VaultEntrySummaryDto updateEntry(Long userId, Long id, VaultEntryRequest request) {
        VaultEntry entry = vaultRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "Vault entry not found."));

        String source = request.source().trim();
        String loginId = request.loginId().trim();

        if (vaultRepository.existsByUserIdAndSourceIgnoreCaseAndLoginIdIgnoreCaseAndIdNot(userId, source, loginId, id)) {
            throw new AppException(
                    HttpStatus.CONFLICT,
                    ErrorCode.VAULT_ENTRY_EXISTS,
                    "You already saved a password for this source and id.",
                    List.of(new FieldErrorItem("loginId", "ALREADY_EXISTS", "This source and id are already saved."))
            );
        }

        entry.setSource(source);
        entry.setLoginId(loginId);
        if (request.password() != null && !request.password().isBlank()) {
            entry.setPasswordEncrypted(encryptionService.encrypt(request.password()));
        }

        VaultEntry updated = vaultRepository.save(entry);
        return toSummaryDto(updated);
    }

    @Transactional
    public void deleteEntry(Long userId, Long id) {
        VaultEntry entry = vaultRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "Vault entry not found."));
        vaultRepository.delete(entry);
    }

    public byte[] exportCsv(Long userId, ExportPasswordsRequest request) {
        User user = getUser(userId);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new AppException(HttpStatus.FORBIDDEN, ErrorCode.INVALID_PASSWORD, "Current password is incorrect.");
        }

        List<VaultEntry> entries = vaultRepository.findByUserId(userId, Sort.by(Sort.Direction.ASC, "createdAt"));

        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.builder().setHeader("source", "id", "password").build())) {
            for (VaultEntry e : entries) {
                String plainPassword = encryptionService.decrypt(e.getPasswordEncrypted());
                printer.printRecord(
                        CsvSanitizer.sanitizeForCsv(e.getSource()),
                        CsvSanitizer.sanitizeForCsv(e.getLoginId()),
                        CsvSanitizer.sanitizeForCsv(plainPassword)
                );
            }
            printer.flush();
        } catch (IOException e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR, "Failed to generate CSV export.");
        }

        return sw.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] getSampleTemplate() {
        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.builder().setHeader("source", "id", "password").build())) {
            printer.printRecord("Google", "example@gmail.com", "SamplePass123");
            printer.flush();
        } catch (IOException e) {
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
            if (headerMap == null || !headerMap.containsKey("source") || !headerMap.containsKey("id") || !headerMap.containsKey("password")) {
                throw new AppException(HttpStatus.BAD_REQUEST, ErrorCode.IMPORT_INVALID_FILE, "CSV must have columns: source,id,password.");
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

        List<VaultEntry> validEntries = new ArrayList<>();
        List<ImportRowError> errors = new ArrayList<>();
        Set<String> seenInBatch = new HashSet<>();
        int duplicateRows = 0;
        int invalidRows = 0;

        int rowNum = 1; // Header is row 1
        for (CSVRecord record : records) {
            rowNum++;
            String source = record.isMapped("source") ? CsvSanitizer.cleanFromImport(record.get("source")) : "";
            String loginId = record.isMapped("id") ? CsvSanitizer.cleanFromImport(record.get("id")) : "";
            String password = record.isMapped("password") ? CsvSanitizer.cleanFromImport(record.get("password")) : "";

            if (source.isBlank()) {
                invalidRows++;
                errors.add(new ImportRowError(rowNum, "Source is missing"));
                continue;
            }
            if (source.length() > 150) {
                invalidRows++;
                errors.add(new ImportRowError(rowNum, "Source must not exceed 150 characters"));
                continue;
            }
            if (loginId.isBlank()) {
                invalidRows++;
                errors.add(new ImportRowError(rowNum, "Id is missing"));
                continue;
            }
            if (loginId.length() > 255) {
                invalidRows++;
                errors.add(new ImportRowError(rowNum, "Id must not exceed 255 characters"));
                continue;
            }
            if (password.isBlank()) {
                invalidRows++;
                errors.add(new ImportRowError(rowNum, "Password is missing"));
                continue;
            }
            if (password.length() > 256) {
                invalidRows++;
                errors.add(new ImportRowError(rowNum, "Password must not exceed 256 characters"));
                continue;
            }

            String pairKey = source.toLowerCase() + ":::" + loginId.toLowerCase();
            if (seenInBatch.contains(pairKey)) {
                duplicateRows++;
                continue;
            }

            if (vaultRepository.existsByUserIdAndSourceIgnoreCaseAndLoginIdIgnoreCase(userId, source, loginId)) {
                duplicateRows++;
                continue;
            }

            seenInBatch.add(pairKey);
            validEntries.add(VaultEntry.builder()
                    .user(user)
                    .source(source)
                    .loginId(loginId)
                    .passwordEncrypted(encryptionService.encrypt(password))
                    .build());
        }

        if (dryRun) {
            return new ImportPreviewResponse(
                    records.size(),
                    validEntries.size(),
                    duplicateRows,
                    invalidRows,
                    errors
            );
        }

        if (!validEntries.isEmpty()) {
            vaultRepository.saveAll(validEntries);
        }

        return new ImportResultResponse(validEntries.size(), duplicateRows + invalidRows);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "User not found."));
    }

    private VaultEntrySummaryDto toSummaryDto(VaultEntry e) {
        return VaultEntrySummaryDto.builder()
                .id(e.getId())
                .source(e.getSource())
                .loginId(e.getLoginId())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private Sort resolveSort(String sortStr) {
        if ("oldest".equalsIgnoreCase(sortStr)) {
            return Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id"));
        } else if ("sourceAsc".equalsIgnoreCase(sortStr)) {
            return Sort.by(Sort.Order.asc("source").ignoreCase(), Sort.Order.asc("id"));
        } else if ("sourceDesc".equalsIgnoreCase(sortStr)) {
            return Sort.by(Sort.Order.desc("source").ignoreCase(), Sort.Order.asc("id"));
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
