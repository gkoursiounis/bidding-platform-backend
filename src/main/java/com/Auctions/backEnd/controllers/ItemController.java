package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.models.*;
import com.Auctions.backEnd.repositories.*;
import com.Auctions.backEnd.responses.Message;
import com.Auctions.backEnd.services.File.DBFileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@CrossOrigin(origins = "https://localhost:3000")
@RequestMapping("/item")
public class ItemController extends BaseController{

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


    /**
     * A User can get an item/auction details using its itemId
     * If the itemId is invalid then we get an <HTTP>NOT FOUND</HTTP>
     *
     * @param itemId - Id of the item
     * @return the item details
     */
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


    /**
     * A User can get a list of All the open auctions
     * i.e. a list of items where the field auctionCompleted is False
     *
     * @return list of items
     */
    @GetMapping("/openAuctions")
    public ResponseEntity getAllOpenAuctions(){
        return ResponseEntity.ok(itemRepository.getAllOpenAuctions());
    }


    /**
     * A User can get a list of All the items/auctions existing in the database
     *
     * @return list of all items
     */
    @GetMapping("/allAuctions")
    public ResponseEntity getAllItems(){
        return ResponseEntity.ok(itemRepository.findAll());
    }


    /**
     * A User can get a list of names of every existing category
     *
     * @return list of all items
     */
    @GetMapping("/allCategories")
    public ResponseEntity getAllCategoriesNames(){
        return ResponseEntity.ok(itemCategoryRepository.findAll());
    }



    @GetMapping("/feed")
    public ResponseEntity getFeed() {

        PageRequest.of(0, 5);
        List<Item> feed = itemRepository.getAllOpenAuctions();

        if(feed.size() > 5){

            List<Item> returnedFeed = new ArrayList<>();
            for(int i = 0; i < 5; i++) {
                returnedFeed.add(feed.get(i));
            }

            return ResponseEntity.ok(returnedFeed);
        }

        return ResponseEntity.ok(feed);
    }

    @GetMapping("/older/{itemId}")
    public ResponseEntity getOlderAuctions(@PathVariable Long itemId){

        return itemRepository.findById(itemId).map((item) -> {

            List<Item> olderItems =  itemRepository.getOlderItems(item.getCreatedAt());

            if(olderItems.size() > 5){

                List<Item> returnedFeed = new ArrayList<>();
                for(int i = 0; i < 5; i++) {
                    returnedFeed.add(olderItems.get(i));
                }

                return ResponseEntity.ok(returnedFeed);
            }

            return ResponseEntity.ok(olderItems);

        }).orElseGet(()-> new ResponseEntity(new Message(
                "Error",
                "Item not found"
        ), HttpStatus.NOT_FOUND));
    }






