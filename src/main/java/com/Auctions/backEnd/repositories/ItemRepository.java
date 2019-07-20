package com.Auctions.backEnd.repositories;

import  com.Auctions.backEnd.models.Item;
import com.Auctions.backEnd.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    Item findByName(String name);
    Item findItemById(Long id);

    //DISPLAY COMPLETED AUCTION
    @Query("SELECT i FROM Item i WHERE :categories IN i.categories")
    List<Item> findItemByCategory(@Param("categories") List<String> categories);

    @Query("SELECT i FROM Item i WHERE i.auctionCompleted = 'true'")
    List<Item> getAllcompletedAuctions();

    @Query("SELECT i FROM Item i WHERE i.auctionCompleted = 'false'")
    List<Item> getAllopenAuctions();

    @Query("SELECT i FROM Item i")
    List<Item> getAllAuctions();

    @Query("SELECT i FROM Item i where i.currently >= :lowerPrice and i.buyPrice <= :higherPrice")
    List<Item> searchByPrice(Double lowerPrice, Double higherPrice);
}