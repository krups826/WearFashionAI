package com.virtualtryon.Service.impl;

import com.virtualtryon.Dto.FavoriteResponse;
import com.virtualtryon.Entity.Favorite;
import com.virtualtryon.Entity.GeneratedImage;
import com.virtualtryon.Repository.FavoriteRepository;
import com.virtualtryon.Repository.GeneratedImageRepository;
import com.virtualtryon.Service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final GeneratedImageRepository generatedImageRepository;

    @Override
    public FavoriteResponse addFavorite(Long generatedImageId) {

        GeneratedImage image = generatedImageRepository.findById(generatedImageId)
                .orElseThrow(() -> new RuntimeException("Generated Image not found"));

        Favorite favorite = new Favorite();

        favorite.setGeneratedImage(image);
        favorite.setCreatedAt(LocalDateTime.now());

        favorite = favoriteRepository.save(favorite);

        return new FavoriteResponse(
                favorite.getId(),
                image.getId(),
                image.getOutputImage()
        );
    }

    @Override
    public List<FavoriteResponse> getAllFavorites() {

        return favoriteRepository.findAll()
                .stream()
                .map(favorite -> new FavoriteResponse(
                        favorite.getId(),
                        favorite.getGeneratedImage().getId(),
                        favorite.getGeneratedImage().getOutputImage()
                ))
                .toList();
    }

    @Override
    public void deleteFavorite(Long id) {

        Favorite favorite = favoriteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Favorite not found"));

        favoriteRepository.delete(favorite);
    }
}
