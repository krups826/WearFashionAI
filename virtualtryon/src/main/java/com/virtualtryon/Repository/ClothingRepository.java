package com.virtualtryon.Repository;

import com.virtualtryon.Entity.Clothing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;

public interface ClothingRepository extends JpaRepository<Clothing, Long> {
}