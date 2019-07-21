package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.models.User;
import com.Auctions.backEnd.repositories.UserRepository;
import com.Auctions.backEnd.services.security.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.Auctions.backEnd.services.security.JWTFilter.resolveToken;

@RestController
public class BaseController {

    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;

    @Autowired
    public BaseController(TokenProvider tokenProvider, UserRepository userRepository) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
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
}