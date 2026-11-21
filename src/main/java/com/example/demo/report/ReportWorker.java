package com.example.demo.report;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class ReportWorker {
    private final ReportJobRepository jobs;
    private final ReportDataService dataService;
    private final PdfReportRenderer renderer;
    private final Path storageDirectory;

    public ReportWorker(ReportJobRepository jobs, ReportDataService dataService, PdfReportRenderer renderer,
                        @Value("${reports.storage-dir:./storage/reports}") String storageDirectory) {
        this.jobs = jobs;
        this.dataService = dataService;
        this.renderer = renderer;
        this.storageDirectory = Path.of(storageDirectory).toAbsolutePath().normalize();
    }

    @Scheduled(fixedDelayString = "${reports.worker.poll-interval-ms:3000}")
    public void processNext() {
        jobs.claimNext().ifPresent(this::process);
    }

    private void process(ReportJob job) {
        try {
            ReportData data = dataService.load(job);
            Path userDirectory = storageDirectory.resolve(String.valueOf(job.requestedBy())).normalize();
            if (!userDirectory.startsWith(storageDirectory)) {
                throw new IllegalStateException("Invalid report storage path");
            }
            Files.createDirectories(userDirectory);
            String fileName = "report-" + job.id() + ".pdf";
            Path output = userDirectory.resolve(fileName);
            Files.write(output, renderer.render(job, data));
            jobs.markCompleted(job.id(), output.toString(), fileName);
        } catch (Exception ex) {
            String message = ex.getMessage() == null ? "Report generation failed" : ex.getMessage();
            jobs.markFailed(job.id(), message.length() > 1000 ? message.substring(0, 1000) : message);
        }
    }
}