    /**
     * A user can crate an item
     * By creating an item we consider an auction to have started
     *
     * The parameters are non-nullable (except for media) so the application
     * will automatically reject with an <HTTP>BAD REQUEST</HTTP> any request
     * with missing parameters. So the checks below (== null and .isEmpty) are
     * about available but empty(!) parameters
     *
     * buyPrice cannot be less than firstBid
     *
     * @param name - item's name
     * @param buyPrice - the price where a bidder can directly buy an item
     * @param media - optional picture
     * @param firstBid - the first bid
     * @param categoriesId - list of Integers as the Id's of the item's categories
     * @param longitude - location
     * @param latitude - location
     * @param locationTitle - location
     * @param endsAt - when the auction ends
     * @param description - item's description
     * @return the item details
     */
    @PostMapping
    public ResponseEntity createItem(@RequestParam String name,
                                     @Nullable @RequestParam Double buyPrice,
                                     @Nullable @RequestParam(name = "media") MultipartFile media,
                                     @RequestParam Double firstBid,
                                     @RequestParam Integer[] categoriesId,
                                     @RequestParam Double longitude,
                                     @RequestParam Double latitude,
                                     @RequestParam String locationTitle,
                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endsAt,
                                     @RequestParam String description) {

        User requestUser = requestUser();

        if (!requestUser.getAccount().isVerified()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "You can not make an auction if you are not verified"
            ));
        }

        if(name.isEmpty() || description.isEmpty() || firstBid == null || endsAt == null ||
                (buyPrice != null && Double.compare(buyPrice, firstBid) < 0)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "Invalid parameters"
            ));
        }

        Item item = new Item(new Date());
        item.setSeller(requestUser);
        item.setName(name);
        item.setFirstBid(firstBid);
        item.setCurrently(firstBid);
        item.setAuctionCompleted(false);
        item.setEndsAt(endsAt);
        item.setDescription(description);

        if(buyPrice != null){
            item.setBuyPrice(buyPrice);
        }


        if (categoriesId != null && categoriesId.length > 5) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "You cannot set more than 5 categories for an item"
            ));
        }
        else if(categoriesId != null && categoriesId.length == 0){
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
                category.getItems().add(item);
                item.getCategories().add(category);
                itemCategoryRepository.save(category);
            }
        }

        if(media != null){

            if (!BaseController.contentTypes.contains(media.getContentType())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                        "Error",
                        "Image type not supported"
                ));
            }

            if (media.getSize() > DBFile.MAXIMUM_IMAGE_SIZE) {

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


        if (longitude != null && latitude != null && locationTitle != null){

            Geolocation location = geolocationRepository.findLocationByLatitudeAndLongitude(latitude, longitude);
            if (location == null) {
                location = new Geolocation(longitude, latitude, locationTitle);

            }
            item.setLocation(location);
            location.getItems().add(item);
            geolocationRepository.save(location);
        }
        else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "Geospatial data cannot be empty"
            ));
        }

        itemRepository.save(item);

        requestUser.getItems().add(item);
        userRepository.save(requestUser);

        return ResponseEntity.ok(item);
    }


    /**
     * A user can modify the details of an item belonging to him
     * The user cannot modify an item if the first bid has been submitted
     *
     * @param itemId - the id of the item
     * @param name - optionally new name
     * @param buyPrice - optionally new buy price
     * @param firstBid - optionally new first bid
     * @param categoriesId - optionally new categories
     * @param endsAt - optionally new auction ending date
     * @param description - optionally new description
     * @return the modified item
     */
    @PatchMapping("/{itemId}")
    public ResponseEntity modifyItem(@PathVariable (value = "itemId") long itemId,
                                     @Nullable @RequestParam String name,
                                     @Nullable @RequestParam Double buyPrice,
                                     @Nullable @RequestParam Double firstBid,
                                     @Nullable @RequestParam Integer[] categoriesId,
                                     @Nullable @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endsAt,
                                     @Nullable @RequestParam String description) {

        User requester = requestUser();

        Item item = itemRepository.findItemById(itemId);
        if(item == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
                    "Error",
                    "Item not found. Invalid item Id"
            ));
        }

        if(!requester.getItems().contains(item)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message(
                    "Error",
                    "You cannot modify an auction that does not belong to you"
            ));
        }

        if(!item.getBids().isEmpty() || checkAuction(item)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Message(
                    "Error",
                    "You cannot modify the auction after the first bid or if it is completed"
            ));
        }

        if(name != null){ item.setName(name); }
        if(endsAt != null){ item.setEndsAt(endsAt); }
        if(description != null){ item.setDescription(description); }

        if(buyPrice != null){

            if((firstBid != null && java.lang.Double.compare(buyPrice, firstBid) < 0) ||
                    (firstBid == null && java.lang.Double.compare(buyPrice, item.getFirstBid()) < 0)){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                        "Error",
                        "Buy price cannot be less than the first bid"
                ));
            }
            item.setBuyPrice(buyPrice);
        }
        if(firstBid != null){

            if((buyPrice != null && java.lang.Double.compare(buyPrice, firstBid) < 0) ||
                    (buyPrice == null && java.lang.Double.compare(item.getBuyPrice(), firstBid) < 0)){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                        "Error",
                        "Buy price cannot be less than the first bid"
                ));
            }
            item.setFirstBid(firstBid);
        }


        if(categoriesId != null){

            if (categoriesId.length > 5) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                        "Error",
                        "You cannot set more than 5 categories for an item"
                ));
            }

            item.getCategories().clear();
            for(Integer id: categoriesId) {
                ItemCategory category = itemCategoryRepository.findItemCategoryById(Long.valueOf(id));

                if(category == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
                            "Error",
                            "Category not found. Invalid category Id"
                    ));
                }
                item.getCategories().add(category);
                category.getItems().add(item);
                itemCategoryRepository.save(category);
            }
        }

        itemRepository.save(item);

//        requester.getItems().add(item);
//        userRepository.save(requester);

        return ResponseEntity.ok(item);
    }


    /**
     * A User can delete one of his items/auctions
     * provided that the first bid has not been made
     *
     * @param itemId - Id of the item
     * @return an <HTTP>OK</HTTP>
     */
    @DeleteMapping("/{itemId}")
    public ResponseEntity deleteItem(@PathVariable (value = "itemId") long itemId){

        User requester = requestUser();

        Item item = itemRepository.findItemById(itemId);
        if(item == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
                    "Error",
                    "Item not found. Invalid item Id"
            ));
        }

        if(!requester.getItems().contains(item)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message(
                    "Error",
                    "You cannot delete an auction that does not belong to you"
            ));
        }

        if(!item.getBids().isEmpty() || checkAuction(item)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Message(
                    "Error",
                    "You cannot delete the auction after the first bid or if it is completed"
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
}