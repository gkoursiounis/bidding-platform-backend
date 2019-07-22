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
public class AdminController {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final ItemCategoryRepository itemCategoryRepository;
    private final BaseController baseController;

    @Autowired
    public AdminController(UserRepository userRepository, AccountRepository accountRepository,
                          ItemCategoryRepository itemCategoryRepository, BaseController baseController){
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.itemCategoryRepository = itemCategoryRepository;
        this.baseController = baseController;
    }


    /**
     * The Administrator can get a list of all unverified users
     * whose approval request is pending
     *
     * @return a list of unverified users
     */
    @GetMapping("/pendingRegisters")
    public ResponseEntity getPendingRegisters(){

        User requester = baseController.requestUser();
        if(!requester.isAdmin()){

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message(
                    "Error",
                    "You need to be an admin to perform this action"
            ));
        }

        return ResponseEntity.ok(userRepository.getPendingUsers());
    }


    /**
     * The Administrator can get a list of all the existing users
     * including administrators but excluding himself
     *
     * @return a list of users
     */
    @GetMapping("/allUsers")
    public ResponseEntity getAllUsers(){

        User requester = baseController.requestUser();
        if(!requester.isAdmin()){

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message(
                    "Error",
                    "You need to be an admin to perform this action"
            ));
        }

        List<User> users = userRepository.getAllUsers();
        users.removeIf(user -> user.equals(requester));

        return ResponseEntity.ok(users);
    }


    /**
     * The administrator can verify all the unverified users in once
     *
     * @return <HTTP>OK</HTTP>
     */
    @PatchMapping("/verifyAll")
    public ResponseEntity verifyAllUsers(){

        User requester = baseController.requestUser();
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


    /**
     * The administrator can verify a single user using his userId
     *
     * @param userId - the Id of the user whom the admin wishes to verify
     * @return <HTTP>OK</HTTP>
     */
    @PatchMapping("/verifyUser/{userId}")
    public ResponseEntity verifyUser(@PathVariable (value = "userId") long userId){

        User requester = baseController.requestUser();
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
                    "User not found"
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


    /**
     * The administrator can create an new auction/item category (ItemCategory)
     * If the category name exists then we get an <HTTP>BAD REQUEST</HTTP>
     *
     * @param name - the name of the new category
     * @return a new item category
     */
    @PostMapping("/newCategory")
    public ResponseEntity createItemCategory(@RequestParam String name){

        User requester = baseController.requestUser();

        if(!requester.isAdmin()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message(
                    "Error",
                    "You need to be an admin to perform this action"
            ));
        }

        if(name == null || name.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "Invalid category name"
            ));
        }

        ItemCategory search = itemCategoryRepository.findItemCategoryByName(name);
        if(search != null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "Category name already exists"
            ));
        }

        ItemCategory category = new ItemCategory();
        category.setName(name);
        itemCategoryRepository.save(category);

        return ResponseEntity.ok(category);
    }


    /**
     * The administrator can delete a user using his userId
     * If the user is an administrator then we get an <HTTP>BAD REQUEST</HTTP>
     *
     * @param userId - the Id of the user whom the admin wishes to delete
     * @return <HTTP>OK</HTTP>
     */
    @DeleteMapping("/deleteUser/{userId}")
    public ResponseEntity deleteUser(@PathVariable (value = "userId") long userId){

        User requester = baseController.requestUser();
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
                    "User not found"
            ));
        }

        if (user.isAdmin()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "You cannot delete an administrator"
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