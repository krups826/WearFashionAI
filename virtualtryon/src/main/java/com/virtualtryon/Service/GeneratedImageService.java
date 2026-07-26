package com.virtualtryon.Service;

import com.virtualtryon.Dto.GenerateRequest;
import com.virtualtryon.Dto.GenerateResponse;

import java.util.List;

public interface GeneratedImageService {

    GenerateResponse generate(GenerateRequest request);

    List<GenerateResponse> getAllGeneratedImages();

    GenerateResponse getGeneratedImageById(Long id);
}
