package com.virtualtryon.upload;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String saveFile(MultipartFile file);

    void deleteFile(String filePath);
}
