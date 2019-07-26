package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.models.*;
import com.Auctions.backEnd.repositories.*;
import com.Auctions.backEnd.responses.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
     * A user can get the details
     * of a user (himself or another user)
     *
     * @param username - username of the user
     * @return user details
     */
    @GetMapping("/{username}")
    public ResponseEntity getUserDetails(@PathVariable String username) {

        User requester = requestUser();

        User user = userRepository.findByAccount_Username(username);

        if (user == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
                    "Error",
                    "User not found!"
            ));
        }

        if (requester.getUsername().equals(username)) {
            return ResponseEntity.ok(requester);
        }

        return ResponseEntity.ok(user);
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

        User requester = requestUser();
        return ResponseEntity.ok(requester.getNotifications());
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



    @GetMapping("/test/test")
    public ResponseEntity test() {
        System.err.println("THIS IS HERE");
        return ResponseEntity.ok("hello  ");
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


}