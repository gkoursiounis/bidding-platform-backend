package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.models.Item;
import com.Auctions.backEnd.models.Notification;
import com.Auctions.backEnd.models.User;
import com.Auctions.backEnd.repositories.ItemRepository;
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

@RestController
public class BaseController {

    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Autowired
    public BaseController(TokenProvider tokenProvider, UserRepository userRepository,
                          ItemRepository itemRepository) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }


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
    public User requestUser(){

        final HttpServletRequest currentRequest =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String token = resolveToken(currentRequest);
        Authentication authentication = tokenProvider.getAuthentication(token);

        return userRepository.findByAccount_Username(authentication.getName());
    }


    //TODO complete
    /**
     * Auction auto-closure function
     *
     * Helper functions that retrieves the non-completed auctions set and
     * for every item it checks if the current time is after the
     * 'endsAt' time meaning that the auction must be completed
     *
     */
    public void auctionClosure(){

        Date now = new Date();

        List<Item> auctions = itemRepository.getAllOpenAuctions();
        auctions.forEach(item -> {
            if(now.compareTo(item.getEndsAt()) >= 0){

                item.setAuctionCompleted(true);
                itemRepository.save(item);

                item.getSeller().getItems().add(item);
                userRepository.save(item.getSeller());

                Notification notification = new Notification();
               // notification.setRecipient(item.get);
            }
        });
    }
}