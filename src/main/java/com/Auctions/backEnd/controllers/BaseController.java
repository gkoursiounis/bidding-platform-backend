package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.models.*;
import com.Auctions.backEnd.repositories.*;
import com.Auctions.backEnd.services.Security.TokenProvider;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.Auctions.backEnd.services.Security.JWTFilter.resolveToken;

public abstract class BaseController {

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private GeolocationRepository geolocationRepository;

    @Autowired
    private ItemCategoryRepository itemCategoryRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BidRepository bidRepository;


    /**
     * Approved content types for pictures
     */
    static final Set<String> contentTypes = new HashSet<>(Arrays.asList(
            "image/png",
            "image/jpeg",
            "image/gif"
    ));


    /**
     * Helper function that returns the User who makes a request
     * based on the token authentication
     *
     * @return the user details
     */
     User requestUser(){

        final HttpServletRequest currentRequest =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String token = resolveToken(currentRequest);
        Authentication authentication = tokenProvider.getAuthentication(token);

        return userRepository.findByAccount_Username(authentication.getName());
    }


    /**
     * Helper functions for early warning auction closure
     *
     * A thread (see BackEndApplication.java) is created every
     * 5 seconds in order to inspect and terminate the auctions
     * whose 'endsAt' time has been reached.
     * But since there is a gap of maximum 5 seconds from an
     * 'endsAt' time to the check, we need to make sure that
     * a user won't be able to make a bid or make any changes
     * to the auction within this time.
     *
     * @param item - the auction
     * @return a bool {true if auction is over, false if auction is open}
     */
    boolean checkAuction(Item item){

        if(item.getEndsAt().getTime() < System.currentTimeMillis()){
            return true;
        } else {
            return false;
        }
    }


    /**
     * Helper function to notify seller in case some user
     * wins an auction using the 'buyPrice' option. In that
     * case, the auction is terminated by the route not the
     * auto-closure thread
     *
     * @param item - the auction
     */
    void notifySeller(Item item){

        Notification toSeller = new Notification();
        toSeller.setRecipient(item.getSeller());
        toSeller.setItemId(item.getId());
        toSeller.setMessage("Your auction with name " + item.getName() + " has been completed");
        notificationRepository.save(toSeller);

        item.getSeller().getNotifications().add(toSeller);
        userRepository.save(item.getSeller());
    }


    /**
     * https://www.tutorialspoint.com/java_xml/java_jdom_parse_document.htm#
     * https://www.mkyong.com/java/how-to-read-xml-file-in-java-jdom-example/
     */
    public void readFromXml(String pathname) {

        Geolocation zero = geolocationRepository.findLocationByLatitudeAndLongitude(0.0,0.0);
        if(zero == null){
            zero = new Geolocation();
            zero.setLatitude(0.0);
            zero.setLongitude(0.0);
            zero.setLocationTitle("Zero Point");
            geolocationRepository.save(zero);
        }

        try {
            File inputFile = new File(pathname);

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
                        User bidder = userRepository.findByAccount_Username(username);
                        if(bidder == null){
                            Account account = new Account();
                            account.setUsername(username);
                            account.setEmail(username + "@di.uoa.gr");
                            account.setPassword(passwordEncoder.encode("123456"));
                            account.setVerified(true);
                            account.setAdmin(false);

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
                User seller = userRepository.findByAccount_Username(username);
                if(seller == null){
                    Account account = new Account();
                    account.setUsername(username);
                    account.setEmail(username + "@di.uoa.gr");
                    account.setPassword(passwordEncoder.encode("123456"));
                    account.setVerified(true);
                    account.setAdmin(false);

                    seller = new User();
                    seller.setFirstName("FirstName");
                    seller.setLastName("LastName");
                    seller.setTelNumber("1234567890");
                    seller.setTaxNumber("1234");
                    seller.setAccount(account);

                    accountRepository.save(account);
                }

                if(seller.getAddress() == null){
                    zero.getUsers().add(seller);
                    seller.setAddress(zero);
                    geolocationRepository.save(zero);
                }

                if(seller.getSellerRating() == 0){
                    seller.setSellerRating(
                            Integer.valueOf(xmlItem.getChild("Seller").getAttribute("Rating").getValue()));
                }

                seller.getItems().add(item);
                item.setSeller(seller);
                userRepository.save(seller);
                itemRepository.save(item);
            }
        } catch(JDOMException e) {
            e.printStackTrace();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }
}