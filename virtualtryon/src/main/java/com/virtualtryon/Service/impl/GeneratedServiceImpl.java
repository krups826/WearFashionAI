package com.virtualtryon.Service.impl;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.virtualtryon.Dto.GenerateRequest;
import com.virtualtryon.Dto.GenerateResponse;
import com.virtualtryon.Dto.PythonGenerateRequest;

import com.virtualtryon.Entity.Clothing;
import com.virtualtryon.Entity.GeneratedImage;
import com.virtualtryon.Entity.Person;

import com.virtualtryon.Repository.ClothingRepository;
import com.virtualtryon.Repository.GeneratedImageRepository;
import com.virtualtryon.Repository.PersonImageRepository;

import com.virtualtryon.Service.GeneratedImageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GeneratedServiceImpl
        implements GeneratedImageService {

    private final GeneratedImageRepository generatedImageRepository;
    private final ClothingRepository clothingRepository;
    private final PersonImageRepository personImageRepository;
    private final AsyncGenerationService asyncGenerationService;

    @Override
    public GenerateResponse generate(
            GenerateRequest request
    ) {
        Clothing clothing = clothingRepository
                .findById(request.clothingId())
                .orElseThrow(
                        () -> new RuntimeException(
                                "Fabric / clothing material not found"
                        )
                );

        Person person = personImageRepository
                .findById(request.personId())
                .orElseThrow(
                        () -> new RuntimeException(
                                "Person image not found"
                        )
                );

        GeneratedImage generatedImage = new GeneratedImage();
        generatedImage.setClothing(clothing);
        generatedImage.setPerson(person);
        generatedImage.setGeneratedAt(LocalDateTime.now());
        generatedImage.setStatus("PROCESSING");

        generatedImage = generatedImageRepository.save(generatedImage);

        PythonGenerateRequest pythonRequest = new PythonGenerateRequest(
                person.getImagePath(),
                clothing.getImagePath(),
                clothing.getClothType(),
                clothing.getClothName(),
                clothing.getColor()
        );

        asyncGenerationService.processGeneration(
                generatedImage.getId(),
                pythonRequest
        );

        return new GenerateResponse(
                generatedImage.getId(),
                null,
                "PROCESSING",
                "Virtual try-on started. Poll /api/generate/"
                        + generatedImage.getId()
                        + " for status."
        );
    }

    @Override
    public List<GenerateResponse> getAllGeneratedImages() {
        return generatedImageRepository
                .findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public GenerateResponse getGeneratedImageById(Long id) {
        GeneratedImage image = generatedImageRepository
                .findById(id)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Generated image not found"
                        )
                );

        return toResponse(image);
    }

    private GenerateResponse toResponse(GeneratedImage image) {
        String message = switch (image.getStatus()) {
            case "PROCESSING" -> "Generation in progress...";
            case "FAILED" -> "Generation failed.";
            default -> "Success";
        };

        return new GenerateResponse(
                image.getId(),
                image.getOutputImage(),
                image.getStatus(),
                message
        );
    }
}
