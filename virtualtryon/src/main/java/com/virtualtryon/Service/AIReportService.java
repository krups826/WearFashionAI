package com.virtualtryon.Service;

import com.virtualtryon.Dto.AIReportResponse;
import java.util.List;

public interface AIReportService {
    AIReportResponse generateReport(Long generatedImageId);
    AIReportResponse getReportById(Long id);
    AIReportResponse getReportByGeneratedImageId(Long generatedImageId);
    List<AIReportResponse> getAllReports();
    void deleteReport(Long id);
}
