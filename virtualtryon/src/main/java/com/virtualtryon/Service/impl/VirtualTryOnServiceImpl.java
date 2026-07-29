package com.virtualtryon.Service.Impl;

import com.virtualtryon.Dto.VirtualTryOnResponse;

import com.virtualtryon.Entity.Clothing;
import com.virtualtryon.Entity.GeneratedImage;
import com.virtualtryon.Entity.History;
import com.virtualtryon.Entity.Person;
import com.virtualtryon.Entity.User;

import com.virtualtryon.Repository.ClothingRepository;
import com.virtualtryon.Repository.GeneratedImageRepository;
import com.virtualtryon.Repository.HistoryRepository;
import com.virtualtryon.Repository.PersonImageRepository;
import com.virtualtryon.Repository.UserRepository;

import com.virtualtryon.Service.VirtualTryOnService;
import com.virtualtryon.Service.AIService;

import org.springframework.core.io.ByteArrayResource;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import org.springframework.web.client.RestTemplate;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.time.LocalDateTime;


@Service
public class VirtualTryOnServiceImpl
        implements VirtualTryOnService {


    @Value("${wearfashion.gpu.base-url:https://mutt-udder-easter.ngrok-free.dev}")
    private String gpuBaseUrl;

    private static final String GENERATED_IMAGE_DIR =
            "outputs/tryon";


    private final UserRepository userRepository;

    private final PersonImageRepository personImageRepository;

    private final ClothingRepository clothingRepository;

    private final GeneratedImageRepository generatedImageRepository;

    private final HistoryRepository historyRepository;

    private final RestTemplate restTemplate;

    private final AIService aiService;


    public VirtualTryOnServiceImpl(
            UserRepository userRepository,
            PersonImageRepository personImageRepository,
            ClothingRepository clothingRepository,
            GeneratedImageRepository generatedImageRepository,
            HistoryRepository historyRepository,
            AIService aiService
    ) {

        this.userRepository =
                userRepository;

        this.personImageRepository =
                personImageRepository;

        this.clothingRepository =
                clothingRepository;

        this.generatedImageRepository =
                generatedImageRepository;

        this.historyRepository =
                historyRepository;

        this.aiService =
                aiService;

        this.restTemplate =
                new RestTemplate();


        try {

            Files.createDirectories(
                    Paths.get(
                            GENERATED_IMAGE_DIR
                    )
            );

        }
        catch (IOException exception) {

            throw new RuntimeException(
                    "Failed to create generated image directory",
                    exception
            );
        }
    }


    @Override
    public VirtualTryOnResponse generateTryOn(
            Long userId,
            Long personId,
            Long clothingId,
            MultipartFile person,
            MultipartFile fabric,
            String garmentType
    ) {

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "User not found: "
                                                + userId
                                )
                        );


        Person personImageEntity =
                personImageRepository
                        .findById(personId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Person not found: "
                                                + personId
                                )
                        );


        Clothing clothing =
                clothingRepository
                        .findById(clothingId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Clothing not found: "
                                                + clothingId
                                )
                        );


        GeneratedImage generatedImage =
                new GeneratedImage();


        generatedImage.setUser(
                user
        );

        generatedImage.setPerson(
                personImageEntity
        );

        generatedImage.setClothing(
                clothing
        );

        generatedImage.setStatus(
                "PROCESSING"
        );

        generatedImage.setGeneratedAt(
                LocalDateTime.now()
        );


        generatedImage =
                generatedImageRepository.save(
                        generatedImage
                );


        try {

            MultiValueMap<String, Object> body =
                    new LinkedMultiValueMap<>();


            body.add(
                    "person",
                    createFileResource(
                            person
                    )
            );


            body.add(
                    "garment",
                    createFileResource(fabric)
            );

            System.out.println("================================");
            System.out.println("CATEGORY SENT TO AI = " + garmentType);
            System.out.println("================================");

            String category = garmentType.trim().toLowerCase();

            switch (category) {
                case "shirt":
                case "t-shirt":
                case "tshirt":
                case "top":
                case "jacket":
                    category = "tops";
                    break;

                case "pant":
                case "pants":
                case "jeans":
                case "trouser":
                    category = "bottoms";
                    break;

                case "dress":
                case "gown":
                    category = "one-pieces";
                    break;

                default:
                    category = "tops";
            }

            body.add("category", category);

            body.add(
                    "garment_photo_type",
                    "flat-lay"
            );

            HttpHeaders headers =
                    new HttpHeaders();


            headers.setContentType(
                    MediaType.MULTIPART_FORM_DATA
            );


            HttpEntity<
                    MultiValueMap<String, Object>
                    > requestEntity =
                    new HttpEntity<>(
                            body,
                            headers
                    );


            System.out.println(
                    "================================"
            );

            System.out.println(
                    "CALLING AISERVICE FASTAPI WRAPPERS"
            );

            System.out.println(
                    "================================"
            );


            byte[] imageBytes = aiService.generateTryOn(person, fabric, garmentType);

            if (imageBytes == null || imageBytes.length == 0) {

                throw new RuntimeException(
                        "AI image response is empty"
                );
            }
            if (
                    imageBytes == null
                            || imageBytes.length == 0
            ) {

                throw new RuntimeException(
                        "AI image response is empty"
                );
            }


            String fileName =
                    System.currentTimeMillis()
                            + "_tryon.png";

            Path localImagePath =
                    Paths.get(
                            GENERATED_IMAGE_DIR,
                            fileName
                    );


            Files.write(
                    localImagePath,
                    imageBytes
            );


            String savedImagePath =
                    localImagePath
                            .toAbsolutePath()
                            .normalize()
                            .toString();


            generatedImage.setStatus(
                    "COMPLETED"
            );


            generatedImage.setOutputImage(
                    savedImagePath
            );


            generatedImage =
                    generatedImageRepository.save(
                            generatedImage
                    );


            History history =
                    new History();


            history.setUser(
                    user
            );


            history.setGeneratedImage(
                    generatedImage
            );


            history.setCreatedAt(
                    LocalDateTime.now()
            );


            historyRepository.save(
                    history
            );


            System.out.println(
                    "================================"
            );

            System.out.println(
                    "SPRING BOOT TRY-ON COMPLETE"
            );

            System.out.println(
                    "================================"
            );


            System.out.println(
                    "GENERATED IMAGE ID: "
                            + generatedImage.getId()
            );


            System.out.println(
                    "SAVED IMAGE: "
                            + savedImagePath
            );


            String imageUrl =
                    "/api/tryon/image/"
                            + generatedImage.getId();


            return new VirtualTryOnResponse(
                    generatedImage.getId(),
                    generatedImage.getStatus(),
                    "Try-on generated successfully",
                    imageUrl
            );
        }
        catch (Exception exception) {

            generatedImage.setStatus(
                    "FAILED"
            );


            generatedImageRepository.save(
                    generatedImage
            );


            System.out.println(
                    "================================"
            );

            System.out.println(
                    "VIRTUAL TRY-ON ERROR"
            );

            System.out.println(
                    "================================"
            );


            System.out.println(
                    exception.getMessage()
            );


            throw new RuntimeException(
                    "Virtual try-on generation failed: "
                            + exception.getMessage(),
                    exception
            );
        }
    }


    private ByteArrayResource createFileResource(
            MultipartFile file
    ) throws IOException {

        return new ByteArrayResource(
                file.getBytes()
        ) {

            @Override
            public String getFilename() {

                return file.getOriginalFilename();
            }
        };
    }
}