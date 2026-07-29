package com.virtualtryon.Service.impl;


import com.virtualtryon.Dto.ClothingResponse;
import com.virtualtryon.Dto.UploadClothingRequest;


import com.virtualtryon.Entity.Clothing;


import com.virtualtryon.Repository.ClothingRepository;
import com.virtualtryon.Repository.GeneratedImageRepository;
import com.virtualtryon.Repository.HistoryRepository;
import com.virtualtryon.Entity.GeneratedImage;
import com.virtualtryon.Entity.History;
import com.virtualtryon.Entity.StyleRecommendation;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;


import com.virtualtryon.Service.ClothingService;


import com.virtualtryon.upload.FileStorageService;


import lombok.RequiredArgsConstructor;


import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ClothingServiceImpl
        implements ClothingService {


    private final ClothingRepository
            clothingRepository;


    private final FileStorageService
            fileStorageService;

    private final GeneratedImageRepository generatedImageRepository;
    private final HistoryRepository historyRepository;
    private final EntityManager entityManager;


    @Override
    public ClothingResponse uploadClothing(
            UploadClothingRequest request
    ) {


        Clothing clothing =
                new Clothing();


        clothing.setClothName(
                request.getClothName()
        );


        clothing.setClothType(
                request.getClothType()
        );


        clothing.setColor(
                request.getColor()
        );


        clothing.setUploadedAt(
                LocalDateTime.now()
        );


        String materialImagePath =

                fileStorageService.saveFile(

                        request.getImage()
                );


        clothing.setImagePath(
                materialImagePath
        );


        clothing =

                clothingRepository.save(
                        clothing
                );


        System.out.println(
                "================================"
        );


        System.out.println(
                "FABRIC / MATERIAL UPLOADED"
        );


        System.out.println(
                "NAME: "
                        + clothing.getClothName()
        );


        System.out.println(
                "TYPE: "
                        + clothing.getClothType()
        );


        System.out.println(
                "COLOR: "
                        + clothing.getColor()
        );


        System.out.println(
                "MATERIAL IMAGE: "
                        + clothing.getImagePath()
        );


        System.out.println(
                "================================"
        );


        return new ClothingResponse(

                clothing.getId(),

                clothing.getClothName(),

                clothing.getClothType(),

                clothing.getColor(),

                clothing.getImagePath()
        );
    }


    @Override
    public List<ClothingResponse>
    getAllClothing() {


        return clothingRepository
                .findAll()

                .stream()

                .map(

                        clothing ->

                                new ClothingResponse(

                                        clothing.getId(),

                                        clothing.getClothName(),

                                        clothing.getClothType(),

                                        clothing.getColor(),

                                        clothing.getImagePath()
                                )
                )

                .toList();
    }


    @Override
    public ClothingResponse
    getClothingById(
            Long id
    ) {


        Clothing clothing =

                clothingRepository
                        .findById(id)

                        .orElseThrow(

                                () -> new RuntimeException(

                                        "Fabric / clothing material not found"
                                )
                        );


        return new ClothingResponse(

                clothing.getId(),

                clothing.getClothName(),

                clothing.getClothType(),

                clothing.getColor(),

                clothing.getImagePath()
        );
    }


    @Override
    @Transactional
    public void deleteClothing(
            Long id
    ) {


        Clothing clothing =

                clothingRepository
                        .findById(id)

                        .orElseThrow(

                                () -> new RuntimeException(

                                        "Fabric / clothing material not found"
                                )
                        );


        // Clean up dependent resources in GeneratedImage, History, StyleRecommendation, Favorite, AIReport
        List<GeneratedImage> dependentImages = generatedImageRepository.findAll().stream()
                .filter(img -> img.getClothing() != null && img.getClothing().getId() == id)
                .toList();

        for (GeneratedImage img : dependentImages) {
            // Delete history entries for this generated image
            List<History> historyEntries = historyRepository.findAll().stream()
                    .filter(h -> h.getGeneratedImage() != null && h.getGeneratedImage().getId() == img.getId())
                    .toList();
            historyRepository.deleteAll(historyEntries);

            // Delete style recommendations for this generated image using EntityManager
            entityManager.createQuery("DELETE FROM StyleRecommendation r WHERE r.generatedImage = :img")
                    .setParameter("img", img)
                    .executeUpdate();

            // Delete the generated image file from disk if it exists
            if (img.getOutputImage() != null) {
                try {
                    fileStorageService.deleteFile(img.getOutputImage());
                } catch (Exception ignored) {}
            }

            // Finally delete the generated image (AIReport and Favorite are cascade deleted)
            generatedImageRepository.delete(img);
        }

        if (clothing.getImagePath() != null) {
            try {
                fileStorageService.deleteFile(clothing.getImagePath());
            } catch (Exception ignored) {}
        }


        clothingRepository.delete(
                clothing
        );
    }
}