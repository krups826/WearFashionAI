package com.virtualtryon.Repository;

import com.virtualtryon.Entity.AIReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AIReportRepository extends JpaRepository<AIReport, Long> {
    Optional<AIReport> findByGeneratedImageId(Long generatedImageId);
}