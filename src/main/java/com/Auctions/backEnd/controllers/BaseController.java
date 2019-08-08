package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.models.Bid;
import com.Auctions.backEnd.models.Item;
import com.Auctions.backEnd.models.Notification;
import com.Auctions.backEnd.models.User;
import com.Auctions.backEnd.repositories.ItemRepository;
import com.Auctions.backEnd.repositories.NotificationRepository;
import com.Auctions.backEnd.repositories.UserRepository;
import com.Auctions.backEnd.services.Security.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.Auctions.backEnd.services.Security.JWTFilter.resolveToken;

public abstract class BaseController {

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private NotificationRepository notificationRepository;


    /**
     * Approved content types for pictures
     */
    static final Set<String> contentTypes = new HashSet<>(Arrays.asList(
            "image/png",
            "image/jpeg",
            "image/gif"
    ));


    /**
     * Helper function that returns the User who makes a request
     * based on the token authentication
     *
     * @return a user
     */
     User requestUser(){

        final HttpServletRequest currentRequest =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String token = resolveToken(currentRequest);
        Authentication authentication = tokenProvider.getAuthentication(token);

        return userRepository.findByAccount_Username(authentication.getName());
    }


    /**
     * Auction auto-closure function
     *
     * Helper functions that retrieves the non-completed auctions set and
     * for every item it checks if the current time is after the
     * 'endsAt' time meaning that the auction must be completed
     *
     */
//     void auctionClosure(){
//
//        List<Item> auctions = itemRepository.getAllOpenAuctions();
//        auctions.forEach(item -> { checkAuction(item); });
//    }

    boolean checkAuction(Item item){

        if(item.getEndsAt().getTime() < System.currentTimeMillis()){
            return true;
        } else {
            return false;
        }
//         if(item.getEndsAt().getTime() < System.currentTimeMillis()){
//      //  if((new Date()).compareTo(item.getEndsAt()) >= 0){
//
//            item.setAuctionCompleted(true);
//            itemRepository.save(item);
//
////            item.getSeller().getItems().add(item);
////            userRepository.save(item.getSeller());
//
//            notifySeller(item);
//            notifiyBuyer(item);
//        }
    }


    void notifySeller(Item item){

        Notification toSeller = new Notification();
        toSeller.setRecipient(item.getSeller());
        toSeller.setItemId(item.getId());
        toSeller.setMessage("Your auction with name \"" + item.getName() + "\" has been completed");
        notificationRepository.save(toSeller);
    }

    void notifiyBuyer(Item item){
         if(!item.getBids().isEmpty()) {
             Notification toBuyer = new Notification();
             Bid highestBid = Collections.max(item.getBids(), Bid.cmp);
             toBuyer.setRecipient(highestBid.getBidder());
             toBuyer.setItemId(item.getId());
             toBuyer.setMessage("Congratulations! You won the auction for \"" + item.getName() + "\"");
             notificationRepository.save(toBuyer);
         }
    }
}