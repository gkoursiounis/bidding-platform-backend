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
@CrossOrigin(origins = "https://localhost:3000")
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
                                   @RequestParam Integer rating) {

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

    public void test(){

        List<Item> auctions = itemRepository.getAllOpenAuctions();
        auctions.forEach(item -> {

            if(item.getEndsAt().getTime() < System.currentTimeMillis()) {
                item.setAuctionCompleted(true);
                itemRepository.save(item);
                System.err.println("HERE");
                Notification toSeller = new Notification();
                toSeller.setRecipient(item.getSeller());
                toSeller.setItemId(item.getId());
                toSeller.setMessage("Your auction with name \"" + item.getName() + "\" has been completed");
                notificationRepository.save(toSeller);

                item.getSeller().getNotifications().add(toSeller);
                userRepository.save(item.getSeller());
                System.err.println("Sending to seller");

                if(!item.getBids().isEmpty()) {
                    Notification toBuyer = new Notification();
                    User highestBidder = Collections.max(item.getBids(), Bid.cmp).getBidder();
                    toBuyer.setRecipient(highestBidder);
                    toBuyer.setItemId(item.getId());
                    toBuyer.setMessage("Congratulations! You won the auction for " + item.getName());
                    notificationRepository.save(toBuyer);

                    highestBidder.getNotifications().add(toBuyer);
                    userRepository.save(highestBidder);
                    System.err.println("Sending to buyer");
                }
            }
        });
    }

//    @GetMapping("/search")
//    public ResponseEntity getPartialMatchedUsers(@RequestParam String name) {
//        User requester = requestUser();
//        List<FormattedUser> res = new ArrayList<>();
//
//        if ("".equals(name)) {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
//                        "Error",
//                        "Invalid name."
//                ));
//        }
//
//        name = name.toLowerCase();
//
//        userRepository.searchUsers(name).forEach(user -> {
//            if (user != requester && !user.getBlocked().contains(requester) && !requester.getBlocked().contains(user)) {
//                user.setFollowed(requester.getFollowing().contains(user));
//                res.add(new FormattedUser(user));
//            }
//        });
//
//        Collections.sort(res);
//        return ResponseEntity.ok(res);
//    }

//

//    @PostMapping("/following-request/{followingRequestId}")
//    public ResponseEntity acceptOrRejectFollowingRequest(@PathVariable long followingRequestId,
//                                                         @RequestParam boolean accept) {
//        User followed = requestUser();
//
//        return followingRequestRepository.findById(following
//        RequestId).map(followingRequest -> {
//            if (accept){
//                User follower = followingRequest.getFollower();
//
//                // follower.getFollowing().add(followed);
//                // userRepository.save(followed);
//
//                this.followRepository.save(
//                        new Follow(follower, followed)
//                );
//
//
//
//                Set<User> followers = new HashSet<>(follower.getFollowers());
//
//                for (User followFollower: followers) {
//                    if (followFollower != followed) {
//                        this.notificationRepository.save(
//                                new FollowNotification(
//                                        follower,
//                                        followFollower,
//                                        follower,
//                                        followed,
//                                        true
//                                )
//                        );
//                    }
//                }
//            }
//            followingRequestRepository.delete(followingRequest);
//            return ResponseEntity.ok(new Message(
//                    "Ok",
//                    "friend request " + (accept ? "accepted" : "refused")
//            ));
//        }).orElseGet(() -> {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
//                            "Error",
//                            "Request not found"
//                    )
//            );
//        });
//    }
//
//
//   


    @GetMapping("/test/test/{notId}")
    public ResponseEntity test(@PathVariable (value = "notId") long notId) {

        Item item = itemRepository.findItemById(notId);
        notifiyBuyer(item);

        return ResponseEntity.ok(null);

    }


}