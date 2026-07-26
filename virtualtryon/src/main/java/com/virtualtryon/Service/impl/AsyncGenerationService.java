package com.virtualtryon.Service.impl;

import com.virtualtryon.Dto.PythonGenerateRequest;
import com.virtualtryon.Dto.PythonGenerateResponse;
import com.virtualtryon.Entity.GeneratedImage;
import com.virtualtryon.Repository.GeneratedImageRepository;
import com.virtualtryon.Service.PythonService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class AsyncGenerationService {

    private final GeneratedImageRepository generatedImageRepository;
    private final PythonService pythonService;

    @Async("generationExecutor")
    public void processGeneration(
            Long generatedImageId,
            PythonGenerateRequest pythonRequest
    ) {
        GeneratedImage generatedImage = generatedImageRepository
                .findById(generatedImageId)
                .orElse(null);

        if (generatedImage == null) {
            return;
        }

        try {
            PythonGenerateResponse response = pythonService.generate(pythonRequest);

            if (response == null
                    || response.outputImage() == null
                    || response.outputImage().isBlank()) {
                throw new RuntimeException("Python AI did not return generated image");
            }

            generatedImage.setOutputImage(
                    toAccessibleImagePath(response.outputImage())
            );
            generatedImage.setStatus(
                    response.status() != null ? response.status() : "SUCCESS"
            );
        } catch (Exception exception) {
            generatedImage.setStatus("FAILED");
            System.err.println("WEARFASHION ASYNC GENERATION FAILED");
            System.err.println(exception.getMessage());
        }

        generatedImageRepository.save(generatedImage);
    }

    private String toAccessibleImagePath(String outputImage) {
        if (outputImage == null || outputImage.isBlank()) {
            return "";
        }

        String normalized = outputImage.trim().replace('\\', '/');

        if (normalized.startsWith("http://")
                || normalized.startsWith("https://")
                || normalized.startsWith("data:")) {
            return normalized;
        }

        Path imagePath = Path.of(normalized).normalize();
        String fileName = imagePath.getFileName() != null
                ? imagePath.getFileName().toString()
                : normalized;

        if (normalized.contains("/uploads/output/")
                || normalized.contains("uploads/output/")) {
            return "/uploads/output/" + fileName;
        }

        if (normalized.startsWith("/")) {
            return normalized;
        }

        if (normalized.contains("uploads/")) {
            return "/" + normalized;
        }

        return "/" + normalized;
    }
}
