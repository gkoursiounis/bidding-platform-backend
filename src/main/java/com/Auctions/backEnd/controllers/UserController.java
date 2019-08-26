package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.models.*;
import com.Auctions.backEnd.repositories.*;
import com.Auctions.backEnd.responses.FormattedUser;
import com.Auctions.backEnd.responses.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@RestController
@CrossOrigin(origins = "https://*:3000")
@RequestMapping("/user")
public class UserController extends BaseController{

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final NotificationRepository notificationRepository;

    @Autowired
    public UserController(UserRepository userRepository, ItemRepository itemRepository,
                          NotificationRepository notificationRepository){
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.notificationRepository = notificationRepository;
    }


    /**
     * A user can get the details of a user (himself or another user)
     * If the user requests himself the we return a FormattedUser which
     * contains more details than in the case the user requests another user
     *
     * @return user details
     */
    @GetMapping()
    public ResponseEntity getUserDetails() {

        User requester = requestUser();
        return ResponseEntity.ok(new FormattedUser(requester));
    }


    /**
     * A User can get a list of His auctions
     *
     * @return list of items
     */
    @GetMapping("/myAuctions")
    public ResponseEntity getMyAuctions() {

        User requester = requestUser();
        Set<Item> allAuctions = requester.getItems();

        return ResponseEntity.ok(allAuctions);
    }


    /**
     * A User can get a list of His open auctions
     * i.e. a list of items where the field auctionCompleted is False
     *
     * @return list of items
     */
    @GetMapping("/myOpenAuctions")
    public ResponseEntity getMyOpenAuctions() {

        User requester = requestUser();
        Set<Item> openAuctions = requester.getItems();
        openAuctions.removeIf(item -> item.isAuctionCompleted());

        return ResponseEntity.ok(openAuctions);
    }


    /**
     * A User can get a list of His completed auctions
     * i.e. a list of items where the field auctionCompleted is True
     *
     * @return list of items
     */
    @GetMapping("/myCompletedAuctions")
    public ResponseEntity getMyCompletedAuctions(){

        User requester = requestUser();
        Set<Item> completedAuctions = requester.getItems();
        completedAuctions.removeIf(item -> !item.isAuctionCompleted());

        return ResponseEntity.ok(completedAuctions);
    }


    /**
     * User can get a list of the bids he has made
     *
     * @return list of bids
     */
    @GetMapping("/myBids")
    public ResponseEntity getMyBids() {

        User requester = requestUser();
        return ResponseEntity.ok(requester.getBids());
    }


    /**
     * User can get a list of his notifications
     *
     * @return list of notifications
     */
    @GetMapping("/myNotifications")
    public ResponseEntity getMyNotifications() {
//test();
        return ResponseEntity.ok(requestUser().getNotifications());
    }


    /**
     * User can get a list of his unseen notifications
     *
     * @return list of notifications
     */
    @GetMapping("/unseenNotifications")
    public ResponseEntity getUnseenNotifications() {

        Set<Notification> notifications = requestUser().getNotifications();
        notifications.removeIf(notification -> notification.isSeen());
        return ResponseEntity.ok(notifications);
    }


    /**
     * User can mark all of his unseen notifications as seen
     *
     * @return <HTTP>OK</HTTP>
     */
    @PatchMapping("/allSeen")
    public ResponseEntity markAllAsSeen() {

        User requester = requestUser();

        List<Notification> unseen = notificationRepository.getAllUnseenNotifications();
        unseen.forEach(notification -> {
            notification.setSeen(true);
            notificationRepository.save(notification);
        });

        return ResponseEntity.ok(new Message(
                "Ok",
                "All notifications are now seen"
        ));
    }

//TODO additional features --> modify user details
    /**
     * User can mark all of his unseen notifications as seen
     *
     * @return <HTTP>OK</HTTP>
     */
    @PatchMapping("/markNotification/{notId}")
    public ResponseEntity markNotification(@PathVariable (value = "notId") long notId) {

        User requester = requestUser();

        Notification not = notificationRepository.findNotificationById(notId);
        not.setSeen(true);
        notificationRepository.save(not);

        return ResponseEntity.ok(not);

    }


    /**
     * User can mark all of his unseen notifications as seen
     *
     * @return <HTTP>OK</HTTP>
     */
    @PatchMapping("/rating/{itemId}")
    public ResponseEntity rateUser(@PathVariable (value = "itemId") long itemId,
                                   @RequestParam Integer score) {

        User requester = requestUser();

        Item item = itemRepository.findItemById(itemId);
        if(item == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
                    "Error",
                    "Auction not found"
            ));
        }

        if(!item.isAuctionCompleted()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "You can rate a user only after the end of an auction"
            ));
        }

        //TODO test
        int rating;
        switch (score){
            case 1:
                rating = -2;
                break;
            case 2:
                rating = -1;
                break;
            case 3:
                rating = 0;
                break;
            case 4:
                rating = 1;
                break;
            case 5:
                rating = 2;
                break;
            default:
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                        "Error",
                        "Score is from 1 up to 5"
                ));
        }

        User highestBidder = Collections.max(item.getBids(), Bid.cmp).getBidder();

        if(requester.equals(item.getSeller())){
            item.setBidderRating(rating);
            itemRepository.save(item);

            highestBidder.setBidderRating(highestBidder.getBidderRating() + rating);
            userRepository.save(highestBidder);

            return ResponseEntity.ok(item);
        }
        else if(requester.equals(highestBidder)){
            item.setSellerRating(rating);
            itemRepository.save(item);

            int current = item.getSeller().getSellerRating();
            item.getSeller().setSellerRating(current + rating);
            userRepository.save(item.getSeller());

            return ResponseEntity.ok(item);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message(
                "Error",
                "Only the seller and the highest bidder can rate the participants of this auction"
        ));
    }
}