package com.virtualtryon.Controller;

import com.virtualtryon.Dto.GeneratedImageResponse;
import com.virtualtryon.Entity.GeneratedImage;
import com.virtualtryon.Repository.GeneratedImageRepository;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/generated-images")
public class GeneratedImageController {


    private final GeneratedImageRepository
            generatedImageRepository;


    public GeneratedImageController(
            GeneratedImageRepository
                    generatedImageRepository
    ) {

        this.generatedImageRepository =
                generatedImageRepository;
    }


    // --------------------------------
    // GET GENERATED IMAGE BY ID
    // --------------------------------

    @GetMapping("/{id}")
    public ResponseEntity<GeneratedImageResponse>
    getGeneratedImageById(
            @PathVariable Long id
    ) {

        GeneratedImage generatedImage =
                generatedImageRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Generated image not found: "
                                                + id
                                )
                        );


        return ResponseEntity.ok(
                mapToResponse(
                        generatedImage
                )
        );
    }


    // --------------------------------
    // GET ALL GENERATED IMAGES
    // --------------------------------

    @GetMapping
    public ResponseEntity<
            List<GeneratedImageResponse>
            > getAllGeneratedImages() {

        List<GeneratedImageResponse> response =
                generatedImageRepository
                        .findAll()
                        .stream()
                        .map(
                                this::mapToResponse
                        )
                        .toList();


        return ResponseEntity.ok(
                response
        );
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<
            List<GeneratedImageResponse>
            > getGeneratedImagesByUser(
            @PathVariable Long userId
    ) {

        List<GeneratedImageResponse> response =
                generatedImageRepository
                        .findByUserIdOrderByGeneratedAtDesc(
                                userId
                        )
                        .stream()
                        .map(
                                this::mapToResponse
                        )
                        .toList();


        return ResponseEntity.ok(
                response
        );
    }

    // --------------------------------
    // CONVERT ENTITY TO DTO
    // --------------------------------

    private GeneratedImageResponse mapToResponse(
            GeneratedImage generatedImage
    ) {

        String imageUrl = null;


        if (
                "COMPLETED".equalsIgnoreCase(
                        generatedImage.getStatus()
                )
                        && generatedImage.getOutputImage()
                        != null
        ) {

            imageUrl =
                    "/api/tryon/image/"
                            + generatedImage.getId();
        }


        Long clothingId =
                generatedImage.getClothing() != null
                        ? generatedImage
                        .getClothing()
                        .getId()
                        : null;


        Long userId =
                generatedImage.getUser() != null
                        ? generatedImage
                        .getUser()
                        .getId()
                        : null;


        Long personId =
                generatedImage.getPerson() != null
                        ? generatedImage
                        .getPerson()
                        .getId()
                        : null;


        return new GeneratedImageResponse(
                generatedImage.getId(),
                imageUrl,
                generatedImage.getGeneratedAt(),
                generatedImage.getStatus(),
                clothingId,
                userId,
                personId
        );
    }
}