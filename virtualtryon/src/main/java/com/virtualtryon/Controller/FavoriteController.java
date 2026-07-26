package com.virtualtryon.Controller;

import com.virtualtryon.Dto.FavoriteResponse;
import com.virtualtryon.Service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{generatedImageId}")
    public ResponseEntity<FavoriteResponse> addFavorite(
            @PathVariable Long generatedImageId) {

        return ResponseEntity.ok(
                favoriteService.addFavorite(generatedImageId)
        );
    }

    @GetMapping
    public ResponseEntity<List<FavoriteResponse>> getAllFavorites() {

        return ResponseEntity.ok(
                favoriteService.getAllFavorites()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFavorite(
            @PathVariable Long id) {

        favoriteService.deleteFavorite(id);

        return ResponseEntity.ok("Favorite deleted successfully.");
    }
}
