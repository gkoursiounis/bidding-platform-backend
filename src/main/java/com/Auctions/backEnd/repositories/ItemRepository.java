package com.Auctions.backEnd.repositories;

import com.Auctions.backEnd.models.Item;
import com.Auctions.backEnd.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    Item findByName(String name);
    Item findItemById(Long id);

    @Query("SELECT i FROM Item i WHERE :category IN i.categories")
    User findItemByCategory(@Param("category") String category);
}