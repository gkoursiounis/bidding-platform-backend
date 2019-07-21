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
    private AccountController accountController;

    @Autowired
    public ItemController(UserRepository userRepository,ItemRepository itemRepository,
                          ItemCategoryRepository itemCategoryRepository, DBFileRepository dbFileRepository,
                          DBFileStorageService dBFileStorageService, GeolocationRepository geolocationRepository,
                          AccountController accountController){
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.itemCategoryRepository = itemCategoryRepository;
        this.dbFileRepository = dbFileRepository;
        this.dBFileStorageService = dBFileStorageService;
        this.geolocationRepository = geolocationRepository;
        this.accountController = accountController;
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


    @GetMapping("/completedAuctions")
    public ResponseEntity getCompletedAuctions(){
        return ResponseEntity.ok(itemRepository.getAllcompletedAuctions());
    }


    @GetMapping("/openAuctions")
    public ResponseEntity getOpenAuctions(){
        return ResponseEntity.ok(itemRepository.getAllopenAuctions());
    }


    @GetMapping("/allAuctions")
    public ResponseEntity getAllItems(){
        return ResponseEntity.ok(itemRepository.getAllAuctions());
    }


    /**
     * User can search for items/auctions based on a category
     *
     * @return a list of items
     */
    @GetMapping("/search")
    public ResponseEntity searchBar(@RequestParam String keyword){

        List<Item> res = new ArrayList<>();

        if ("".equals(keyword)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "Invalid keyword"
            ));
        }

        keyword = keyword.toLowerCase();

        res = itemRepository.searchItems(keyword);
        Collections.sort(res);

        return ResponseEntity.ok(res);
    }


    //TODO complete
    @GetMapping("/search/filters")
    public ResponseEntity filterSearch(@Nullable @RequestParam List<String> categoryNames,
                                         @Nullable @RequestParam Double lowerPrice,
                                         @Nullable @RequestParam Double higherPrice,
                                         @Nullable @RequestParam String freeText){

        if(categoryNames == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "Names of the categories are missing"
            ));
        }

        return ResponseEntity.ok(itemRepository.searchByPrice(lowerPrice, higherPrice));
    }


    @PostMapping
    public ResponseEntity createItem(@RequestParam String name,
                                     @RequestParam Double buyPrice,
                                     @Nullable @RequestParam(name = "media") MultipartFile media,
                                     @RequestParam Double firstBid,
                                     @Nullable @RequestParam Integer[] categoriesId,    //nullable
                                     @Nullable @RequestParam Double longitude,
                                     @Nullable @RequestParam Double latitude,
                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endsAt,
                                     @Nullable @RequestParam String description) {

        User requestUser = accountController.requestUser();

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

        Item item = new Item(new Date());
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


        if (longitude != null && latitude != null){

            Geolocation location = geolocationRepository.findLocationByLatitudeAndLongitude(latitude, longitude);
            if (location == null) {
                location = new Geolocation(longitude, latitude);

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

        item.getSeller().getItems().remove(item);
        userRepository.save(item.getSeller());
        itemRepository.delete(item);

        return ResponseEntity.status(HttpStatus.OK).body(new Message(
                "Ok",
                "Auction has been deleted"
        ));
    }

    //TODO test delete
    @PostMapping("/test")
    public ResponseEntity upPic(@RequestParam(name = "media") MultipartFile media){
        DBFile dbFile = dBFileStorageService.storeFile(media);
        dbFile.setDownloadLink("/downloadFile/" + dbFile.getId() + "." + dbFile.getFileType().split("/")[1]);
        dbFile = dbFileRepository.save(dbFile);
        System.out.println(dbFile.getId() + "\n" + dbFile.getFileName() + "\n" + dbFile.getDownloadLink());
        return ResponseEntity.ok(dbFile);
    }
}