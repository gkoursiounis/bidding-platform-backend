package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.models.*;
import com.Auctions.backEnd.repositories.*;
import com.Auctions.backEnd.responses.Message;
import com.Auctions.backEnd.responses.RatedItem;
import info.debatty.java.lsh.LSHSuperBit;
import org.jdom.Attribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.util.*;

import java.io.File;
import java.io.IOException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import static info.debatty.java.lsh.SuperBit.cosineSimilarity;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/recommend")
public class RecommendationController extends BaseController{

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BidRepository bidRepository;
    private final GeolocationRepository geolocationRepository;
    private final ItemCategoryRepository itemCategoryRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public RecommendationController(UserRepository userRepository, ItemRepository itemRepository,
                         BidRepository bidRepository, GeolocationRepository geolocationRepository,
                         ItemCategoryRepository itemCategoryRepository, PasswordEncoder passwordEncoder,
                                    AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.bidRepository = bidRepository;
        this.geolocationRepository = geolocationRepository;
        this.itemCategoryRepository = itemCategoryRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountRepository = accountRepository;
    }


    /**
     * https://www.tutorialspoint.com/java_xml/java_jdom_parse_document.htm#
     * https://www.mkyong.com/java/how-to-read-xml-file-in-java-jdom-example/
     *
     * @return
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws XPathExpressionException
     */
    @GetMapping("/xmlRead")
    public ResponseEntity loadFromXml() {

//        User requester = requestUser();
//
//        if(!requester.isAdmin()){
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message(
//                    "Error",
//                    "You need to be an admin to perform this action"
//            ));
//        }

            Geolocation zero = geolocationRepository.findLocationByLatitudeAndLongitude(0.0,0.0);
            if(zero == null){
                zero = new Geolocation();
                zero.setLatitude(0.0);
                zero.setLongitude(0.0);
                zero.setLocationTitle("No available location");
                geolocationRepository.save(zero);
            }

        try {
            File inputFile = new File("ebay/items-10.xml");

            SAXBuilder saxBuilder = new SAXBuilder();
            Document document = saxBuilder.build(inputFile);

            // System.out.println("Root element :" + document.getRootElement().getName());
            Element classElement = document.getRootElement();
            List<Element> itemList = classElement.getChildren();

            for (int i = 0; i < itemList.size(); i++) {

                System.out.println("Importing " + (i+1) + " of " + itemList.size());

                Element xmlItem = itemList.get(i);
                Item item = new Item();

                item.setName(xmlItem.getChildText("Name"));

                item.setCurrently(
                        Double.valueOf(xmlItem.getChildText("Currently").substring(1)));

                item.setFirstBid(
                        Double.valueOf(xmlItem.getChildText("First_Bid").substring(1)));

                //item.setDescription(xmlItem.getChildText("Description"));

                if(xmlItem.getChildText("Buy_Price") != null){
                    item.setBuyPrice(
                            Double.valueOf(xmlItem.getChildText("Buy_Price").substring(1)));
                }

                Attribute longitude =  xmlItem.getChild("Location").getAttribute("Longitude");
                Attribute latitude =  xmlItem.getChild("Location").getAttribute("Latitude");
                String locationTitle = xmlItem.getChildText("Location") + ", " +
                        xmlItem.getChildText("Country");

                item.setEndsAt(new Date(new Date().getTime() + 10*86400000));

                Geolocation location;
                if (longitude != null && latitude != null){

                    Double lat = Double.valueOf(latitude.getValue());
                    Double lon = Double.valueOf(longitude.getValue());

                    location = geolocationRepository.findLocationByLatitudeAndLongitude(lat, lon);
                    if (location == null) {
                        location = new Geolocation(lon, lat, locationTitle);
                    }
                }
                else {
                    location = zero;
                }

                item.setLocation(location);
                location.getItems().add(item);
                geolocationRepository.save(location);

                ItemCategory category = null;
                List<Element> categories = xmlItem.getChildren("Category");
                for (int j = 0; j < categories.size(); j++) {

                    Element cat = categories.get(j);

                    if(j == 0) {

                        category = itemCategoryRepository.findItemCategoryByName(cat.getText());
                        if (category == null) {

                            ItemCategory allCategories = itemCategoryRepository.findItemCategoryByName("All categories");

                            category = new ItemCategory();
                            category.setName(cat.getText());
                            category.setParent(allCategories);
                            itemCategoryRepository.save(category);

                            allCategories.getSubcategories().add(category);
                            itemCategoryRepository.save(allCategories);
                        }
                        item.getCategories().add(category);
                    }
                    else {

                        if (itemCategoryRepository.findItemCategoryByName(cat.getText()) == null) {

                            ItemCategory subcategory = new ItemCategory();
                            subcategory.setName(cat.getText());
                            subcategory.setParent(category);
                            itemCategoryRepository.save(subcategory);

                            category.getSubcategories().add(subcategory);
                            itemCategoryRepository.save(category);

                            item.getCategories().add(subcategory);
                            category = subcategory;
                        }
                    }
                }

                List<Element> bids = xmlItem.getChildren("Bids");
                for (int a = 0; a < bids.size(); a++) {

                    Element BidElement = bids.get(a);
                    List<Element> bidList = BidElement.getChildren("Bid");
                    for (int b = 0; b  < bidList.size(); b++) {

                        Element bid = bidList.get(b);

                        String username = bid.getChild("Bidder").getAttribute("UserID").getValue();
                        username = username
                                .replace("@", "1")
                                .replace(".", "2")
                                .replace("$", "3")
                                .replace("*", "4");

                        User bidder = userRepository.findByAccount_Username(username);
                        if(bidder == null){
                            Account account = new Account();
                            account.setUsername(username);
                            account.setEmail(username + "@di.uoa.gr");
                            account.setPassword(passwordEncoder.encode("123456"));
                            account.setVerified(true);

                            bidder = new User();
                            bidder.setFirstName("FirstName");
                            bidder.setLastName("LastName");
                            bidder.setTelNumber("1234567890");
                            bidder.setTaxNumber("1234");
                            bidder.setAccount(account);

                            accountRepository.save(account);
                        }

                        if(bidder.getAddress() == null){
                            zero.getUsers().add(bidder);
                            bidder.setAddress(zero);
                            geolocationRepository.save(zero);
                        }

                        if(bidder.getBidderRating() == 0){
                            bidder.setBidderRating(
                                    Integer.valueOf(bid.getChild("Bidder").getAttribute("Rating").getValue()));
                        }

                        Bid newBid = new Bid();
                        newBid.setOffer(Double.valueOf(bid.getChildText("Amount").substring(1)));
                        newBid.setBidder(bidder);
                        newBid.setItem(item);
                        bidRepository.save(newBid);

                        bidder.getBids().add(newBid);
                        userRepository.save(bidder);

                        item.getBids().add(newBid);
                    }
                }

                String username = xmlItem.getChild("Seller").getAttribute("UserID").getValue();
                username = username
                        .replace("@", "1")
                        .replace(".", "2")
                        .replace("$", "3")
                        .replace("*", "4");

                User seller = userRepository.findByAccount_Username(username);
                if(seller == null){
                    Account account = new Account();
                    account.setUsername(username);
                    account.setEmail(username + "@di.uoa.gr");
                    account.setPassword(passwordEncoder.encode("123456"));
                    account.setVerified(true);

                    account  = accountRepository.save(account);

                    seller = new User();
                    seller.setFirstName("FirstName");
                    seller.setLastName("LastName");
                    seller.setTelNumber("1234567890");
                    seller.setTaxNumber("1234");
                    seller.setAccount(account);
                }

                if(seller.getAddress() == null){
                    seller.setAddress(zero);
                    zero.getUsers().add(seller);
                    geolocationRepository.save(zero);
                }

                if(seller.getSellerRating() == 0){
                    seller.setSellerRating(
                            Integer.valueOf(xmlItem.getChild("Seller").getAttribute("Rating").getValue()));
                }

                item.setSeller(seller);
                userRepository.save(seller);
                seller.getItems().add(item);
                itemRepository.save(item);
            }
        } catch(JDOMException e) {
            e.printStackTrace();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }

        return ResponseEntity.ok(new Message(
                "Ok",
                "All Items have bee imported"
        ));
    }


    @GetMapping("/visitor")
    public ResponseEntity popularItems(){
        List<Item> items = itemRepository.popularItems();
        if(items.size() > 5){
            return ResponseEntity.ok(items.subList(0,5));
        }
        return ResponseEntity.ok(items);
    }



    @GetMapping("/lsh")
    public ResponseEntity lsh(@RequestParam String username) throws JDOMException, IOException {

        if(userRepository.findByAccount_Username(username) == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
                    "Error",
                    "User not found"
            ));
        }

