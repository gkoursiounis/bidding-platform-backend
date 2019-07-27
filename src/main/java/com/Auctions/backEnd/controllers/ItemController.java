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

        checkAuction(item);
        return ResponseEntity.ok(item);
    }

    //TODO additional features --> modify user details


    /**
     * A User can get a list of All the open auctions
     * i.e. a list of items where the field auctionCompleted is False
     *
     * @return list of items
     */
    @GetMapping("/openAuctions")
    public ResponseEntity getAllOpenAuctions(){
        auctionClosure();
        return ResponseEntity.ok(itemRepository.getAllOpenAuctions());
    }


    /**
     * A User can get a list of All the items/auctions existing in the database
     *
     * @return list of all items
     */
    @GetMapping("/allAuctions")
    public ResponseEntity getAllItems(){
        auctionClosure();
        return ResponseEntity.ok(itemRepository.findAll());
    }


    /**
     * A User can get a list of All the items/auctions existing in the database
     *
     * @return list of all items
     */
    @GetMapping("/allCategories")
    public ResponseEntity getAllCategoriesNames(){
        return ResponseEntity.ok(itemCategoryRepository.findAll());
    }



    @GetMapping("/search/partialMatch")
    public ResponseEntity getPartialMatchedSearch(@RequestParam String keyword){

        List<Item> res;

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
     * For every word of the text given we search for partial matching
     * in the above mentioned fields and we collect the results in a set
     * called 'res'. Afterwards, we sort the results on a best fit basis
     * i.e. the items appearing more times in the set are moved first in
     * the set
     *
     * @return a list of items
     */
    @GetMapping("/search/searchBar")
    public ResponseEntity searchBar(@RequestParam String text){

        if(text == null || text.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "No keywords given"
            ));
        }

        auctionClosure();

        Set<Item> res = new HashSet<>();

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


    @GetMapping("/search/filters")
    public ResponseEntity filterSearch(@Nullable @RequestParam String category,
                                       @Nullable @RequestParam Double lowerPrice,
                                       @Nullable @RequestParam Double higherPrice,
                                       @Nullable @RequestParam String locationTitle,
                                       @Nullable @RequestParam String description){

        Set<Item> byCategory = null;
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
        else if(higherPrice != null){
            byHigherPrice = itemRepository.searchByHigherPrice(higherPrice);
        }
        else if(lowerPrice != null){
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
                .filter( byDescription::contains)
                .filter( byLocationTitle::contains)
                .filter( byHigherPrice::contains)
                .filter( byLowerPrice::contains)
                .collect(Collectors.toSet());

        return ResponseEntity.ok(result);
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
                                     @RequestParam Double buyPrice,
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

        if(name.isEmpty() || description.isEmpty() || buyPrice == null ||
                firstBid == null || endsAt == null || Double.compare(buyPrice, firstBid) < 0){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "Invalid parameters"
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
        item.setDescription(description);


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
//                item.getCategories().add(category);
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
                    "You cannot modify an item that does not belong to you"
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
        if(description != null) { item.setDescription(description); }


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

            item.getCategories().clear();
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

        checkAuction(item);
        if(!item.getBids().isEmpty() || item.isAuctionCompleted()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
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