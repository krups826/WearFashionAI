package com.virtualtryon.Controller;

import com.virtualtryon.Dto.ClothingResponse;
import com.virtualtryon.Dto.UploadClothingRequest;
import com.virtualtryon.Service.ClothingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/clothing")
@RequiredArgsConstructor
public class ClothingController {

    private final ClothingService clothingService;

    @PostMapping("/upload")
    public ResponseEntity<ClothingResponse> uploadClothing(@ModelAttribute UploadClothingRequest request){
        return ResponseEntity.ok(clothingService.uploadClothing(request));
    }

    @GetMapping
    public ResponseEntity<List<ClothingResponse>> getAllClothing() {
        return ResponseEntity.ok(clothingService.getAllClothing());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClothingResponse> getById(@PathVariable Long id) {

        return ResponseEntity.ok(clothingService.getClothingById(id));

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {

        clothingService.deleteClothing(id);

        return ResponseEntity.ok("Clothing deleted successfully.");

    }
}
