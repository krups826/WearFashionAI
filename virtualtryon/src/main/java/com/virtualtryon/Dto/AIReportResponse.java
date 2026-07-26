package com.virtualtryon.Dto;

public record AIReportResponse(
    Long id,
    Integer rating,
    String recommendation,
    String occasion,
    String colorSuggestion,
    Long generatedImageId,
    String outputImageUrl
) {}
