package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.models.*;
import com.Auctions.backEnd.repositories.*;
import com.Auctions.backEnd.responses.Message;
import com.Auctions.backEnd.services.File.DBFileStorageService;
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
    private final DBFileRepository dbFileRepository;
    private final DBFileStorageService dBFileStorageService;
    private final GeolocationRepository geolocationRepository;

    @Autowired
    public ItemController(UserRepository userRepository,ItemRepository itemRepository,
                          ItemCategoryRepository itemCategoryRepository, DBFileRepository dbFileRepository,
                          DBFileStorageService dBFileStorageService, GeolocationRepository geolocationRepository){
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.itemCategoryRepository = itemCategoryRepository;
        this.dbFileRepository = dbFileRepository;
        this.dBFileStorageService = dBFileStorageService;
        this.geolocationRepository = geolocationRepository;
    }


    @GetMapping("/{itemId}")
    public ResponseEntity getItem(@PathVariable (value = "itemId") long itemId){

        Item item = itemRepository.findItemById(itemId);
        if(item == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
                    "Error",
                    "Item not found. Invalid item Id"
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
                                     @Nullable @RequestParam String apiIdentifier,
                                     @Nullable @RequestParam Double longitude,
                                     @Nullable @RequestParam Double latitude,
                                     @Nullable @RequestParam String locationType,
                                     @Nullable @RequestParam String locationTitle,
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
        item.setCurrently(firstBid);
        item.setEndsAt(endsAt);

        if (description != null) {
            item.setDescription(description);
        }


        if (categoriesId.length > 5) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "You cannot set more than 5 categories for an item"
            ));
        }
        else if(categoriesId.length == 0){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "You need to set at least one category for an item"
            ));
        }
        else{

            for(Integer id: categoriesId){
                ItemCategory category = itemCategoryRepository.findItemCategoryById(Long.valueOf(id));

                if(category == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
                            "Error",
                            "Category not found. Invalid category Id"
                    ));
                }
                item.getCategories().add(category);
            }
        }

        if(media != null){

            if (!contentTypes.contains(media.getContentType())){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                        "Error",
                        "Image type not supported"
                ));
            }

            if (media.getSize() > DBFile.MAXIMUM_IMAGE_SIZE && (
                    "image/png".equals(media.getContentType())  || "image/jpeg".equals(media.getContentType()) ||
                            "image/gif".equals(media.getContentType()))) {

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                        "Error",
                        "Image over limits"
                ));
            }

            DBFile dbFile = dBFileStorageService.storeFile(media);
            dbFile.setDownloadLink("/downloadFile/" + dbFile.getId() + "." + dbFile.getFileType().split("/")[1]);
            dbFile = dbFileRepository.save(dbFile);
            item.getMedia().add(dbFile);
        }


        if (apiIdentifier != null && longitude != null && latitude != null &&
                locationType != null && locationTitle != null){

            Geolocation location = geolocationRepository.findByLocationTitle(locationTitle);
            if (location == null) {
                location = new Geolocation(apiIdentifier, longitude, latitude, locationType, locationTitle);

            }
            item.setLocation(geolocationRepository.save(geolocationRepository.save(location)));
        }

        if (item.getLocation() != null) {
            item.getLocation().getItems().add(item);
            geolocationRepository.save(item.getLocation());
        }

        itemRepository.save(item);

        requestUser.getItems().add(item);
        userRepository.save(requestUser);

        return ResponseEntity.ok(item);
    }


    @PatchMapping("/{itemId}")
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
                    "Error",
                    "Item not found. Invalid item Id"
            ));
        }

        if(!item.getBids().isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "You cannot modify the item's details after the first bid"
            ));
        }

        if(java.lang.Double.compare(buyPrice, firstBid) < 0){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "Buy price cannot be less than the first bid"
            ));
        }

        if(name != null){ item.setName(name); }
        if(buyPrice != null){ item.setBuyPrice(buyPrice); }
        if(firstBid != null){ item.setFirstBid(firstBid); }
        if(endsAt != null){ item.setEndsAt(endsAt); }
        if (description != null) { item.setDescription(description); }


        if (categoriesId.length > 5) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "You cannot set more than 5 categories for an item"
            ));
        }
        else if(categoriesId.length == 0){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "You need to set at least one category for an item"
            ));
        }
        else{

            for(Integer id: categoriesId){
                ItemCategory category = itemCategoryRepository.findItemCategoryById(Long.valueOf(id));

                if(category == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
                            "Error",
                            "Category not found. Invalid category Id"
                    ));
                }
                item.getCategories().add(category);
            }
        }

        itemRepository.save(item);
        return ResponseEntity.ok(item);
    }


    @DeleteMapping("/{itemId}")
    public ResponseEntity deleteItem(@PathVariable (value = "itemId") long itemId){

        Item item = itemRepository.findItemById(itemId);
        if(item == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
                    "Error",
                    "Item not found. Invalid item Id"
            ));
        }

        if(!item.getBids().isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "You cannot delete the auction after the first bid"
            ));
        }

        itemRepository.delete(item);

        return ResponseEntity.status(HttpStatus.OK).body(new Message(
                "Ok",
                "Auction has been deleted"
        ));
    }
}