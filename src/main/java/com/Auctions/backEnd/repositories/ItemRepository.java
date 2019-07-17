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

//    @Query("SELECT b FROM Bid b WHERE p.user IN :followings ORDER BY p.createdAt DESC")
//    User getHighestBidder(@Param("followings") Set<User> followings);
}