package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.models.*;
import com.Auctions.backEnd.repositories.*;
import com.Auctions.backEnd.responses.BidRes;
import com.Auctions.backEnd.responses.Message;
import org.jdom.Attribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.*;
import java.util.Date;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/bid")
public class BidController extends BaseController{

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BidRepository bidRepository;
    private final GeolocationRepository geolocationRepository;

    @Autowired
    public BidController(UserRepository userRepository, ItemRepository itemRepository,
                          BidRepository bidRepository, GeolocationRepository geolocationRepository) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.bidRepository = bidRepository;
        this.geolocationRepository = geolocationRepository;
    }


    /**
     * A user can get the details of a bid using the bidId
     *
     * @param bidId - the id of the bid
     * @return the bid
     */
    @GetMapping("/{bidId}")
    public ResponseEntity getBid(@PathVariable (value = "bidId") long bidId){
        Bid bid = bidRepository.findBidById(bidId);
        if(bid == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
                    "Error",
                    "Invalid id. Bid not found"
            ));
        }

        return ResponseEntity.ok(bid);
    }


    /**
     * A user can participate in an auction by making a bid
     *
     * @param offer - the amount of the bid
     * @return the created bid
     */
    @PostMapping("/makeBid/{itemId}")
    public ResponseEntity makeBid(@PathVariable (value = "itemId") long itemId,
                                  @RequestParam Double offer){

        User requester = requestUser();

        Item item = itemRepository.findItemById(itemId);
        if (item == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
                    "Error",
                    "Item not found. Invalid item Id"
            ));
        }

        if(checkAuction(item)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "Auction has been completed and no bids can be made"
            ));
        }

        if(item.getSeller().equals(requester)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "You cannot bid at your own auction"
            ));
        }
//TODO fix
        if(java.lang.Double.compare(offer, item.getCurrently()) <= 0){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "Offer cannot be equal or less than the current best offer or the initial price"
            ));
        }

        item.setCurrently(offer);
        if(java.lang.Double.compare(item.getBuyPrice(), offer) <= 0){
            item.setAuctionCompleted(true);
            notifySeller(item);
        }

        Bid bid = new Bid(new Date());
        bid.setBidder(requester);
        bid.setItem(item);
        bid.setOffer(offer);
        bidRepository.save(bid);

        requester.getBids().add(bid);
        userRepository.save(requester);

        item.getBids().add(bid);
        itemRepository.save(item);

        return ResponseEntity.ok(new BidRes(bid, item.isAuctionCompleted()));
    }


    @GetMapping("/test1")
    public ResponseEntity test1() {
        return ResponseEntity.ok(itemRepository.findAll());
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
    @GetMapping("/test2")
    public ResponseEntity test2() {

        try {
            File inputFile = new File("media/book.xml");

            SAXBuilder saxBuilder = new SAXBuilder();
            Document document = saxBuilder.build(inputFile);

           // System.out.println("Root element :" + document.getRootElement().getName());
            Element classElement = document.getRootElement();
            List<Element> itemList = classElement.getChildren();

            for (int i = 0; i < itemList.size(); i++) {

                Element xmlItem = itemList.get(i);
                Item item = new Item();

                item.setName(xmlItem.getChildText("Name"));

                item.setCurrently(
                        Double.valueOf(xmlItem.getChildText("Currently").substring(1)));

                item.setFirstBid(
                        Double.valueOf(xmlItem.getChildText("First_Bid").substring(1)));

                item.setDescription(xmlItem.getChildText("Description"));

                if(xmlItem.getChildText("Buy_Price") != null){
                    item.setBuyPrice(
                            Double.valueOf(xmlItem.getChildText("Buy_Price").substring(1)));
                }

                Attribute longitude =  xmlItem.getChild("Location").getAttribute("Longitude");
                Attribute latitude =  xmlItem.getChild("Location").getAttribute("Latitude");
                String locationTitle = xmlItem.getChildText("Location") + ", " +
                        xmlItem.getChildText("Country");

                Geolocation location;
                if (longitude.getValue() != null && latitude.getValue() != null){

                    Double lat = Double.valueOf(latitude.getValue());
                    Double lon = Double.valueOf(longitude.getValue());

                    location = geolocationRepository.findLocationByLatitudeAndLongitude(lat, lon);
                    if (location == null) {
                        location = new Geolocation(lon, lat, locationTitle);
                    }
                }
                else {
                    location = new Geolocation(null, null, locationTitle);
                }

                item.setLocation(location);
                location.getItems().add(item);
                geolocationRepository.save(location);

                System.out.println(xmlItem.getChildren("Category").size());


                return ResponseEntity.ok(item);
//                System.out.println("\nCurrent Element :"
//                        + xmlItem.getName());

//                Attribute attribute =  xmlItem.getChild("Location").getAttribute("Longitude");
//                System.out.println("Student roll no : "
//                        + attribute.getValue() );
//
//                System.out.println("First Name : "
//                        + xmlItem.getChild("Name").getText());
//                System.out.println("Last Name : "
//                        + xmlItem.getChildText("Currently"));
            }
        } catch(JDOMException e) {
            e.printStackTrace();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }

        return ResponseEntity.ok(null);


    }

    @GetMapping("/test3")
    public ResponseEntity test3() {

        SAXBuilder builder = new SAXBuilder();
        File xmlFile = new File("media/book.xml");

        try {

            Document document = (Document) builder.build(xmlFile);
            Element rootNode = document.getRootElement();
            List list = rootNode.getChildren();

            for (int i = 0; i < list.size(); i++) {

                Element node = (Element) list.get(i);

                System.out.println("First Name : " + node.getChildText("Name"));
                Attribute attribute =  node.getChild("Location").getAttribute("Longitude");
                System.out.println("Student roll no : "
                        + attribute.getValue() );



            }

        } catch (IOException io) {
            System.out.println(io.getMessage());
        } catch (JDOMException jdomex) {
            System.out.println(jdomex.getMessage());
        }
        return ResponseEntity.ok(null);
    }
}