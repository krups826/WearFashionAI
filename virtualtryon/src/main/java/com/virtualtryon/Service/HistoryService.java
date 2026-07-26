package com.virtualtryon.Service;

import com.virtualtryon.Dto.HistoryResponse;
import java.util.List;

public interface HistoryService {
    HistoryResponse addHistory(Long userId, Long generatedImageId);
    List<HistoryResponse> getHistoryByUserId(Long userId);
    List<HistoryResponse> getAllHistory();
    void deleteHistory(Long id);
    void clearUserHistory(Long userId);
}
