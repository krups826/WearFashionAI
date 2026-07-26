package com.virtualtryon.Service.impl;

import com.virtualtryon.Dto.HistoryResponse;
import com.virtualtryon.Entity.GeneratedImage;
import com.virtualtryon.Entity.History;
import com.virtualtryon.Entity.User;
import com.virtualtryon.Repository.GeneratedImageRepository;
import com.virtualtryon.Repository.HistoryRepository;
import com.virtualtryon.Repository.UserRepository;
import com.virtualtryon.Service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {

    private final HistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final GeneratedImageRepository generatedImageRepository;

    @Override
    public HistoryResponse addHistory(Long userId, Long generatedImageId) {
        GeneratedImage generatedImage = generatedImageRepository.findById(generatedImageId)
                .orElseThrow(() -> new RuntimeException("Generated image not found"));

        User user = null;
        if (userId != null) {
            user = userRepository.findById(userId).orElse(null);
        }

        // If no user found/provided, get or create a default user to prevent DB foreign key errors
        if (user == null) {
            user = userRepository.findAll().stream().findFirst().orElseGet(() -> {
                User u = new User();
                u.setName("Guest User");
                u.setEmail("guest@wearfashion.com");
                u.setPassword("password");
                u.setEnabled(true);
                u.setTheme("LIGHT");
                u.setCreatedAt(LocalDateTime.now());
                return userRepository.save(u);
            });
        }

        History history = new History();
        history.setUser(user);
        history.setGeneratedImage(generatedImage);
        history.setCreatedAt(LocalDateTime.now());

        history = historyRepository.save(history);

        // Update the GeneratedImage's user if not set
        if (generatedImage.getUser() == null) {
            generatedImage.setUser(user);
            generatedImageRepository.save(generatedImage);
        }

        return mapToResponse(history);
    }

    @Override
    public List<HistoryResponse> getHistoryByUserId(Long userId) {
        return historyRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<HistoryResponse> getAllHistory() {
        return historyRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public void deleteHistory(Long id) {
        History history = historyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("History entry not found"));
        historyRepository.delete(history);
    }

    @Override
    public void clearUserHistory(Long userId) {
        if (userId != null) {
            List<History> userHistory = historyRepository.findByUserIdOrderByCreatedAtDesc(userId);
            historyRepository.deleteAll(userHistory);
        } else {
            historyRepository.deleteAll();
        }
    }

    private HistoryResponse mapToResponse(History history) {
        return new HistoryResponse(
                history.getId(),
                history.getUser().getId(),
                history.getUser().getName(),
                history.getGeneratedImage().getId(),
                history.getGeneratedImage().getOutputImage(),
                history.getCreatedAt()
        );
    }
}
