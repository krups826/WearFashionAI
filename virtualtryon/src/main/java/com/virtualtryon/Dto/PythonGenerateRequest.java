package com.virtualtryon.Dto;

public record PythonGenerateRequest(
        String personPath,
        String materialPath,
        String clothType,
        String clothName,
        String color
) {
}