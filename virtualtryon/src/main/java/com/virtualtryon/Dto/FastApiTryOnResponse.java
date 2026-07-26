package com.virtualtryon.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FastApiTryOnResponse {

    @JsonProperty("request_id")
    private String requestId;

    private String status;

    private String message;

    @JsonProperty("output_image")
    private String outputImage;
}