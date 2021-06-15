package com.auction.repo;

import com.auction.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepo extends JpaRepository<Category, Long> {
    Page<Category> findAll(Pageable pageable);
    Optional<Category> findByName(String name);
}
