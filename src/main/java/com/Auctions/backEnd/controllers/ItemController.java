package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.models.*;
import com.Auctions.backEnd.repositories.*;
import com.Auctions.backEnd.responses.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/item")
public class ItemController extends BaseController {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemCategoryRepository itemCategoryRepository;

    @Autowired
    public ItemController(UserRepository userRepository,ItemRepository itemRepository,
                          ItemCategoryRepository itemCategoryRepository){
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.itemCategoryRepository = itemCategoryRepository;
    }


    @GetMapping("/item/{itemId}")
    public ResponseEntity getItem(@PathVariable (value = "itemId") long itemId){

        Item item = itemRepository.findItemById(itemId);
        if(item == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "Invalid itemId"
            ));
        }

        return ResponseEntity.ok(item);
    }


    @PostMapping
    public ResponseEntity createItem(@RequestParam String name,
                                     @RequestParam Double buyPrice,
                                     @Nullable @RequestParam(name = "media") MultipartFile media,
                                     @RequestParam Double firstBid,
                                     @Nullable @RequestParam Integer[] categoriesId,
                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endsAt,
                                     @Nullable @RequestParam String description) {

        User requestUser = requestUser();

        if (!requestUser.getAccount().isVerified()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "You can not make an auction if you are not verified"
            ));
        }

        if (requestUser.getAccount().isVisitor()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "You can not make an auction if you are a visitor"
            ));
        }

        Item item = new Item();
        item.setSeller(requestUser);
        item.setName(name);
        item.setBuyPrice(buyPrice);
        item.setFirstBid(firstBid);
        item.setEndsAt(endsAt);

        if (description != null) {
            item.setDescription(description);
        }


        if(categoriesId != null){
            for(Integer id: categoriesId){
                ItemCategory category = itemCategoryRepository.findItemCategoryById(Long.valueOf(id));

                if(category == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                            "Error",
                            "Invalid category id"
                    ));
                }

                item.getCategories().add(category);
            }
        }

        itemRepository.save(item);
        return ResponseEntity.ok(item);
    }


    @PatchMapping("/item/{itemId}")
    public ResponseEntity modifyItem(@PathVariable (value = "itemId") long itemId,
                                     @Nullable @RequestParam String name,
                                     @Nullable @RequestParam Double buyPrice,
                                     @Nullable @RequestParam(name = "media") MultipartFile media,
                                     @Nullable @RequestParam Double firstBid,
                                     @Nullable @RequestParam Integer[] categoriesId,
                                     @Nullable @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endsAt,
                                     @Nullable @RequestParam String description) {

        Item item = itemRepository.findItemById(itemId);
        if(item == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "Invalid itemId"
            ));
        }

        if(item.getBids() != null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "You cannot modify the item's details after the first bid"
            ));
        }

        if(name != null){ item.setName(name); }
        if(buyPrice != null){ item.setBuyPrice(buyPrice); }
        if(firstBid != null){ item.setFirstBid(firstBid); }
        if(endsAt != null){ item.setEndsAt(endsAt); }
        if (description != null) { item.setDescription(description); }

        if(categoriesId != null){

            item.getCategories().clear();

            for(Integer id: categoriesId){
                ItemCategory category = itemCategoryRepository.findItemCategoryById(Long.valueOf(id));

                if(category == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                            "Error",
                            "Invalid category id"
                    ));
                }

                item.getCategories().add(category);
            }
        }

        itemRepository.save(item);
        return ResponseEntity.ok(item);
    }


    @DeleteMapping("/item/{itemId}")
    public ResponseEntity deleteItem(@PathVariable (value = "itemId") long itemId){

        Item item = itemRepository.findItemById(itemId);
        if(item == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "Invalid itemId"
            ));
        }

        itemRepository.delete(item);

        return ResponseEntity.status(HttpStatus.OK).body(new Message(
                "Ok",
                "Item has been deleted"
        ));
    }
}