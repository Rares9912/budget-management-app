package com.rares.budget_management_app.report;

import com.rares.budget_management_app.user.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Month;

@Tag(name = "Report")
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/monthly")
    public ResponseEntity<byte[]> getMonthlyReport(
            @AuthenticationPrincipal User currentUser,
            @RequestParam int month,
            @RequestParam int year) {

        byte[] pdf = reportService.generateMonthlyReport(currentUser, month, year);

        String filename = String.format("lunar-report-%s-%d.pdf",
                Month.of(month).name().toLowerCase(), year);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdf);
    }

    @GetMapping("/annual")
    public ResponseEntity<byte[]> getAnnualReport(
            @AuthenticationPrincipal User currentUser,
            @RequestParam int year) {

        byte[] pdf = reportService.generateAnnualReport(currentUser, year);

        String filename = String.format("raport-anual-%d.pdf", year);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(pdf);
    }
}