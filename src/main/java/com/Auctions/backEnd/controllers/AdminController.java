package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.models.*;
import com.Auctions.backEnd.repositories.*;
import com.Auctions.backEnd.responses.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController extends BaseController {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final ItemCategoryRepository itemCategoryRepository;

    @Autowired
    public AdminController(UserRepository userRepository, AccountRepository accountRepository,
                          ItemCategoryRepository itemCategoryRepository){
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.itemCategoryRepository = itemCategoryRepository;
    }


    /**
     * The application administrator can get a list of all unverified users
     * whose approval request is pending
     *
     * @return a list of unverified users
     */
    @GetMapping("/pendingRegisters")
    public ResponseEntity getPendingRegisters(){

        User requester = requestUser();
        if(!requester.isAdmin()){

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message(
                    "Error",
                    "You need to be an admin to perform this action"
            ));
        }

        return ResponseEntity.ok(userRepository.getPendingUsers());
    }


    /**
     * The application administrator can get a list of all the existing users
     * excluding everyone who is also an administrator
     *
     * @return a list of users
     */
    @GetMapping("/allUsers")
    public ResponseEntity getAllUsers(){

        User requester = requestUser();
        if(!requester.isAdmin()){

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message(
                    "Error",
                    "You need to be an admin to perform this action"
            ));
        }

        List<User> users = userRepository.getAllUsers();
        users.removeIf(user -> user.isAdmin());

        return ResponseEntity.ok(users);
    }


    @PatchMapping("/verifyAll")
    public ResponseEntity verifyAllUsers(){

        User requester = requestUser();
        if(!requester.isAdmin()){

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message(
                    "Error",
                    "You need to be an admin to perform this action"
            ));
        }

        List<User> pending = userRepository.getPendingUsers();
        pending.forEach(user -> {
            user.getAccount().setVerified(true);
            accountRepository.save(user.getAccount());
            userRepository.save(user);
        });

        return ResponseEntity.ok(new Message(
                "Ok",
                "All pending users have been verified"
        ));
    }


    @PatchMapping("/verifyUser/{userId}")
    public ResponseEntity verifyUser(@PathVariable (value = "userId") long userId){

        User requester = requestUser();
        if(!requester.isAdmin()){

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message(
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


    @PostMapping("/newCategory")
    public ResponseEntity createItemCategory(@RequestParam String name){

        User requester = requestUser();
        if(!requester.isAdmin()){

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message(
                    "Error",
                    "You need to be an admin to perform this action"
            ));
        }

        if(name == null){

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "Category name is missing"
            ));
        }

        ItemCategory category = new ItemCategory();
        category.setName(name);
        itemCategoryRepository.save(category);

        return ResponseEntity.ok(category);
    }


    @DeleteMapping("/deleteUser/{userId}")
    public ResponseEntity deleteUser(@PathVariable (value = "userId") long userId){

        User requester = requestUser();
        if(!requester.isAdmin()){

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message(
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

        accountRepository.delete(user.getAccount());
        userRepository.delete(user);

        return ResponseEntity.ok(new Message(
                "Ok",
                "User has been deleted"
        ));
    }

}