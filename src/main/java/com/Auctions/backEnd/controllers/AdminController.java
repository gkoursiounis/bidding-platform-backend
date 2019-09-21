package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.models.*;
import com.Auctions.backEnd.repositories.*;
import com.Auctions.backEnd.responses.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/admin")
public class AdminController extends BaseController{

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final ItemCategoryRepository itemCategoryRepository;
    private final ItemRepository itemRepository;

    @Autowired
    public AdminController(UserRepository userRepository, AccountRepository accountRepository,
                          ItemCategoryRepository itemCategoryRepository, ItemRepository itemRepository){
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.itemCategoryRepository = itemCategoryRepository;
        this.itemRepository = itemRepository;
    }


    /**
     * The Administrator can get a list of all unverified users
     * whose approval request is pending
     *
     * We use Database Pagination which means that the front-end
     * part needs to send:
     *  - a page number
     *  - a page size
     *  - any order/sorting preferences
     *
     * @return a list of unverified users
     */
    @GetMapping("/pendingRegisters")
    public ResponseEntity getPendingRegisters(Pageable pageable){

        User requester = requestUser();
        if(!requester.isAdmin()){

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message(
                    "Error",
                    "You need to be an admin to perform this action"
            ));
        }

        return ResponseEntity.ok(userRepository.getPendingUsers(pageable));
    }


    /**
     * The Administrator can get a list of all the existing users
     * including other administrators and himself
     *
     * We use Database Pagination which means that the front-end
     * part needs to send:
     *  - a page number
     *  - a page size
     *  - any order/sorting preferences
     *
     * @return a list of users
     */
    @GetMapping("/allUsers")
    public ResponseEntity getAllUsers(Pageable pageable) {

        User requester = requestUser();
        if(!requester.isAdmin()){

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message(
                    "Error",
                    "You need to be an admin to perform this action"
            ));
        }

        return ResponseEntity.ok(userRepository.getAllUsers(pageable));
    }


    /**
     * The administrator can verify all the unverified users in once
     *
     * @return <HTTP>OK</HTTP>
     */
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
            notifyForVerification(user);
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
        notifyForVerification(user);

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
    @PostMapping("/newCategory/{categoryId}")
    public ResponseEntity createItemCategory(@PathVariable (value = "categoryId") long categoryId,
                                             @RequestParam String name){

        User requester = requestUser();

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

        ItemCategory parent = itemCategoryRepository.findItemCategoryById(categoryId);
        if(parent == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "Parent category does not exist"
            ));
        }

        ItemCategory category = new ItemCategory();
        category.setName(name);
        category.setParent(parent);
        itemCategoryRepository.save(category);

        parent.getSubcategories().add(category);
        itemCategoryRepository.save(parent);

        return ResponseEntity.ok(category);
    }


    /**
     * The administrator can get a list of All the existing items/auctions
     * The front-end part is responsible for creating the file and converting
     * to XML if necessary
     *
     * @return list of all items
     */
    @GetMapping("/allAuctions")
    public ResponseEntity getAllItems(){

        User requester = requestUser();
        if(!requester.isAdmin()){

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message(
                    "Error",
                    "You need to be an admin to perform this action"
            ));
        }

        return ResponseEntity.ok(itemRepository.findAll());
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
                    "User not found"
            ));
        }

        if (user.isVerified()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "You can not delete a verified user"
            ));
        }


        accountRepository.deleteById(user.getAccount().getId());
        userRepository.deleteById(user.getId());


        return ResponseEntity.ok(new Message(
                "Ok",
                "User has been deleted"
        ));
    }

}