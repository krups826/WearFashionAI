package com.virtualtryon.Controller;

import com.virtualtryon.Dto.VirtualTryOnResponse;
import com.virtualtryon.Entity.GeneratedImage;
import com.virtualtryon.Repository.GeneratedImageRepository;
import com.virtualtryon.Service.VirtualTryOnService;
import com.virtualtryon.Dto.GeneratedImageResponse;


import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;


@RestController
@RequestMapping("/api/tryon")
public class VirtualTryOnController {


    private final VirtualTryOnService virtualTryOnService;

    private final GeneratedImageRepository generatedImageRepository;


    public VirtualTryOnController(
            VirtualTryOnService virtualTryOnService,
            GeneratedImageRepository generatedImageRepository
    ) {

        this.virtualTryOnService =
                virtualTryOnService;

        this.generatedImageRepository =
                generatedImageRepository;
    }


    // --------------------------------
    // GENERATE VIRTUAL TRY-ON
    // --------------------------------

    @PostMapping("/generate")
    public ResponseEntity<VirtualTryOnResponse>
    generateTryOn(

            @RequestParam Long userId,

            @RequestParam Long personId,

            @RequestParam Long clothingId,

            @RequestParam MultipartFile person,

            @RequestParam MultipartFile fabric,

            @RequestParam String garmentType

    ) {

        System.out.println(
                "================================"
        );

        System.out.println(
                "SPRING BOOT TRY-ON API HIT"
        );

        System.out.println(
                "================================"
        );


        VirtualTryOnResponse response =
                virtualTryOnService.generateTryOn(
                        userId,
                        personId,
                        clothingId,
                        person,
                        fabric,
                        garmentType
                );


        return ResponseEntity.ok(
                response
        );
    }


    // --------------------------------
    // GET GENERATED TRY-ON IMAGE
    // --------------------------------

    @GetMapping("/image/{generatedImageId}")
    public ResponseEntity<Resource> getGeneratedImage(
            @PathVariable Long generatedImageId
    ) throws IOException {


        // --------------------------------
        // FIND GENERATED IMAGE
        // --------------------------------

        GeneratedImage generatedImage =
                generatedImageRepository
                        .findById(
                                generatedImageId
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Generated image not found: "
                                                + generatedImageId
                                )
                        );


        // --------------------------------
        // CHECK OUTPUT IMAGE PATH
        // --------------------------------

        if (
                generatedImage.getOutputImage()
                        == null
        ) {

            throw new RuntimeException(
                    "Output image is not available"
            );
        }


        // --------------------------------
        // CREATE IMAGE PATH
        // --------------------------------

        Path imagePath =
                Paths.get(
                                generatedImage.getOutputImage()
                        )
                        .toAbsolutePath()
                        .normalize();


        System.out.println(
                "================================"
        );

        System.out.println(
                "GENERATED IMAGE REQUEST"
        );

        System.out.println(
                "================================"
        );

        System.out.println(
                "GENERATED IMAGE ID: "
                        + generatedImageId
        );

        System.out.println(
                "IMAGE PATH: "
                        + imagePath
        );


        // --------------------------------
        // CREATE FILE RESOURCE
        // --------------------------------

        Resource resource =
                new FileSystemResource(
                        imagePath
                );


        // --------------------------------
        // CHECK IMAGE EXISTS
        // --------------------------------

        if (!resource.exists()) {

            throw new RuntimeException(
                    "Output image file not found: "
                            + imagePath
            );
        }


        // --------------------------------
        // RETURN IMAGE
        // --------------------------------

        return ResponseEntity
                .ok()
                .contentType(
                        MediaType.IMAGE_PNG
                )
                .body(
                        resource
                );
    }

    @GetMapping("/status/{generatedImageId}")
    public ResponseEntity<GeneratedImageResponse>
    getTryOnStatus(
            @PathVariable Long generatedImageId
    ) {

        GeneratedImage generatedImage =
                generatedImageRepository
                        .findById(
                                generatedImageId
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Generated image not found: "
                                                + generatedImageId
                                )
                        );


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


        GeneratedImageResponse response =
                new GeneratedImageResponse(
                        generatedImage.getId(),

                        imageUrl,

                        generatedImage.getGeneratedAt(),

                        generatedImage.getStatus(),

                        generatedImage.getClothing() != null
                                ? generatedImage
                                .getClothing()
                                .getId()
                                : null,

                        generatedImage.getUser() != null
                                ? generatedImage
                                .getUser()
                                .getId()
                                : null,

                        generatedImage.getPerson() != null
                                ? generatedImage
                                .getPerson()
                                .getId()
                                : null
                );


        return ResponseEntity.ok(
                response
        );
    }
}