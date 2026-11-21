package com.example.demo.controller;

import com.example.demo.dto.GeneratedReportResponse;
import com.example.demo.dto.ReportGenerationRequest;
import com.example.demo.report.ReportGenerationService;
import com.example.demo.report.ReportJob;
import com.example.demo.security.UserAccountPrincipal;
import jakarta.validation.Valid;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportGenerationService service;

    public ReportController(ReportGenerationService service) {
        this.service = service;
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generate(@Valid @RequestBody ReportGenerationRequest request,
                                                         Authentication authentication) {
        GeneratedReportResponse report = service.generate(request, userId(authentication));
        return ResponseEntity.accepted().body(Map.of(
                "reportId", report.id(), "status", report.status(),
                "message", "Report generation has been scheduled."));
    }

    @GetMapping
    public List<GeneratedReportResponse> list(Authentication authentication) {
        return service.list(userId(authentication));
    }

    @GetMapping("/{id}")
    public GeneratedReportResponse get(@PathVariable Long id, Authentication authentication) {
        return service.get(id, userId(authentication));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id, Authentication authentication) {
        ReportJob job = service.getOwnedJob(id, userId(authentication));
        if (!"COMPLETED".equals(job.status()) || job.filePath() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Report is not ready for download");
        }
        Path file = Path.of(job.filePath()).toAbsolutePath().normalize();
        if (!file.startsWith(service.storageDirectory()) || !Files.isRegularFile(file)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Generated report file not found");
        }
        String name = job.fileName() == null ? "report-" + id + ".pdf" : job.fileName();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(name).build().toString())
                .contentLength(file.toFile().length())
                .body(new FileSystemResource(file));
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<Void> retry(@PathVariable Long id, Authentication authentication) {
        service.retry(id, userId(authentication));
        return ResponseEntity.accepted().build();
    }

    private Integer userId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserAccountPrincipal principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user is required");
        }
        return principal.getId();
    }
}
