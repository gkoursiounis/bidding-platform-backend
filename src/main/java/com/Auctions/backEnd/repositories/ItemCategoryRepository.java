package com.Auctions.backEnd.repositories;

import com.Auctions.backEnd.models.Item;
import com.Auctions.backEnd.models.ItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemCategoryRepository extends JpaRepository<ItemCategory, Long> {

    ItemCategory findItemCategoryById(Long id);

    ItemCategory findItemCategoryByName(String name);

    @Query("SELECT distinct(ic.name) FROM ItemCategory ic")
    List<String> getAllCategoriesNames();
}