package com.virtualtryon.Service;

import com.virtualtryon.Dto.VirtualTryOnResponse;
import org.springframework.web.multipart.MultipartFile;

public interface VirtualTryOnService {

    VirtualTryOnResponse generateTryOn(
            Long userId,
            Long personId,
            Long clothingId,
            MultipartFile person,
            MultipartFile fabric,
            String garmentType
    );
}