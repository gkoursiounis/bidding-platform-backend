package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.models.Item;
import com.Auctions.backEnd.models.Notification;
import com.Auctions.backEnd.models.User;
import com.Auctions.backEnd.repositories.ItemRepository;
import com.Auctions.backEnd.repositories.NotificationRepository;
import com.Auctions.backEnd.repositories.UserRepository;
import com.Auctions.backEnd.services.Security.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
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
     * @return the user details
     */
     User requestUser(){

        final HttpServletRequest currentRequest =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String token = resolveToken(currentRequest);
        Authentication authentication = tokenProvider.getAuthentication(token);

        return userRepository.findByAccount_Username(authentication.getName());
    }


    /**
     * Helper functions for early warning auction closure
     *
     * A thread (see BackEndApplication.java) is created every
     * 5 seconds in order to inspect and terminate the auctions
     * whose 'endsAt' time has been reached.
     * But since there is a gap of maximum 5 seconds from an
     * 'endsAt' time to the check, we need to make sure that
     * a user won't be able to make a bid or make any changes
     * to the auction within this time.
     *
     * @param item - the auction
     * @return a bool {true if auction is over, false if auction is open}
     */
    boolean checkAuction(Item item){

        if(item.getEndsAt().getTime() < System.currentTimeMillis()){
            return true;
        } else {
            return false;
        }
    }


    /**
     * Helper function to notify seller in case some user
     * wins an auction using the 'buyPrice' option. In that
     * case, the auction is terminated by the route not the
     * auto-closure thread
     *
     * @param item - the auction
     */
    void notifySeller(Item item){

        Notification toSeller = new Notification();
        toSeller.setRecipient(item.getSeller());
        toSeller.setItemId(item.getId());
        toSeller.setMessage("Your auction with name " + item.getName() + " has been completed");
        notificationRepository.save(toSeller);

        item.getSeller().getNotifications().add(toSeller);
        userRepository.save(item.getSeller());
    }
}