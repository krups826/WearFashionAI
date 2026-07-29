package com.virtualtryon.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    private final FastApiConfig fastApiConfig;

    public WebClientConfig(FastApiConfig fastApiConfig) {
        this.fastApiConfig = fastApiConfig;
    }

    @Bean
    public WebClient fastApiWebClient() {
        return WebClient.builder()
                .baseUrl(fastApiConfig.getBaseUrl())
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(50 * 1024 * 1024)) // 50MB
                .build();
    }
}
