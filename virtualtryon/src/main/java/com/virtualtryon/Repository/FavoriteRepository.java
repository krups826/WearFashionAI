package com.virtualtryon.Repository;

import com.virtualtryon.Entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
}