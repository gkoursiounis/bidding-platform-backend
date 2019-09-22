package com.Auctions.backEnd.repositories;

import  com.Auctions.backEnd.models.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {
    Item findItemById(Long id);

    @Query("SELECT i FROM Item i WHERE i.auctionCompleted = 'false' ORDER BY i.createdAt DESC")
    Page<Item> getAllOpenAuctions(Pageable pageable);

    @Query("SELECT i FROM Item i WHERE i.auctionCompleted = 'false' ORDER BY i.createdAt DESC")
    List<Item> getAllOpenAuctions();

    @Query("SELECT i FROM Item i ORDER BY i.createdAt DESC")
    List<Item> getAll();

    @Query("SELECT i.name from Item i WHERE (locate(:query, lower(i.name)) <> 0)")
    List<String> searchByName(@Param("query") String query);

    @Query(
            "select ic.name from Item i join i.categories ic " +
            "where locate(:query, lower(ic.name)) <> 0")
    List<String> searchByCategory(@Param("query") String query);

    @Query(
            "select i from Item i join i.categories ic " +
            "where (locate(:query, lower(i.name)) <> 0) or " +
            "(locate(:query, lower(i.description)) <> 0) or " +
            "locate(:query, lower(ic.name)) <> 0"
    )
    List<Item> searchItems(@Param("query") String query);

    @Query(
            "select i from Item i " +
            "order by i.bids.size DESC "
    )
    List<Item> popularItems();
}