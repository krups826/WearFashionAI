package com.virtualtryon.Controller;

import com.virtualtryon.Dto.AIReportResponse;
import com.virtualtryon.Service.AIReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin
public class AIReportController {

    private final AIReportService aiReportService;

    @PostMapping("/generate")
    public ResponseEntity<AIReportResponse> generateReport(@RequestParam Long generatedImageId) {
        return ResponseEntity.ok(aiReportService.generateReport(generatedImageId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AIReportResponse> getReportById(@PathVariable Long id) {
        return ResponseEntity.ok(aiReportService.getReportById(id));
    }

    @GetMapping("/image/{imageId}")
    public ResponseEntity<AIReportResponse> getReportByGeneratedImageId(@PathVariable Long imageId) {
        return ResponseEntity.ok(aiReportService.getReportByGeneratedImageId(imageId));
    }

    @GetMapping
    public ResponseEntity<List<AIReportResponse>> getAllReports() {
        return ResponseEntity.ok(aiReportService.getAllReports());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteReport(@PathVariable Long id) {
        aiReportService.deleteReport(id);
        return ResponseEntity.ok("AI Report deleted successfully.");
    }
}
