package com.virtualtryon.Service.impl;

import com.virtualtryon.Dto.AIReportResponse;
import com.virtualtryon.Entity.AIReport;
import com.virtualtryon.Entity.Clothing;
import com.virtualtryon.Entity.GeneratedImage;
import com.virtualtryon.Repository.AIReportRepository;
import com.virtualtryon.Repository.GeneratedImageRepository;
import com.virtualtryon.Service.AIReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AIReportServiceImpl implements AIReportService {

    private final AIReportRepository aiReportRepository;
    private final GeneratedImageRepository generatedImageRepository;

    @Override
    public AIReportResponse generateReport(Long generatedImageId) {
        GeneratedImage generatedImage = generatedImageRepository.findById(generatedImageId)
                .orElseThrow(() -> new RuntimeException("Generated image not found"));

        // If report already exists for this image, return it
        Optional<AIReport> existingReport = aiReportRepository.findByGeneratedImageId(generatedImageId);
        if (existingReport.isPresent()) {
            return mapToResponse(existingReport.get());
        }

        Clothing clothing = generatedImage.getClothing();
        String type = clothing != null ? clothing.getClothType() : "casual";
        String color = clothing != null ? clothing.getColor() : "neutral";
        String name = clothing != null ? clothing.getClothName() : "Outfit";

        // Generate styled details
        String occasion = determineOccasion(type);
        String colorSuggestion = determineMatchingColors(color);
        String recommendation = generateStylingText(name, type, color, occasion);
        int rating = (int) (Math.random() * 2) + 4; // Generate a rating of 4 or 5 stars

        AIReport report = new AIReport();
        report.setGeneratedImage(generatedImage);
        report.setOccasion(occasion);
        report.setColorSuggestion(colorSuggestion);
        report.setRecommendation(recommendation);
        report.setRating(rating);

        report = aiReportRepository.save(report);
        return mapToResponse(report);
    }

    @Override
    public AIReportResponse getReportById(Long id) {
        AIReport report = aiReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AI Report not found"));
        return mapToResponse(report);
    }

    @Override
    public AIReportResponse getReportByGeneratedImageId(Long generatedImageId) {
        AIReport report = aiReportRepository.findByGeneratedImageId(generatedImageId)
                .orElseThrow(() -> new RuntimeException("AI Report not found for this generated image"));
        return mapToResponse(report);
    }

    @Override
    public List<AIReportResponse> getAllReports() {
        return aiReportRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public void deleteReport(Long id) {
        AIReport report = aiReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AI Report not found"));
        aiReportRepository.delete(report);
    }

    private String determineOccasion(String type) {
        if (type == null) return "Casual Outing";
        String lower = type.toLowerCase();
        if (lower.contains("shirt") || lower.contains("blazer") || lower.contains("suit") || lower.contains("formal")) {
            return "Business Casual / Semi-Formal";
        } else if (lower.contains("dress") || lower.contains("gown") || lower.contains("skirt")) {
            return "Evening Party / Special Occasions";
        } else if (lower.contains("sport") || lower.contains("active") || lower.contains("hoodie") || lower.contains("sweat")) {
            return "Athletic & Casual Loungewear";
        } else if (lower.contains("jean") || lower.contains("jacket") || lower.contains("denim")) {
            return "Streetwear / Casual Wear";
        }
        return "Everyday Casual Wear";
    }

    private String determineMatchingColors(String color) {
        if (color == null) return "Neutral tones (White, Black, Gray)";
        String lower = color.toLowerCase();
        if (lower.contains("black")) {
            return "Contrast with White, Mustard Yellow, or Emerald Green";
        } else if (lower.contains("white")) {
            return "Pair with Denim Blue, Soft Pink, or Navy";
        } else if (lower.contains("red")) {
            return "Complement with Charcoal Gray, Beige, or Dark Denim";
        } else if (lower.contains("blue")) {
            return "Style with Tan/Beige, Cream, or Crisp White";
        } else if (lower.contains("green")) {
            return "Match with Tan, Dark Gray, or Gold accessories";
        } else if (lower.contains("yellow")) {
            return "Balance with Navy Blue, Black, or Pure White";
        } else if (lower.contains("pink")) {
            return "Contrast with White, Light Denim, or Soft Gray";
        }
        return "Neutral accents (Cream, Beige, Charcoal)";
    }

    private String generateStylingText(String name, String type, String color, String occasion) {
        return "This " + color + " " + type + " (" + name + ") is a versatile piece perfect for " + occasion + 
               ". To complete the look, we recommend styling it with sleek fitted trousers or premium dark-wash denim. " +
               "For footwear, pair with clean white sneakers for a relaxed vibe, or loafers/heels to elevate the outfit. " +
               "Accessorize minimally with silver or gold accents to keep the focus on the clean silhouette of this garment.";
    }

    private AIReportResponse mapToResponse(AIReport report) {
        return new AIReportResponse(
                report.getId(),
                report.getRating(),
                report.getRecommendation(),
                report.getOccasion(),
                report.getColorSuggestion(),
                report.getGeneratedImage().getId(),
                report.getGeneratedImage().getOutputImage()
        );
    }
}