        List<User> allUsers = userRepository.findAll();
        allUsers.removeIf(user -> user.getBids().isEmpty());
        int userSize = allUsers.size();

        List<Item> allItems = itemRepository.findAll();
        int itemSize = allItems.size();

        if(itemSize == 0){
            return ResponseEntity.ok(null);
        }

        double[][] vectors = new double[userSize][];
        for (int i = 0; i < userSize; i++) {
            vectors[i] = new double[itemSize];

            List<Item> items = new ArrayList<>();
            allUsers.get(i).getBids().forEach(bid -> {
                items.add(bid.getItem());
            });

            for (int j = 0; j < itemSize; j++) {
                if(items.contains(allItems.get(j))) {
                    vectors[i][j] = 1;
                } else if(items.contains(allUsers.get(i).getItemSeen())){
                    vectors[i][j] = 0.5;
                } else {
                    vectors[i][j] = 0;
                }
            }
        }

//        for (int i = 0; i < userSize; i++) {
//
//            System.out.print(allUsers.get(i).getUsername() + "   ");
//            for (int j = 0; j < itemSize; j++) {
//               System.out.print(vectors[i][j]  + " ");
//            }
//            System.out.println();
//        }

        int stages = 5;
        int buckets = (int) Math.sqrt(userSize);

