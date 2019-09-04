package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.models.*;
import com.Auctions.backEnd.repositories.*;
import com.Auctions.backEnd.responses.BidRes;
import com.Auctions.backEnd.responses.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;



@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/bid")
public class BidController extends BaseController{

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BidRepository bidRepository;

    @Autowired
    public BidController(UserRepository userRepository, ItemRepository itemRepository,
                          BidRepository bidRepository) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.bidRepository = bidRepository;
    }


    /**
     * A user can get the details of a bid using the bidId
     *
     * @param bidId - the id of the bid
     * @return the bid
     */
    @GetMapping("/{bidId}")
    public ResponseEntity getBid(@PathVariable (value = "bidId") long bidId){
        Bid bid = bidRepository.findBidById(bidId);
        if(bid == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
                    "Error",
                    "Invalid id. Bid not found"
            ));
        }

        return ResponseEntity.ok(bid);
    }


    /**
     * A user can participate in an auction by making a bid
     *
     * @param offer - the amount of the bid
     * @return the created bid
     */
    @PostMapping("/makeBid/{itemId}")
    public ResponseEntity makeBid(@PathVariable (value = "itemId") long itemId,
                                  @RequestParam Double offer){

        User requester = requestUser();

        Item item = itemRepository.findItemById(itemId);
        if (item == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
                    "Error",
                    "Item not found. Invalid item Id"
            ));
        }

        if(checkAuction(item)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "Auction has been completed and no bids can be made"
            ));
        }

        if(item.getSeller().equals(requester)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "You cannot bid at your own auction"
            ));
        }
//TODO fix
        if(java.lang.Double.compare(offer, item.getCurrently()) <= 0){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "Offer cannot be equal or less than the current best offer or the initial price"
            ));
        }

        item.setCurrently(offer);
        if(java.lang.Double.compare(item.getBuyPrice(), offer) <= 0){
            item.setAuctionCompleted(true);
            notifySeller(item);
        }

        Bid bid = new Bid(new Date());
        bid.setBidder(requester);
        bid.setItem(item);
        bid.setOffer(offer);
        bidRepository.save(bid);

        requester.getBids().add(bid);
        userRepository.save(requester);

        item.getBids().add(bid);
        itemRepository.save(item);

        return ResponseEntity.ok(new BidRes(bid, item.isAuctionCompleted()));
    }
}