package com.virtualtryon.Config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
public class FastApiConfig {

    @Value("${wearfashion.python.base-url:http://127.0.0.1:8000}")
    private String baseUrl;

    @Value("${wearfashion.python.timeout-ms:7200000}")
    private int timeoutMs;
}
