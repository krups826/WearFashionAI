package com.virtualtryon.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UploadClothingRequest {

    private String clothName;

    private String clothType;

    private String color;

    private MultipartFile image;

}
