package com.virtualtryon.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class HistoryResponse {

    private Long historyId;

    private Long userId;

    private String userName;

    private Long generatedImageId;

    private String outputImage;

    private LocalDateTime createdAt;
}