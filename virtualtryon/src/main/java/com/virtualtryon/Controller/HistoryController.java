package com.virtualtryon.Controller;

import com.virtualtryon.Dto.HistoryResponse;
import com.virtualtryon.Service.HistoryService;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(
            HistoryService historyService
    ) {
        this.historyService = historyService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<HistoryResponse>>
    getUserHistory(
            @PathVariable Long userId
    ) {

        return ResponseEntity.ok(
                historyService.getHistoryByUserId(
                        userId
                )
        );
    }
}