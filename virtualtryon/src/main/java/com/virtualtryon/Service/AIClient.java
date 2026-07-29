package com.virtualtryon.Service;

import com.virtualtryon.Dto.FastApiResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class AIClient {

    private final WebClient webClient;

    public AIClient(WebClient fastApiWebClient) {
        this.webClient = fastApiWebClient;
    }

    public FastApiResponse generateGarment(Resource fabric, String garmentType) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("fabric", fabric);
        builder.part("garment_type", garmentType);

        return webClient.post()
                .uri("/generate-garment")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(FastApiResponse.class)
                .block();
    }

    public FastApiResponse generateTryOn(Resource person, Resource garment) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("person", person);
        builder.part("garment", garment);

        return webClient.post()
                .uri("/generate-tryon")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(FastApiResponse.class)
                .block();
    }

    public byte[] downloadImage(String imageUrl) {
        return webClient.get()
                .uri(imageUrl)
                .accept(MediaType.IMAGE_PNG, MediaType.IMAGE_JPEG, MediaType.APPLICATION_OCTET_STREAM)
                .retrieve()
                .bodyToMono(byte[].class)
                .block();
    }
}
