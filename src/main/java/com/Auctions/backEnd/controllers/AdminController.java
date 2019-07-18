package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.models.*;
import com.Auctions.backEnd.repositories.*;
import com.Auctions.backEnd.responses.BidRes;
import com.Auctions.backEnd.responses.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/admin")
public class AdminController extends BaseController {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final BidRepository bidRepository;

    @Autowired
    public AdminController(UserRepository userRepository, AccountRepository accountRepository,
                          BidRepository bidRepository){
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.bidRepository = bidRepository;
    }

    @GetMapping("/pendingRegisters")
    public ResponseEntity getPendingRegisters(){

        User requester = requestUser();
        if(!requester.isAdmin()){

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "You need to be an admin to perform this action"
            ));
        }

        return ResponseEntity.ok(userRepository.getPendingUsers());
    }


    @PatchMapping("/verifyUser/{userId}")
    public ResponseEntity verifyUser(@PathVariable (value = "userId") long userId){

        User requester = requestUser();
        if(!requester.isAdmin()){

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "You need to be an admin to perform this action"
            ));
        }

        User user = userRepository.findUserById(userId);

        if (user == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
                    "Error",
                    "User not found!"
            ));
        }

        if (user.isVerified()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "User is already verified"
            ));
        }

        user.getAccount().setVerified(true);
        accountRepository.save(user.getAccount());
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.OK).body(new Message(
                "Ok",
                "User is now verified"
        ));
    }

}