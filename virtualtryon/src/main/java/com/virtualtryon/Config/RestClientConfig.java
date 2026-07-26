package com.virtualtryon.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${wearfashion.python.timeout-ms:7200000}")
    private int pythonTimeoutMs;

    @Bean
    public RestClient restClient() {
        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(30_000);
        requestFactory.setReadTimeout(pythonTimeoutMs);

        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }
}
