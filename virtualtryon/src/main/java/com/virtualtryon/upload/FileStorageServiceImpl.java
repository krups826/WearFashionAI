package com.virtualtryon.upload;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService{

    @Override
    public String saveFile(MultipartFile file) {

        try {

            Path uploadPath = Paths.get("uploads", "clothes");

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFileName = file.getOriginalFilename().replaceAll("\\s+", "_");;

            String fileName = UUID.randomUUID() + "-" + originalFileName;

            Path filePath = uploadPath.resolve(fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "uploads/clothes/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("Failed to store image.", e);
        }
    }

    @Override
    public void deleteFile(String imagePath) {

        try {
            Files.deleteIfExists(Paths.get(imagePath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete image.", e);
        }

    }
}