        int activeUserBucket = -1;
        int activeUserPosition = 0;
        double avgRating = 0.0;

        LSHSuperBit lsh = new LSHSuperBit(stages, buckets, itemSize);
        Map<Integer, List<Integer>> map = new HashMap<>();

        for (int i = 0; i < userSize; i++) {

            double[] vector = vectors[i];
            int[] hash = lsh.hash(vector);

            List<Integer> neighbours = map.get(hash[0]);
            if(neighbours == null){
                neighbours = new ArrayList<>();
            }
            neighbours.add(i);
            map.put(hash[0], neighbours);

            if(allUsers.get(i).getUsername().equals(username)){
                activeUserBucket = hash[0];
                activeUserPosition = i;
             //   System.err.println("activeUserBucket " + activeUserBucket);
            // System.err.println("activeUserPosition " + activeUserPosition);
                avgRating =  Arrays.stream(vector).average().orElse(0);
             //   System.err.println("avgRating " + avgRating);
            }

           // System.out.print(allUsers.get(i).getUsername() + " :\t" + hash[0]);
            //System.out.print("\n");
        }

//        System.out.println("\n\n\n NEIGHBORS");

        List<Integer> neighborhood = map.get(activeUserBucket);
        if(neighborhood == null){
            return ResponseEntity.ok(null);
        }

//        for (Integer integer : neighborhood) {
//            System.out.print(allUsers.get(integer).getUsername() + "   ");
//            for (int j = 0; j < itemSize; j++) {
//               System.out.print(vectors[integer][j]  + " ");
//            }
//            System.out.println();
//        }

        neighborhood.removeIf(number -> allUsers.get(number).getUsername().equals(username));

//        System.out.println("dddd");
//        for (Integer k : neighborhood) {
//            System.out.println(vectors[activeUserPosition]);
//            System.out.println(vectors[k]);
//            System.out.println("cosine" +
//                    "");
//            System.out.println(cosineSimilarity(vectors[activeUserPosition], vectors[k]));
//        }
//        System.out.println("dd");

        int finalActiveUserPosition = activeUserPosition;
        double lambda = 1 / neighborhood.stream().mapToDouble(
                neighbourPosition -> cosineSimilarity(vectors[finalActiveUserPosition], vectors[neighbourPosition])).sum();

        //auctions that users has participated in
        List<Item> participations = new ArrayList<>();
        allUsers.get(finalActiveUserPosition).getBids().forEach(bid -> {
            participations.add(bid.getItem());
        });

        //for every neighbour of the active user we get the auctions they have participated in
        List<RatedItem> ratedItems = new ArrayList<>();
        double finalAvgRating = avgRating;
        neighborhood.forEach(neighbourPosition -> {
            allUsers.get(neighbourPosition).getBids().forEach(bid -> {

                if(!participations.contains(bid.getItem()) &&
                        !allUsers.get(finalActiveUserPosition).getItems().contains(bid.getItem())) {

                    double sum = neighborhood.stream().mapToDouble(neighbourPos ->
                            cosineSimilarity(vectors[finalActiveUserPosition], vectors[neighbourPos]) *
                                    (1 - Arrays.stream(vectors[neighbourPos]).average().orElse(0))
                    ).sum();
                    System.out.println(finalAvgRating + lambda * sum);

                    List<Item> existingRatings = new ArrayList<>();
                    ratedItems.forEach(item -> existingRatings.add(item.getItem()));
                    if(!existingRatings.contains(bid.getItem())){
                        ratedItems.add(new RatedItem(bid.getItem(), finalAvgRating + lambda * sum));
                    }
                    else{
                        double score = finalAvgRating + lambda * sum;
                        if(ratedItems.get(existingRatings.indexOf(bid.getItem())).getRating() < score){
                            ratedItems.get(existingRatings.indexOf(bid.getItem())).setRating(score);
                        }
                    }
                }
            });
        });

//        System.out.println("\n\n\n");
//        int finalActiveUserBucket = activeUserBucket;
//        map.entrySet().forEach(entry-> {
//            if(entry.getKey() == finalActiveUserBucket) {
//                System.out.println(entry.getKey());
//                List<Integer> nn = entry.getValue();
//                nn.forEach(user -> {
//                    System.out.println(allUsers.get(user).getUsername());
//                });
//            }
//        });

        ratedItems.sort(Comparator.comparingDouble(RatedItem::getRating).reversed());
//        System.out.println("\n\n\nFINAL");
        ratedItems.forEach(item -> { System.out.println(item.getItem().getName() + " with rating " + item.getRating());});

        List<Item> finalRatings = new ArrayList<>();
        ratedItems.forEach(item -> finalRatings.add(item.getItem()));
        return ResponseEntity.ok(finalRatings);
    }
}

