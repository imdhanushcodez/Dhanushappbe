package com.developersfab.Dhanushappbe.notes;

import com.developersfab.Dhanushappbe.common.PageResponse;
import com.developersfab.Dhanushappbe.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @GetMapping
    public ResponseEntity<PageResponse<NoteSummary>> listNotes(
            @RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @RequestParam(required = false) String addedFrom,
            @RequestParam(required = false) String addedTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        PageResponse<NoteSummary> response = noteService.listNotes(
                userId, q, sort, addedFrom, addedTo, page, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<NoteResponse> createNote(@Valid @RequestBody NoteRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        NoteResponse created = noteService.createNote(userId, request);
        URI location = URI.create("/api/notes/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteResponse> getNote(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        NoteResponse note = noteService.getNote(userId, id);
        return ResponseEntity.ok(note);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoteResponse> updateNote(
            @PathVariable Long id,
            @Valid @RequestBody NoteRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        NoteResponse updated = noteService.updateNote(userId, id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        noteService.deleteNote(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportNotes() {
        Long userId = SecurityUtils.getCurrentUserId();
        byte[] csvBytes = noteService.exportCsv(userId);

        String filename = "notes-" + LocalDate.now() + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }

    @GetMapping("/import/template")
    public ResponseEntity<byte[]> getImportTemplate() {
        byte[] csvBytes = noteService.getSampleTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"notes-import-template.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> importNotes(
            @RequestParam("file") MultipartFile file,
            @RequestParam(name = "dryRun", defaultValue = "false") boolean dryRun) {
        Long userId = SecurityUtils.getCurrentUserId();
        Object result = noteService.importCsv(userId, file, dryRun);
        return ResponseEntity.ok(result);
    }
}

