package com.virtualtryon.Service;

import com.virtualtryon.Dto.ClothingResponse;
import com.virtualtryon.Dto.UploadClothingRequest;

import java.util.List;

public interface ClothingService {

    ClothingResponse uploadClothing(UploadClothingRequest request);

    List<ClothingResponse> getAllClothing();

    ClothingResponse getClothingById(Long id);

    void deleteClothing(Long id);
}
