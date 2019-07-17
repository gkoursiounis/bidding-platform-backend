package com.Auctions.backEnd.repositories;

import com.Auctions.backEnd.models.ItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemCategoryRepository extends JpaRepository<ItemCategory, Long> {
    ItemCategory findItemCategoryById(Long id);
    // Item findById(Long id);
}