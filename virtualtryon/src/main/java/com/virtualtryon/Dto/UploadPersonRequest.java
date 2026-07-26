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
public class UploadPersonRequest {

    private String personName;

    private Integer age;

    private String gender;

    private MultipartFile image;
}
