package com.virtualtryon.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class VirtualTryOnResponse {

    private Long generatedImageId;

    private String status;

    private String message;

    private String outputImage;
}