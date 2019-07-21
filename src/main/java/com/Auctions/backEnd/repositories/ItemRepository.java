package com.Auctions.backEnd.repositories;

import  com.Auctions.backEnd.models.Item;
import com.Auctions.backEnd.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    Item findByName(String name);
    Item findItemById(Long id);

    //DISPLAY COMPLETED AUCTION
    @Query("SELECT i FROM Item i WHERE :categories IN i.categories and i.auctionCompleted = 'false'")
    List<Item> findItemByCategory(@Param("categories") List<String> categories);

    @Query("SELECT i FROM Item i WHERE i.auctionCompleted = 'true' and i.seller = :requester")
    List<Item> getAllcompletedAuctions(User requester);

    @Query("SELECT i FROM Item i WHERE i.auctionCompleted = 'false'")
    List<Item> getAllopenAuctions();

    @Query("SELECT i FROM Item i")
    List<Item> getAllAuctions();

    @Query("SELECT i FROM Item i where i.currently >= :lowerPrice and i.buyPrice <= :higherPrice")
    List<Item> searchByPrice(Double lowerPrice, Double higherPrice);

    @Query("SELECT i FROM Item i where i.currently >= :lowerPrice")
    List<Item> searchByLowerPrice(Double lowerPrice);

    @Query("SELECT i FROM Item i where i.buyPrice <= :higherPrice")
    List<Item> searchByHigherPrice(Double higherPrice);

    @Query("SELECT i FROM Item i where (locate( lower(:query), lower(i.location.locationTitle)) <> 0)")
    List<Item> searchByLocation(@Param("query") String location);

    @Query("SELECT i FROM Item i where (locate( lower(:query), lower(i.description)) <> 0)")
    List<Item> searchByDescription(@Param("query") String keyword);

    @Query(
            "select i from Item i, ItemCategory ic " +
            "where (locate(:query, lower(i.name)) <> 0) or " +
            "(locate(:query, lower(i.description)) <> 0) or " +
            "(locate(:query, lower(ic.name)) <> 0 and ic in i.categories)"
    )
    List<Item> searchItems(@Param("query") String query);


}