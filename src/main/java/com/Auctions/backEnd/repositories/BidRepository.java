
package com.Auctions.backEnd.repositories;

import com.Auctions.backEnd.models.Account;
import com.Auctions.backEnd.models.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {
    Bid findBidById(Long id);
}