package com.virtualtryon.Service.impl;


import com.virtualtryon.Dto.ClothingResponse;
import com.virtualtryon.Dto.UploadClothingRequest;


import com.virtualtryon.Entity.Clothing;


import com.virtualtryon.Repository.ClothingRepository;


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


        fileStorageService.deleteFile(

                clothing.getImagePath()
        );


        clothingRepository.delete(
                clothing
        );
    }
}