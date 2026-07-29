package com.virtualtryon.Service.impl;

import com.virtualtryon.Dto.FastApiResponse;
import com.virtualtryon.Service.AIClient;
import com.virtualtryon.Service.AIService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class AIServiceImpl implements AIService {

    private final AIClient aiClient;

    private static final Path UPLOADS_PERSON_DIR = Paths.get("uploads", "person");
    private static final Path UPLOADS_FABRIC_DIR = Paths.get("uploads", "fabric");
    private static final Path OUTPUTS_GARMENT_DIR = Paths.get("outputs", "garment");

    public AIServiceImpl(AIClient aiClient) {
        this.aiClient = aiClient;
        try {
            Files.createDirectories(UPLOADS_PERSON_DIR);
            Files.createDirectories(UPLOADS_FABRIC_DIR);
            Files.createDirectories(OUTPUTS_GARMENT_DIR);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create storage directories", e);
        }
    }

    @Override
    public byte[] generateTryOn(MultipartFile person, MultipartFile fabric, String garmentType) {
        try {
            // 1. Save fabric file
            String fabricFileName = UUID.randomUUID() + "_" + fabric.getOriginalFilename().replaceAll("\\s+", "_");
            Path fabricPath = UPLOADS_FABRIC_DIR.resolve(fabricFileName);
            Files.copy(fabric.getInputStream(), fabricPath, StandardCopyOption.REPLACE_EXISTING);

            // 2. Save person file
            String personFileName = UUID.randomUUID() + "_" + person.getOriginalFilename().replaceAll("\\s+", "_");
            Path personPath = UPLOADS_PERSON_DIR.resolve(personFileName);
            Files.copy(person.getInputStream(), personPath, StandardCopyOption.REPLACE_EXISTING);

            // 3. Call FastAPI to generate garment
            Resource fabricResource = new FileSystemResource(fabricPath.toFile());
            FastApiResponse garmentResponse = aiClient.generateGarment(fabricResource, garmentType);

            if (garmentResponse == null || !"SUCCESS".equalsIgnoreCase(garmentResponse.getStatus()) 
                    || garmentResponse.getImageUrl() == null) {
                throw new RuntimeException("FastAPI garment generation failed: " 
                        + (garmentResponse != null ? garmentResponse.getMessage() : "No response"));
            }

            // 4. Download generated garment image
            byte[] garmentBytes = aiClient.downloadImage(garmentResponse.getImageUrl());
            String garmentFileName = UUID.randomUUID() + "_garment.png";
            Path garmentPath = OUTPUTS_GARMENT_DIR.resolve(garmentFileName);
            Files.write(garmentPath, garmentBytes);

            // 5. Call FastAPI to generate final try-on
            Resource personResource = new FileSystemResource(personPath.toFile());
            Resource garmentResource = new FileSystemResource(garmentPath.toFile());
            FastApiResponse tryOnResponse = aiClient.generateTryOn(personResource, garmentResource);

            if (tryOnResponse == null || !"SUCCESS".equalsIgnoreCase(tryOnResponse.getStatus()) 
                    || tryOnResponse.getImageUrl() == null) {
                throw new RuntimeException("FastAPI try-on generation failed: " 
                        + (tryOnResponse != null ? tryOnResponse.getMessage() : "No response"));
            }

            // 6. Download and return final try-on image bytes
            return aiClient.downloadImage(tryOnResponse.getImageUrl());

        } catch (IOException e) {
            throw new RuntimeException("Failed to process try-on flow files", e);
        }
    }
}
