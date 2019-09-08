package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.models.*;
import com.Auctions.backEnd.repositories.*;
import com.Auctions.backEnd.responses.BidRes;
import com.Auctions.backEnd.responses.Message;
import info.debatty.java.lsh.LSHMinHash;
import info.debatty.java.lsh.LSHSuperBit;
import org.jdom.Attribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.*;
import java.util.*;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

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
    @GetMapping
    public ResponseEntity test2() {

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

    @GetMapping("/lsh")
   public ResponseEntity lsh() throws JDOMException, IOException {


        List<User> allUsers = userRepository.findAll();
        int userSize = allUsers.size();

        List<Item> allItems = itemRepository.findAll();
        int itemSize = allItems.size();

        int[][] vectors = new int[userSize][];
        for (int i = 0; i < userSize; i++) {
            vectors[i] = new int[itemSize];

            List<Item> items = new ArrayList<>();
            allUsers.get(i).getBids().forEach(bid -> {
                items.add(bid.getItem());
            });

            for (int j = 0; j < itemSize; j++) {
                if (items.contains(allItems.get(j))) {
                    vectors[i][j] = 1;
                } else {
                    vectors[i][j] = 0;
                }
            }
        }

        for (int i = 0; i < userSize; i++) {

            System.out.print(allUsers.get(i).getUsername() + "   ");
            for (int j = 0; j < itemSize; j++) {
               System.out.print(vectors[i][j]);
            }
            System.out.println();
        }
//
//        int count = 80;
//
//        // R^n
//        int n = 16;
//
//        int stages = 5;
//        int buckets = 15;
//
//        // Produce some vectors in R^n
//        Random r = new Random();
//        int[][] vectors = new int[count][];
//        for (int i = 0; i < count; i++) {
//            vectors[i] = new int[n];
//
//            for (int j = 0; j < n; j++) {
//                vectors[i][j] = ThreadLocalRandom.current().nextInt(0, 1 + 1);
//              //  System.out.print(vectors[i][j]);
//            }
//          //  System.out.println();
//        }
//
//        LSHSuperBit lsh = new LSHSuperBit(stages, buckets, n);
//
//        // Compute a SuperBit signature, and a LSH hash
//        for (int i = 0; i < count; i++) {
//            int[] vector = vectors[i];
//            int[] hash = lsh.hash(vector);
//            for (int v : vector) {
//                System.out.print(v);
//            }
//            System.out.print(" : " + hash[0]);
//            System.out.print("\n");
//        }

        return ResponseEntity.ok(null);
    }




//    @GetMapping("/test3")
//    public ResponseEntity test3() {
//
//        SAXBuilder builder = new SAXBuilder();
//        File xmlFile = new File("media/book.xml");
//
//        try {
//
//            Document document = (Document) builder.build(xmlFile);
//            Element rootNode = document.getRootElement();
//            List list = rootNode.getChildren();
//
//            for (int i = 0; i < list.size(); i++) {
//
//                Element node = (Element) list.get(i);
//
//                System.out.println("First Name : " + node.getChildText("Name"));
//                Attribute attribute =  node.getChild("Location").getAttribute("Longitude");
//                System.out.println("Student roll no : "
//                        + attribute.getValue() );
//
//
//
//            }
//
//        } catch (IOException io) {
//            System.out.println(io.getMessage());
//        } catch (JDOMException jdomex) {
//            System.out.println(jdomex.getMessage());
//        }
//        return ResponseEntity.ok(null);
//    }

}
