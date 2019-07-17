package com.Auctions.backEnd.responses;

import com.Auctions.backEnd.models.Bid;
import lombok.*;

@Getter
@Setter
public class BidRes {
    private Bid bid;
    private boolean auctionCompleted;

    public BidRes(Bid bid, boolean auctionCompleted){
        this.bid = bid;
        this.auctionCompleted = auctionCompleted;
    }
}