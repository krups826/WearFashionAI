package com.virtualtryon.Repository;

import com.virtualtryon.Entity.GeneratedImage;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface GeneratedImageRepository
        extends JpaRepository<GeneratedImage, Long> {

    List<GeneratedImage>
    findByUserIdOrderByGeneratedAtDesc(
            Long userId
    );
}