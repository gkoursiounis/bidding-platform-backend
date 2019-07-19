package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.models.*;
import com.Auctions.backEnd.repositories.*;
import com.Auctions.backEnd.repositories.UserRepository;
import com.Auctions.backEnd.services.security.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.Auctions.backEnd.services.security.JWTFilter.resolveToken;

public abstract class BaseController {

    @Autowired
    private UserRepository userRepository;

    static final Set<String> contentTypes = new HashSet<>(Arrays.asList(
            "image/png",
            "image/jpeg",
            "image/gif"
    ));

//    void sendResetPasswordEmail(String email, String token) {
//        Thread mailSender = new Thread(){ 	// Creating an anonymous thread
//            public void run()
//            {
//                SimpleMailMessage mailMessage = new SimpleMailMessage();
//                mailMessage.setTo(email);
//                mailMessage.setSubject("Reset your Password !");
//                mailMessage.setFrom("illusion.services.usi@gmail.com");
//                mailMessage.setText("To reset your password, please click here : "
//                        +"http://localhost:3000/reset/password/?token="+token);
//                emailSenderService.sendEmail(mailMessage);
//            }
//        };
//        mailSender.start();
//    }

//    Set<FriendList> convertStringListToListsArray(String listID, User user) {
//        if(listID == null || listID.isEmpty())
//            // CASE THE THE STRING IS NULL OR EMPTY. @author wize
//            return new HashSet<>();
//
//        // setOfIds is an array which contains all the list ids
//        String[] setOfIds = listID.split(",");
//        // HashSetOfFriendL is the set of all the friendlists
//        HashSet<FriendList> HashSetOfFriendL = new HashSet<>();
//        for(String listId : setOfIds){
//            int aListId = Integer.parseInt(listId);
//            FriendList friendList = friendListRepository.findFriendListById(aListId);
//
//            if (friendList == null || !(friendListRepository.findAllByOwner(user).contains(friendList))){
//                throw new NumberFormatException();
//            }
//
//            HashSetOfFriendL.add(friendList);
//        }
//
//        return HashSetOfFriendL;
//    }
}