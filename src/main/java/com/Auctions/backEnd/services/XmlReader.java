package com.Auctions.backEnd.services;

import com.Auctions.backEnd.models.*;
import com.Auctions.backEnd.repositories.*;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public abstract class XmlReader {

    @Autowired
    private static GeolocationRepository geolocationRepository;

    @Autowired
    private static ItemCategoryRepository itemCategoryRepository;

    @Autowired
    private static ItemRepository itemRepository;

    @Autowired
    private static UserRepository userRepository;

    @Autowired
    private static AccountRepository accountRepository;

    @Autowired
    private static PasswordEncoder passwordEncoder;

    @Autowired
    private static BidRepository bidRepository;


    public static void XmlLoader(String pathname, int amount){
        Geolocation zero = geolocationRepository.findLocationByLatitudeAndLongitude(0.0,0.0);
        if(zero == null){
            zero = new Geolocation();
            zero.setLatitude(0.0);
            zero.setLongitude(0.0);
            zero.setLocationTitle("No available location");
            geolocationRepository.save(zero);
        }

        try {
            File inputFile = new File(pathname);

            SAXBuilder saxBuilder = new SAXBuilder();
            Document document = saxBuilder.build(inputFile);

            // System.out.println("Root element :" + document.getRootElement().getName());
            Element classElement = document.getRootElement();
            List<Element> itemList = classElement.getChildren();

            int itemsToRead;
            if(itemList.size() < amount){
                itemsToRead = itemList.size();
            }
            else {
                itemsToRead = amount;
            }

            for (int i = 0; i < itemsToRead; i++) {

                System.out.println("Importing " + (i+1) + " of " + itemsToRead);

                Element xmlItem = itemList.get(i);
                Item item = new Item();

                //System.out.println(xmlItem.getChildText("Name"));

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
    }
}
