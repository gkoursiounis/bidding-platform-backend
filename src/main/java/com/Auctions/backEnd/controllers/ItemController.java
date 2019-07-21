package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.models.*;
import com.Auctions.backEnd.repositories.*;
import com.Auctions.backEnd.responses.Message;
import com.Auctions.backEnd.services.File.DBFileStorageService;
import com.Auctions.backEnd.services.Search.SortComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/item")
public class ItemController {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemCategoryRepository itemCategoryRepository;
    private final DBFileRepository dbFileRepository;
    private final DBFileStorageService dBFileStorageService;
    private final GeolocationRepository geolocationRepository;
    private final BaseController baseController;

    @Autowired
    public ItemController(UserRepository userRepository,ItemRepository itemRepository,
                          ItemCategoryRepository itemCategoryRepository, DBFileRepository dbFileRepository,
                          DBFileStorageService dBFileStorageService, GeolocationRepository geolocationRepository,
                          BaseController baseController){
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.itemCategoryRepository = itemCategoryRepository;
        this.dbFileRepository = dbFileRepository;
        this.dBFileStorageService = dBFileStorageService;
        this.geolocationRepository = geolocationRepository;
        this.baseController = baseController;
    }


    /**
     * A User can get an item/auction details using its itemId
     * If the itemId is invalid then we get an <HTTP>NOT FOUND</HTTP>
     *
     * @param itemId
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

    //TODO additional features --> modify user details
    //TODO HUGE ---> filter time to close open auctions and modify search queries

    /**
     * A User can get a list of His completed auctions
     * i.e. a list of items where the field auctionCompleted is True
     *
     * @return list of items
     */
    @GetMapping("/completedAuctions")
    public ResponseEntity getUserCompletedAuctions(){
        User requester = baseController.requestUser();
        return ResponseEntity.ok(itemRepository.getAllcompletedAuctions(requester));
    }


    /**
     * A User can get a list of All the open auctions
     * i.e. a list of items where the field auctionCompleted is False
     *
     * @return list of items
     */
    @GetMapping("/openAuctions")
    public ResponseEntity getAllOpenAuctions(){
        return ResponseEntity.ok(itemRepository.getAllopenAuctions());
    }


    /**
     * A User can get a list of All the items/auctions existing in the database
     *
     * @return list of all items
     */
    @GetMapping("/allAuctions")
    public ResponseEntity getAllItems(){
        return ResponseEntity.ok(itemRepository.getAllAuctions());
    }



    @GetMapping("/search/partialMatch")
    public ResponseEntity getPartialMatchedSearch(@RequestParam String keyword){

        List<Item> res = new ArrayList<>();

        if (keyword == null || keyword.isEmpty()) {
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


    /**
     * A User can use a search bar to find items/auctions based on:
     * the category name, the item's name and the item's description
     *
     * @return a list of items
     */
    @GetMapping("/search/searchBar")
    public ResponseEntity searchBar(@RequestParam String text){

        if(text.isEmpty() || text == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "No keywords given"
            ));
        }

        Set<Item> res = new HashSet<Item>();

        //split string to words
        String[] values = text.split(" ");
        for (String element : values) {
            res.addAll(itemRepository.searchItems(element));
        }

        //algorithm for sorting elements by frequency was taken from:
        //https://www.geeksforgeeks.org/sort-elements-by-frequency-set-5-using-java-map/

        Map<Item, Integer> map = new HashMap<>();
        List<Item> outputArray = new ArrayList<>();

        //assign elements and their count in the list and map
        for (Item current : res) {
            int count = map.getOrDefault(current, 0);
            map.put(current, count + 1);
            outputArray.add(current);
        }

        //compare the map by value
        SortComparator comp = new SortComparator(map);

        //sort the map using Collections CLass
        Collections.sort(outputArray, comp);

        LinkedHashSet<Item> hashSet = new LinkedHashSet<>(outputArray);

        ArrayList<Item> listWithoutDuplicates = new ArrayList<>(hashSet);

        return ResponseEntity.ok(listWithoutDuplicates);
    }


    //TODO complete
    @GetMapping("/search/filters")
    public ResponseEntity filterSearch(@Nullable @RequestParam String category,
                                       @Nullable @RequestParam Double lowerPrice,
                                       @Nullable @RequestParam Double higherPrice,
                                       @Nullable @RequestParam String locationTitle,
                                       @Nullable @RequestParam String description){

        List<Item> byCategory = new ArrayList<>();
        List<Item> byPrice = new ArrayList<>();
        List<Item> byHigherPrice = new ArrayList<>();
        List<Item> byLowerPrice = new ArrayList<>();
        List<Item> byLocationTitle = new ArrayList<>();
        List<Item> byDescription = new ArrayList<>();

        if(category != null){

            ItemCategory cat = itemCategoryRepository.findItemCategoryByName(category);
            if(cat != null) {
                byCategory = cat.getItems();
            }
        }

        if(lowerPrice != null && higherPrice != null){
            byPrice = itemRepository.searchByPrice(lowerPrice, higherPrice);
        }
        else if(lowerPrice == null){
            byHigherPrice = itemRepository.searchByHigherPrice(higherPrice);
        }
        else{
            byLowerPrice = itemRepository.searchByLowerPrice(lowerPrice);
        }

        if(locationTitle != null){
            byLocationTitle = itemRepository.searchByLocation(locationTitle);
        }

        if(description != null){
            byDescription = itemRepository.searchByDescription(description);
        }

        //https://www.baeldung.com/java-lists-intersection
        Set<Item> result = byCategory.stream()
                .distinct()
                .filter( byPrice::contains)
                .filter( byHigherPrice::contains)
                .filter( byLowerPrice::contains)
                .filter( byLocationTitle::contains)
                .filter( byDescription::contains)
                .collect(Collectors.toSet());

        return ResponseEntity.ok(result);
    }


    /**
     * A user can crate an item
     * By creating an item we consider an auction to have started
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
                                     @RequestParam double buyPrice,
                                     @Nullable @RequestParam(name = "media") MultipartFile media,
                                     @RequestParam Double firstBid,
                                     @Nullable @RequestParam Integer[] categoriesId,    //TODO fix nullable
                                     @Nullable @RequestParam Double longitude,
                                     @Nullable @RequestParam Double latitude,
                                     @Nullable @RequestParam String locationTitle,
                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endsAt,
                                     @Nullable @RequestParam String description) {

        User requestUser = baseController.requestUser();

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
        item.setAuctionCompleted(false);
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

            if (!baseController.contentTypes.contains(media.getContentType())){
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


        if (longitude != null && latitude != null && locationTitle != null){

            Geolocation location = geolocationRepository.findLocationByLatitudeAndLongitude(latitude, longitude);
            if (location == null) {
                location = new Geolocation(longitude, latitude, locationTitle);

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