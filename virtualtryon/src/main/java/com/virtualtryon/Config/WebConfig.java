package com.virtualtryon.Config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path projectRoot = Paths.get("").toAbsolutePath().normalize();
        Path appUploads = projectRoot.resolve("uploads");
        Path aiUploads = projectRoot.resolve("../virtualtryon-ai/uploads");
        Path aiCatVtonUploads = projectRoot.resolve("../virtualtryon-ai/CatVTON/uploads");

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(
                        toDirectoryUri(appUploads),
                        toDirectoryUri(aiUploads),
                        toDirectoryUri(aiCatVtonUploads)
                );
    }

    private String toDirectoryUri(Path path) {
        String uri = path.toAbsolutePath().normalize().toUri().toString();
        return uri.endsWith("/") ? uri : uri + "/";
    }
}
