package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.models.*;
import com.Auctions.backEnd.repositories.*;
import com.Auctions.backEnd.responses.Message;
import com.Auctions.backEnd.services.File.DBFileStorageService;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@CrossOrigin(origins = "*")
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

        User requester = requestUser();

        Item item = itemRepository.findItemById(itemId);
        if(item == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
                    "Error",
                    "Item not found. Invalid item Id"
            ));
        }

        if(!requester.getItemSeen().contains(item)){
            requester.getItemSeen().add(item);
            userRepository.save(requester);
        }

        return ResponseEntity.ok(item);
    }


    /**
     * A User can get an item/auction details using its itemId
     * If the itemId is invalid then we get an <HTTP>NOT FOUND</HTTP>
     *
     * @param itemId - Id of the item
     * @return the item details
     */
    @GetMapping("/{itemId}/visitor")
    public ResponseEntity getItemAsVisitor(@PathVariable (value = "itemId") long itemId){

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
    public ResponseEntity getAllOpenAuctions(Pageable pageable){
        return ResponseEntity.ok(itemRepository.getAllOpenAuctions(pageable));
    }


    //TODO remove
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
     * A User can get a list of all the categories
     * We have a predefined category call 'All categories'
     * which acts like a root at the category tree. Every new
     * category is included in the subcategories list of
     * 'All categories' or one of its children
     *
     * @return list of all items
     */
    @GetMapping("/allCategories")
    public ResponseEntity getAllCategoriesNames(){

        ItemCategory root = itemCategoryRepository.findItemCategoryByName("All categories");
        return ResponseEntity.ok(root);
    }



//    @GetMapping("/feed")
//    public ResponseEntity getFeed() {
//
//        PageRequest.of(0, 5);
//        List<Item> feed = itemRepository.getAll();
//
//        if(feed.size() > 5){
//
//            List<Item> returnedFeed = new ArrayList<>();
//            for(int i = 0; i < 5; i++) {
//                returnedFeed.add(feed.get(i));
//            }
//
//            return ResponseEntity.ok(returnedFeed);
//        }
//
//        return ResponseEntity.ok(feed);
//    }
//
//    @GetMapping("/older/{itemId}")
//    public ResponseEntity getOlderAuctions(@PathVariable Long itemId){
//
//        return itemRepository.findById(itemId).map((item) -> {
//
//            List<Item> olderItems =  itemRepository.getOlderItems(item.getCreatedAt());
//
//            if(olderItems.size() > 5){
//
//                List<Item> returnedFeed = new ArrayList<>();
//                for(int i = 0; i < 5; i++) {
//                    returnedFeed.add(olderItems.get(i));
//                }
//
//                return ResponseEntity.ok(returnedFeed);
//            }
//
//            return ResponseEntity.ok(olderItems);
//
//        }).orElseGet(()-> new ResponseEntity(new Message(
//                "Error",
//                "Item not found"
//        ), HttpStatus.NOT_FOUND));
//    }






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
     * @param categoryId - id of the bottom-most category
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
                                     @Nullable @RequestParam(name = "media") List<MultipartFile> media,
                                     @RequestParam Double firstBid,
                                     @RequestParam Long categoryId,
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
                    "Missing parameters or buy Price is smaller than first Bid"
            ));
        }

//        if(endsAt.compareTo(new Date()) < 0){
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
//                    "Error",
//                    "Auction ending time cannot be before the current time"
//            ));
//        }

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


        if (categoryId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "You have to set a category"
            ));
        }
        else{

            ItemCategory category = itemCategoryRepository.findItemCategoryById(Long.valueOf(categoryId));
            if(category == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
                        "Error",
                        "Category not found"
                ));
            }

            ItemCategory cat = category;
            do{
                cat.getItems().add(item);
                itemCategoryRepository.save(cat);
                item.getCategories().add(cat);
                cat = cat.getParent();
            }while(cat != null && !cat.getName().equals("All categories"));
           Collections.reverse(item.getCategories());
        }

        if(media != null){

            for(MultipartFile picture : media){

                if (!BaseController.contentTypes.contains(picture.getContentType())) {
                     return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                            "Error",
                            "Image type not supported"
                    ));
                }

                if (picture.getSize() > DBFile.MAXIMUM_IMAGE_SIZE) {

                    ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                            "Error",
                            "Image over limits"
                    ));
                }

                DBFile dbFile = dBFileStorageService.storeFile(picture);
                dbFile.setDownloadLink("/downloadFile/" + dbFile.getId() + "." + dbFile.getFileType().split("/")[1]);
                dbFile = dbFileRepository.save(dbFile);
                item.getMedia().add(dbFile);
            }
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
     * @param categoryId - optionally new categories
     * @param endsAt - optionally new auction ending date
     * @param description - optionally new description
     * @return the modified item
     */
    @PatchMapping("/{itemId}")
    public ResponseEntity modifyItem(@PathVariable (value = "itemId") long itemId,
                                     @Nullable @RequestParam String name,
                                     @Nullable @RequestParam(name = "media") List<MultipartFile> media,
                                     @Nullable @RequestParam Double buyPrice,
                                     @Nullable @RequestParam Double firstBid,
                                     @Nullable @RequestParam Long categoryId,
                                     @Nullable @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endsAt,
                                     @Nullable @RequestParam String description) {

        User requester = requestUser();

        Item item = itemRepository.findItemById(itemId);
        if(item == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
                    "Error",
                    "Item not found"
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
        }
        item.setBuyPrice(buyPrice);

        if(firstBid != null){

            if((buyPrice != null && java.lang.Double.compare(buyPrice, firstBid) < 0)){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                        "Error",
                        "Buy price cannot be less than the first bid"
                ));
            }
            item.setFirstBid(firstBid);
        }


        if(categoryId != null){

            ItemCategory category = itemCategoryRepository.findItemCategoryById(Long.valueOf(categoryId));
            if(category == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
                        "Error",
                        "Category not found"
                ));
            }

            item.getCategories().clear();
            ItemCategory cat = category;
            do{
                item.getCategories().add(cat);
                cat.getItems().add(item);
                itemCategoryRepository.save(cat);
                cat = cat.getParent();
            }while(cat != null && !cat.getName().equals("All categories"));
        }

        if(media != null){

            for(MultipartFile picture : media){

                if (!BaseController.contentTypes.contains(picture.getContentType())) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                            "Error",
                            "Image type not supported"
                    ));
                }

                if (picture.getSize() > DBFile.MAXIMUM_IMAGE_SIZE) {

                    ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                            "Error",
                            "Image over limits"
                    ));
                }

                DBFile dbFile = dBFileStorageService.storeFile(picture);
                dbFile.setDownloadLink("/downloadFile/" + dbFile.getId() + "." + dbFile.getFileType().split("/")[1]);
                dbFile = dbFileRepository.save(dbFile);
                item.getMedia().add(dbFile);
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
                    "Item not found"
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
        item.getSeller().getItemSeen().remove(item);
        userRepository.save(item.getSeller());

        itemRepository.deleteById(item.getId());

        return ResponseEntity.status(HttpStatus.OK).body(new Message(
                "Ok",
                "Auction has been deleted"
        ));
    }
}