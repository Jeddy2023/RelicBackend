package com.example.relicv2.repository;

import com.example.relicv2.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findByName(String categoryName);
    boolean existsByName(String categoryName);
    @Query("SELECT c FROM Category c JOIN c.products p GROUP BY c ORDER BY COUNT(p) DESC")
    List<Category> findTopFiveActiveCategories();
}
