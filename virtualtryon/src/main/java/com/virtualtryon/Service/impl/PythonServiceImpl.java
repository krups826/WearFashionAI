package com.virtualtryon.Service.impl;

import com.virtualtryon.Dto.PythonGenerateRequest;
import com.virtualtryon.Dto.PythonGenerateResponse;
import com.virtualtryon.Service.PythonService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class PythonServiceImpl implements PythonService {

    private final RestClient restClient;

    @Value("${wearfashion.python.base-url:http://127.0.0.1:8000}")
    private String pythonBaseUrl;

    @Override
    public PythonGenerateResponse generate(
            PythonGenerateRequest request
    ) {
        String personPath = Path.of(request.personPath())
                .toAbsolutePath()
                .normalize()
                .toString();

        String materialPath = Path.of(request.materialPath())
                .toAbsolutePath()
                .normalize()
                .toString();

        PythonGenerateRequest pythonRequest = new PythonGenerateRequest(
                personPath,
                materialPath,
                request.clothType(),
                request.clothName(),
                request.color()
        );

        System.out.println("CALLING WEARFASHION PYTHON AI...");

        PythonGenerateResponse response = restClient
                .post()
                .uri(pythonBaseUrl + "/generate")
                .body(pythonRequest)
                .retrieve()
                .body(PythonGenerateResponse.class);

        if (response == null) {
            throw new RuntimeException(
                    "Empty response from WearFashion Python AI"
            );
        }

        System.out.println("AI STATUS: " + response.status());
        System.out.println("AI OUTPUT IMAGE: " + response.outputImage());

        return response;
    }
}