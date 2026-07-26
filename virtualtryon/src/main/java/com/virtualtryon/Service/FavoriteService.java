package com.virtualtryon.Service;

import com.virtualtryon.Dto.FavoriteResponse;

import java.util.List;

public interface FavoriteService {

    FavoriteResponse addFavorite(Long generatedImageId);

    List<FavoriteResponse> getAllFavorites();

    void deleteFavorite(Long id);
}
