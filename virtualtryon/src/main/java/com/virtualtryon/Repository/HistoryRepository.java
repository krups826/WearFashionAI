package com.virtualtryon.Repository;

import com.virtualtryon.Entity.History;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface HistoryRepository
        extends JpaRepository<History, Long> {

    List<History>
    findByUserIdOrderByCreatedAtDesc(
            Long userId
    );
}