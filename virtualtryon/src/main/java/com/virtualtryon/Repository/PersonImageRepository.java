package com.virtualtryon.Repository;

import com.virtualtryon.Entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonImageRepository extends JpaRepository<Person, Long> {
}