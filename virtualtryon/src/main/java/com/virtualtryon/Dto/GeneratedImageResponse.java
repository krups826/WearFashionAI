package com.virtualtryon.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
@AllArgsConstructor
public class GeneratedImageResponse {

    private Long id;

    private String outputImage;

    private LocalDateTime generatedAt;

    private String status;

    private Long clothingId;

    private Long userId;

    private Long personId;
}