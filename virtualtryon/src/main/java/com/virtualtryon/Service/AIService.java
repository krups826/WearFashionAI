package com.virtualtryon.Service;

import org.springframework.web.multipart.MultipartFile;

public interface AIService {
    byte[] generateTryOn(MultipartFile person, MultipartFile fabric, String garmentType);
}
