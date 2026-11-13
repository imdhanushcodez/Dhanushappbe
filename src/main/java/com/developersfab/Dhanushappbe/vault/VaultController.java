package com.developersfab.Dhanushappbe.vault;

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

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/vault")
@RequiredArgsConstructor
public class VaultController {

    private final VaultService vaultService;

    @GetMapping
    public ResponseEntity<PageResponse<VaultEntrySummaryDto>> listVault(
            @RequestParam(required = false) String q,
            @RequestParam(required = false, defaultValue = "newest") String sort,
            @RequestParam(name = "source", required = false) List<String> sources,
            @RequestParam(required = false) String addedFrom,
            @RequestParam(required = false) String addedTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        PageResponse<VaultEntrySummaryDto> response = vaultService.listVaultEntries(
                userId, q, sort, sources, addedFrom, addedTo, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sources")
    public ResponseEntity<List<String>> getSources() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<String> sources = vaultService.getDistinctSources(userId);
        return ResponseEntity.ok(sources);
    }

    @PostMapping
    public ResponseEntity<VaultEntrySummaryDto> createEntry(@Valid @RequestBody VaultEntryRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        VaultEntrySummaryDto created = vaultService.createEntry(userId, request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VaultEntryDetailDto> getEntry(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        VaultEntryDetailDto detail = vaultService.getEntryDetail(userId, id);
        return ResponseEntity.ok(detail);
    }

    @GetMapping("/{id}/password")
    public ResponseEntity<VaultPasswordResponse> getPassword(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        VaultPasswordResponse password = vaultService.getPassword(userId, id);
        return ResponseEntity.ok(password);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VaultEntrySummaryDto> updateEntry(
            @PathVariable Long id,
            @Valid @RequestBody VaultEntryRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        VaultEntrySummaryDto updated = vaultService.updateEntry(userId, id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntry(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        vaultService.deleteEntry(userId, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/export")
    public ResponseEntity<byte[]> exportVault(@Valid @RequestBody ExportPasswordsRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        byte[] csvBytes = vaultService.exportCsv(userId, request);

        String filename = "passwords-" + LocalDate.now() + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }

    @GetMapping("/import/template")
    public ResponseEntity<byte[]> getImportTemplate() {
        byte[] csvBytes = vaultService.getSampleTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"passwords-template.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> importVault(
            @RequestParam("file") MultipartFile file,
            @RequestParam(name = "dryRun", defaultValue = "false") boolean dryRun) {
        Long userId = SecurityUtils.getCurrentUserId();
        Object result = vaultService.importCsv(userId, file, dryRun);
        return ResponseEntity.ok(result);
    }
}
