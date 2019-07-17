package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.models.*;
import com.Auctions.backEnd.repositories.*;
import com.Auctions.backEnd.responses.BidRes;
import com.Auctions.backEnd.responses.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/user")
public class UserController extends BaseController {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BidRepository bidRepository;

    @Autowired
    public UserController(UserRepository userRepository, ItemRepository itemRepository,
                          BidRepository bidRepository){
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.bidRepository = bidRepository;
    }


    /**
     * User can get the details of a user
     *
     * @param username
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
     * User can get a list of the items displayed in his auctions
     *
     * @return list of auctions
     */
    @GetMapping("/getAuctions")
    public ResponseEntity getMyAuctions() {

        User requester = requestUser();
        return ResponseEntity.ok(requester.getItems());
    }


    /**
     * User can participate to an auction making a bid
     *
     * @param offer
     * @return created bid
     */
    @GetMapping("/makeBid/{itemId}")
    public ResponseEntity makeBid(@PathVariable (value = "itemId") long itemId,
                                         @RequestParam Double offer) {

        User requester = requestUser();

        Item item = itemRepository.findItemById(itemId);
        if (item == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
                    "Error",
                    "Item not found. Invalid item Id"
            ));
        }

        if(item.isAuctionCompleted()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "Auction has been completed and no bids can be made"
            ));
        }

        //In case there are bids: (offer, item.getCurrently()) <= 0
        //Tn case there are no bids: (offer, item.getCurrently()) < 0
        //In case offer = getCurrently = firstBid = BuyPrice
        if((!item.getBids().isEmpty() && java.lang.Double.compare(offer, item.getCurrently()) <= 0) ||
                (item.getBids().isEmpty() && java.lang.Double.compare(offer, item.getFirstBid()) < 0)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "Offer cannot be less than the current best offer or the initial price"
            ));
        }

        item.setCurrently(offer);
        if(java.lang.Double.compare(item.getBuyPrice(), offer) >= 0){
            item.setAuctionCompleted(true);
        }
        itemRepository.save(item);

        //??????
        item.getSeller().getItems().add(item);
        userRepository.save(item.getSeller());

        Bid bid = new Bid();
        bid.setBidder(requester);
        bid.setItem(item);
        bid.setOffer(offer);
        bidRepository.save(bid);

        requester.getBids().add(bid);
        userRepository.save(requester);

        return ResponseEntity.ok(new BidRes(bid, item.isAuctionCompleted()));
    }



//    @GetMapping("/search")
//    public ResponseEntity getPartialMatchedUsers(@RequestParam String name) {
//        User requester = requestUser();
//        List<FormattedUser> res = new ArrayList<>();

//        if ("".equals(name)) {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
//                        "Error",
//                        "Invalid name."
//                ));
//        }

   //     name = name.toLowerCase();

//        userRepository.searchUsers(name).forEach(user -> {
//            if (user != requester && !user.getBlocked().contains(requester) && !requester.getBlocked().contains(user)) {
//                user.setFollowed(requester.getFollowing().contains(user));
//                res.add(new FormattedUser(user));
//            }
//        });

//        Collections.sort(res);
//        return ResponseEntity.ok(res);
//    }

//    @GetMapping("/following-requests")
//    public ResponseEntity getFollowingRequests() {
//        User currentUser = requestUser();
//
//        return ResponseEntity.ok(currentUser.getFollowingRequests());
//    }

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
//    @PostMapping("/postAuction/")
//    public ResponseEntity followUser(@RequestBody CreateItem newItem) {
//
//        User requestUser = requestUser();
//
//        if (!requestUser.getAccount().isVerified()) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
//                    "Error",
//                    "You cannot make any auctions until your are verified by the admin"
//            ));
//        }
//
//
//
//        User followed = userRepository.findByAccount_UserName(username);
//        if (followed == null || requestUser.getBlocked().contains(followed) || followed.getBlocked().contains(requestUser)) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
//        }
//
//        if (followed.isPrivateProfile()){
//
//            if (followingRequestRepository.findByUserAndFollower(followed, requestUser) != null) {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
//                        "Error",
//                        "request already sent"
//                ));
//            }
//
//            FollowingRequest followingRequest =  new FollowingRequest();
//            followingRequest.setFollower(requestUser);
//            followingRequest.setUser(followed);
//            followingRequest = followingRequestRepository.save(followingRequest);
//
//            followed.getFollowingRequests().add(followingRequest);
//            userRepository.save(followed);
//
//            return ResponseEntity.status(HttpStatus.OK).body(new Message(
//                    "Ok",
//                    followed.getFirstName() + " has received a follow request from " + requestUser.getFirstName()
//            ));
//
//        } else {
//            // requestUser.getFollowing().add(followed);
//            // followed = userRepository.save(followed);
//
//            followRepository.save(new Follow(requestUser, followed));
//
//            Set<User> followers = new HashSet<>(requestUser.getFollowers());
//
//            for (User follower: followers) {
//                if (follower != followed) {
//                    this.notificationRepository.save(
//                            new FollowNotification(
//                                    requestUser,
//                                    follower,
//                                    requestUser,
//                                    followed,
//                                    true
//                            )
//                    );
//                }
//            }
//
//            notificationRepository.save(
//                    new FollowNotification(
//                            requestUser,
//                            followed,
//                            requestUser,
//                            followed,
//                            false
//                    )
//            );
//
//            return ResponseEntity.ok(new Message(
//                    "Ok",
//                    requestUser.getFirstName() + " follows " + followed.getFirstName()
//            ));
//        }
//    }
//
//    @DeleteMapping("/postAuction/{userName}")
//    public ResponseEntity unfollowUser(@PathVariable String userName) {
//        User requestUser = requestUser();
//        User followed = userRepository.findByAccount_UserName(userName);
//
//        if (followed == null || requestUser.getBlocked().contains(followed)||
//                followed.getBlocked().contains(requestUser)){
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
//                    "Error",
//                    "User not found!"
//            ));
//        }
//        else if (!requestUser.getFollowing().contains(followed)) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
//                    "Error",
//                    "You can not unfollow someone you don't follow."
//            ));
//        }
//
//        // followed.getFollowing().remove(followed);
//        // followed.getFollowers().remove(requestUser);
//
//        followRepository.removeByFollowerAndFollowing(requestUser, followed);
//
//        followed = userRepository.findUserById(followed.getId());
//        requestUser = userRepository.findUserById(requestUser.getId());
//
//        Set<Notification> notifiedActivities = new HashSet<>(requestUser.getGeneratedActivities());
//
//
//        User finalFollowed = followed;
//        notifiedActivities.removeIf((Notification notification) -> (
//                !(notification instanceof FollowNotification) && !notification.isActivity()
//                || !finalFollowed.equals(notification.getReceiver())
//                        && notification instanceof PostNotification
//                        && finalFollowed.equals(((PostNotification) notification).getPost().getUser())
//        ));
//
//
//        requestUser.getGeneratedActivities().removeAll(notifiedActivities);
//        followed.getReceivedActivities().removeAll(notifiedActivities);
//
//        requestUser.getFollowers().forEach(follower -> {
//            follower.getReceivedActivities().removeAll(notifiedActivities);
//            userRepository.save(follower);
//        });
//
//        // Gary addition for deleting activities
//        for (Notification activity : notificationRepository.findAllBySenderAndReceiver(followed, requestUser)){
//            notificationRepository.delete(activity);
//        }
//
//        userRepository.save(requestUser);
//        userRepository.save(followed);
//        notificationRepository.deleteAll(notifiedActivities);
//
//        return ResponseEntity.ok(new Message(
//                "Ok",
//                "User unfollowed."
//        ));
//    }
//
//    @GetMapping("getFollowers/{userName}")
//    public ResponseEntity getFolowers(@PathVariable String userName) {
//        User requester = requestUser();
//        User user = userRepository.findByAccount_UserName(userName);
//
//        if (user == null || requester.getBlocked().contains(user)||
//                user.getBlocked().contains(requester)){
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
//                    "Error",
//                    "User not found."
//            ));
//        }
//
//        Set<User> followers = new HashSet<>(user.getFollowers());
//        Set<FormattedUser> res = new HashSet<>();
//        if (!user.isPrivateProfile() || user.equals(requestUser()) || followers.contains(requestUser())) {
//
//            //remove followers which have blocked or have been blocked by the requester
//            rmvFollower(requester, user);
//
//            followers.forEach(follower -> {
//                follower.setFollowed(requester.getFollowing().contains(follower));
//                follower.setRequestSent(followingRequestRepository.findByUserAndFollower(follower, requester) != null);
//                res.add(new FormattedUser(follower));
//            });
//            return ResponseEntity.ok(res);
//        }
//
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message(
//                "Error",
//                "You must follow the user."
//        ));
//    }
//
//    @GetMapping("getFollowing/{userName}")
//    public ResponseEntity getFollowing(@PathVariable(value = "userName") String userName) {
//        User requester = requestUser();
//        User user = userRepository.findByAccount_UserName(userName);
//
//        if (user == null || requester.getBlocked().contains(user)||
//                user.getBlocked().contains(requester)){
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
//                    "Error",
//                    "User not found."
//            ));
//        }
//
//        Set<User> followings = new HashSet<>(user.getFollowing());
//        Set<FormattedUser> res = new HashSet<>();
//        if (!user.isPrivateProfile() || user.equals(requestUser()) || user.getFollowers().contains(requester)) {
//
//            rmvFollowings(requester, user);
//
//            followings.forEach(followed -> {
//                followed.setFollowed(requester.getFollowing().contains(followed));
//                followed.setRequestSent(followingRequestRepository.findByUserAndFollower(followed, requester) != null);
//                res.add(new FormattedUser(followed));
//            });
//            return ResponseEntity.ok(res);
//        }
//
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message(
//                "Error",
//                "you must follow the user"
//        ));
//    }
//
//    @GetMapping("/activities")
//    public ResponseEntity getActivities() {
//        User user = requestUser();
//
//        List<Notification> notifications = user.getRecentReceivedActivities();
//        List<Notification> activities = new ArrayList<>(notifications);
//
//        notifications.removeIf(activity -> activity.isActivity() || activity.isSeen());
//        activities.removeIf(activity -> !activity.isActivity() || activity.isSeen());
//
//        return ResponseEntity.ok(new ActivityRes(
//                notifications.size(),
//                activities.size(),
//                user.getFollowingRequests().size()
//            )
//        );
//    }
//
//    @GetMapping("/activities/notifications")
//    public ResponseEntity getNotifications() {
//        User user = requestUser();
//
//        List<Notification> notifications = user.getRecentReceivedActivities();
//        notifications.removeIf(activity -> activity.isActivity());
//
//        List<Notification> res = new ArrayList<>();
//        notifications.forEach(notification -> {
//            res.add(notification);
//            notification.setSeen(true);
//            notificationRepository.save(notification);
//        });
//
//        return ResponseEntity.ok(res);
//    }
//
//    @GetMapping("/activities/following")
//    public ResponseEntity getFollowingActivities() {
//        User user = requestUser();
//
//        List<Notification> activities = user.getRecentReceivedActivities();
//        activities.removeIf(activity -> !activity.isActivity());
//
//        List<Notification> res = new ArrayList<>();
//        activities.forEach(notification -> {
//            res.add((Notification) notification.clone());
//            notification.setSeen(true);
//            notificationRepository.save(notification);
//        });
//
//        return ResponseEntity.ok(res);
//    }
//
//    @PutMapping("/updateDetails")
//    public ResponseEntity updateUser(@RequestParam(value = "media", required = false) MultipartFile media,
//                                     @RequestParam(value = "bio", required = false) String bio,
//                                     @RequestParam(value = "privateProfile", required = false) boolean privateProfile) {
//        User requestUser = requestUser();
//        String imgPath = "";
//        Set<User> followers = new HashSet<>(requestUser.getFollowers());
//
//        if (!privateProfile) {
//            Set<FollowingRequest> requests = new HashSet<>(requestUser.getFollowingRequests());
//            for (FollowingRequest followingRequest : requests) {
//                // User follower = followingRequest.getFollower();
//                // requestUser.getFollowers().add(follower);
//                // requestUser = userRepository.save(requestUser);
//                User follower = followingRequest.getFollower();
//                this.followRepository.save(
//                        new Follow(follower, requestUser)
//                );
//
//                Set<User> followFollowers = new HashSet<>(follower.getFollowers());
//
//                for (User followFollower : followFollowers) {
//                    if (follower != requestUser) {
//                        this.notificationRepository.save(
//                                new FollowNotification(
//                                        follower,
//                                        followFollower,
//                                        follower,
//                                        requestUser,
//                                        true
//                                )
//                        );
//                    }
//                }
//
//
//                requestUser.getFollowingRequests().remove(followingRequest);
//                this.notificationRepository.save(
//                        new FollowNotification(
//                                follower,
//                                requestUser,
//                                follower,
//                                requestUser,
//                                false
//                        )
//                );
//            }
//            followingRequestRepository.deleteAll(requests);
//        }
//
//        if (media != null){
//            Post post = new Post();
//            DBFile dbFile = dBFileStorageService.storeFile(media);
//            // dbFile.setDownloadLink("/downloadFile/" + dbFile.getId());
//            dbFile.setDownloadLink("/downloadFile/" + dbFile.getId() + "." + dbFile.getFileType().split("/")[1]);
//            dbFile = dbFileRepository.save(dbFile);
//
//            post.setMedia(dbFile);
//            post.setTitle("User profile image updated");
//            post.setCaption("My new profile image!");
//            post.setUser(requestUser);
//            post.setPublicationDate(new java.sql.Date(System.currentTimeMillis()));
//            post = postRepository.save(post);
//            requestUser.getPosts().add(post);
//            requestUser = userRepository.save(requestUser);
//
//            for (User follower: followers) {
//                this.notificationRepository.save(
//                        new PostNotification(
//                                requestUser,
//                                follower,
//                                post
//                        )
//                );
//            }
//
//            imgPath = dbFile.getDownloadLink();
//        }
//
//        updateUser(requestUser, new User(bio, imgPath, privateProfile));
//        return ResponseEntity.ok(userRepository.save(requestUser));
//    }
//
//
//    /*
//     * Block user with the specified id.
//     *
//     * @Param userId of the user we are blocking
//     * @Return the blockedUser with the blockedBy set updated
//     */
//    @PatchMapping("/block/{userId}")
//    public ResponseEntity blockUser(@PathVariable long userId){
//        User currentUser = requestUser();
//
//        return userRepository.findById(userId).map( blockedUser ->{
//
//            if (currentUser.getBlockedBy().contains(blockedUser)){
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
//                        "Error",
//                        "User not found!"
//                ));
//            }
//
//            else if (currentUser.getUsername().equals(blockedUser.getUsername())){
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
//                        "Error",
//                        "You can not block yourself."
//                ));
//            }
//
//            else if (currentUser.blocked.contains(blockedUser)){
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
//                        "Error",
//                        "You already blocked this user."
//                ));
//            }
//
//            currentUser.getBlocked().add(blockedUser);
//            blockedUser.getBlockedBy().add(currentUser);
//
//            //currentUser is following the blockedUser
//            if (currentUser.getFollowing().contains(blockedUser)){
//                // currentUser.getFollowing().remove(blockedUser);
//                // blockedUser.getFollowers().remove(currentUser);
//                this.followRepository.removeByFollowerAndFollowing(currentUser, blockedUser);
//            }
//
//            //currentUser is followed by blockedUser
//            if (currentUser.getFollowers().contains(blockedUser)){
////                currentUser.getFollowers().remove(blockedUser);
////                blockedUser.getFollowing().remove(currentUser);
//                this.followRepository.removeByFollowerAndFollowing(blockedUser, currentUser);
//
//            }
//
//            // Check if a friendlist of the current user contains this deleted follower
//            for (FriendList friendlist : friendListRepository.findAllByOwner(currentUser)){
//                friendlist.getMembers().remove(blockedUser);
//                friendListRepository.save(friendlist);
//            }
//
//            // Check if a friendlist of the follower contains the current user
//            for (FriendList friendlist : friendListRepository.findAllByOwner(blockedUser)){
//                friendlist.getMembers().remove(currentUser);
//                friendListRepository.save(friendlist);
//            }
//
//
//            for (FollowingRequest followreq : currentUser.getFollowingRequests()){
//                if (followreq.getFollower().getUsername().equals(blockedUser.getUsername())){
//                    currentUser.getFollowingRequests().remove(followreq);
//                    followingRequestRepository.delete(followreq);
//                }
//            }
//
//            for(FollowingRequest followreq : blockedUser.getFollowingRequests()){
//                if (followreq.getFollower().getUsername().equals(currentUser.getUsername())){
//                    blockedUser.getFollowingRequests().remove(followreq);
//                    followingRequestRepository.delete(followreq);
//                }
//            }
//
//            //If I re-posted a post belonging to the blockedUser or the
//            //blockedUser has re-posted one of my posts the re-posts should be deleted.
//            deleteRepostBlock(currentUser.getPosts(), blockedUser);
//            deleteRepostBlock(blockedUser.getPosts(), currentUser);
//
//            userRepository.save(blockedUser);
//            userRepository.save(currentUser);
//
//            return ResponseEntity.ok(blockedUser);
//
//        }).orElseGet(() -> {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body( new Message(
//                    "Error",
//                    "User not found!"
//            ));
//        });
//    }
//
//    /**
//     * Unblock user with the specified id.
//     *
//     * @param userId of the user we are blocking
//     * @return the blockedUser with the blockedBy set updated
//     */
//    @PatchMapping("/unblock/{userId}")
//    public ResponseEntity unblockUser(@PathVariable long userId){
//        User currentUser = requestUser();
//
//        return userRepository.findById(userId).map( blockedUser -> {
//
//            if (currentUser.getBlocked().contains(blockedUser)){
//                currentUser.getBlocked().remove(blockedUser);
//                blockedUser.getBlockedBy().remove(currentUser);
//
//                userRepository.save(blockedUser);
//                userRepository.save(currentUser);
//
//                return ResponseEntity.ok("User was unblocked!");
//            }
//
//            // else bad request
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
//                    "Error",
//                    "You can not unblock a non-blocked user."
//            ));
//        }).orElseGet(() -> {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
//                    "Error",
//                    "User not found!"
//            ));
//        });
//    }
//
//    @GetMapping("/blocked")
//    public ResponseEntity getMyBlockedUser(){
//        User currentUser = requestUser();
//        return ResponseEntity.ok(currentUser.getBlocked());
//    }
//
//    /*
//     * User follows hashtag functionality
//     */
//
//    @GetMapping("/hashtag")
//    public ResponseEntity getHashtagsFollowed(){
//        User user = requestUser();
//
//        return ResponseEntity.ok(hashTagRepository.findAllByFollower(user));
//
//    }
//
//    @PostMapping("/hashtag/{tag}")
//    public ResponseEntity followHashTag(@PathVariable String tag){
//
//        User user = requestUser();
//
//        final String tagWithHash = "#" + tag;
//        HashTag hashTag = hashTagRepository.findSpecificHashtagByTag(tagWithHash);
//
//        if (hashTag == null){
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
//                    "Error",
//                    "The hash tag does not exist."
//            ));
//        }
//
//        hashTag.getFollower().add(user);
//        HashTag savedHashTag =  hashTagRepository.save(hashTag);
//
//        return ResponseEntity.ok(savedHashTag);
//
//    }
//
//    @DeleteMapping("/hashtag/{tag}")
//    public ResponseEntity deleteFollowedHashtag(@PathVariable String tag){
//        User user = requestUser();
//
//        final String tagWithHash = "#" + tag;
//        HashTag hashtag = hashTagRepository.findSpecificHashtagByTag(tagWithHash);
//
//        if (hashtag != null && hashTagRepository.findAllByFollower(user).contains(hashtag)){
//            hashtag.getFollower().remove(user);
//            HashTag savedHashTag =  hashTagRepository.save(hashtag);
//            return ResponseEntity.ok(savedHashTag);
//        } else {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
//                    "Error",
//                    "The hash tag does not exist."
//            ));
//        }
//    }
//
//    /**
//     * @author Gary
//     * @return The friends recommendations
//     */
//    @GetMapping("/friends/recommendations")
//    public ResponseEntity getRecommendedFriends(){
//        User user = requestUser();
//
//        List<FriendRecommendation> recommendation = new ArrayList<>();
//
//        // Intersection
//        Set<User> friends = user.getFollowers();
//        friends.retainAll(user.getFollowing());
//
//        for (User friend : friends){
//
//            // Intersection
//            Set<User> friendsOfFriend = friend.getFollowers();
//            friendsOfFriend.retainAll(friend.getFollowing());
//
//            for (User friendOfFriend : friendsOfFriend){
//                if (!friends.contains(friendOfFriend)
//                        && friendOfFriend != user
//                        && !user.getBlocked().contains(friendOfFriend)
//                        && !friendOfFriend.getBlocked().contains(user)){
//
//                    // Intersection
//                    Set<User> friendsOfFriendsOfFriends = new HashSet<>(friendOfFriend.getFollowers()); // use the copy constructor
//                    friendsOfFriendsOfFriends.retainAll(friendOfFriend.getFollowing());
//
//                    Set<User> friendsInCommon = new HashSet<>(friendsOfFriendsOfFriends); // use the copy constructor
//                    friendsInCommon.retainAll(friends);
//
//                    FriendRecommendation friendRecommendation = new FriendRecommendation();
//                    friendRecommendation.setFriendsInCommon(friendsInCommon.size());
//                    friendRecommendation.setUser(friendOfFriend);
//
//                    recommendation.add(friendRecommendation);
//                }
//            }
//        }
//
//        HashMap<User, Integer> seen = new HashMap<>();
//        List<FriendRecommendation> uniqueRecommendation = new ArrayList<>();
//        for (FriendRecommendation friendRecommendation : recommendation){
//
//            if (!seen.containsKey(friendRecommendation.getUser())){
//                uniqueRecommendation.add(friendRecommendation);
//                seen.put(friendRecommendation.getUser(), 1);
//            }
//
//
//        }
//
//        // recommendation = recommendation.stream().distinct().collect(Collectors.toList());
//        Collections.sort(uniqueRecommendation, Comparator.comparingInt(FriendRecommendation::getFriendsInCommon).reversed());
//        uniqueRecommendation.forEach(rec -> rec.getUser().setFollowed(false));
//        return ResponseEntity.ok(uniqueRecommendation);
//    }
//
//    /**
//     * BFS algorithm
//     * @author Gary
//     * @param username1 - The username
//     * @return Shortest path(s)
//     */
//    @GetMapping("/friends/path/{username1}")
//    public ResponseEntity getShortestPath(@PathVariable String username1){
//
//        User sourceUser = requestUser();
//        User destUser = userRepository.findByAccount_UserName(username1);
//
//        if (destUser == null || sourceUser.getBlocked().contains(destUser) || destUser.getBlocked().contains(sourceUser)){
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
//                    "Error",
//                    "User not found"
//            ));
//        }
//
//
//        // Keep track of the previous node of each node
//        // In order to construct the linked list when the path
//        // has been found
//        HashMap<User, List<User>> previousNode = new HashMap<>();
//        previousNode.put(sourceUser, null);
//
//        // Put user in this set if he has been visited
//        // Here, user is a node
//        Set<User> visited = new HashSet<>();
//        visited.add(sourceUser);
//
//        // For the first time we have to put
//        // the previous user in the previousNode hashMap
//        int level = 0;
//        boolean finish = false;
//
//        // The paths and one path
//        List<LinkedList<User>> paths = new ArrayList<>();
//        List<LinkedList<User>> tempPaths = new ArrayList<>();
//        LinkedList<User> path;
//        boolean blockedUser = false;
//
//
//        if ((sourceUser.getFollowers().contains(destUser) && sourceUser.getFollowing().contains(destUser))){
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
//                    "Error",
//                    "The current user did not exist or is one of your friend already"
//            ));
//        }
//
//        Set<User> connections = new HashSet<>(sourceUser.getFollowing());
//        connections.retainAll(sourceUser.getFollowers());
//
//        while (!connections.isEmpty()){
//
//            Set<User> connectionsCopy = new HashSet<>(connections);
//            connections.clear();
//
//            level++;
//
//            for (User user1 : connectionsCopy){
//                Set<User> i1 = new HashSet<>(user1.getFollowers());
//                i1.retainAll(user1.getFollowing());
//
//                if (level == 1){
//                    List<User> users = new ArrayList<>();
//                    users.add(sourceUser);
//                    previousNode.put(user1, users);
//                }
//
//                for (User user2 : i1){
//
//                    if(!visited.contains(user2)){
//                        connections.add(user2);
//
//                        if (previousNode.get(user2)!=null){
//                            previousNode.get(user2).add(user1);
//                        } else {
//                            List<User> users = new ArrayList<>();
//                            users.add(user1);
//                            previousNode.put(user2, users);
//                        }
//
//                    }
//
//                    if (user2 == destUser){
//                        finish = true;
//                    }
//
//                }
//
//                visited.add(user1);
//            }
//
//            if (finish){ break;}
//        }
//
//        if (finish) {
//
//            List<User> previous = previousNode.get(destUser);
//            boolean first = true;
//            boolean done = false;
//
//            while (!done) {
//
//                if (first){
//
//                    for (User user : previous){
//                        path = new LinkedList<>();
//                        path.addFirst(destUser);
//                        path.addFirst(user);
//                        paths.add(path);
//                    }
//
//                    first = false;
//
//                } else {
//
//                    for (LinkedList<User> path0 : paths){
//
//                        previous = previousNode.get(path0.getFirst());
//
//                        if (previous.size() > 1){
//
//                            for (int i = 1; i < previous.size(); i++){
//                                LinkedList<User> path1 = (LinkedList<User>) path0.clone();
//                                path1.addFirst(previous.get(i));
//                                tempPaths.add(path1);
//                            }
//
//                            path0.addFirst(previous.get(0));
//
//                        } else {
//                            path0.addFirst(previous.get(0));
//                        }
//
//                    }
//
//                    paths.addAll(tempPaths);
//                    tempPaths.clear();
//                }
//
//                if (previousNode.get(paths.get(0).getFirst()) == null){
//                    done = true;
//                }
//            }
//        }
//
//        List<LinkedList<User>> pathsToRemove = new ArrayList<>();
//        for (LinkedList<User> path0 : paths){
//            for (User user : path0){
//                if (user.getBlocked().contains(sourceUser)|| sourceUser.getBlocked().contains(user)){
//                    pathsToRemove.add(path0);
//                }
//            }
//        }
//        paths.removeAll(pathsToRemove);
//
//        return ResponseEntity.ok(paths);
//    }
//
//
//    /* Helper functions */
//    private void updateUser(User oldUser, User newUser) {
//        if (newUser.getBio() != null) {
//            oldUser.setBio(newUser.getBio());
//        }
//
//        if (!"".equals(newUser.getImgPath())) {
//            oldUser.setImgPath(newUser.getImgPath());
//        }
//
//        //if author of a post, which has been re-posted,
//        //become private re-posts must be deleted
//        oldUser.setPrivateProfile(newUser.isPrivateProfile());
//        if(oldUser.isPrivateProfile()){
//            deleteReposts2(oldUser);
//        }
//    }
//
//    private void deleteReposts2(User user){
//        List<Post> reposts = new ArrayList<>();
//        for (Post post : user.getPosts()){
//            reposts.addAll(postRepository.getAllRepost(post));
//        }
//        reposts.removeIf(repost -> repost.getUser() == user);
//        deleteReposts(reposts);
//    }
//
//    private void deleteRepostBlock(Set<Post> posts, User other){
//        List<Post> tempList = new ArrayList<>();
//        for (Post post : posts){
//            if (post.getInnerPost() != null){
//                if (post.getInnerPost().getUser().equals(other)){
//                    tempList.add(post);
//                }
//            }
//        }
//        if(!tempList.isEmpty()){
//            deleteReposts(tempList);
//        }
//    }
//
//    private void rmvFollower(User currentUser, User otherUser){
//        otherUser.getFollowers().removeIf(follower ->
//                currentUser.getBlocked().contains(follower) ||
//                        follower.getBlocked().contains(currentUser)
//        );
//    }
//
//    private void rmvFollowings(User currentUser, User otherUser){
//        otherUser.getFollowing().removeIf(following ->
//                currentUser.getBlocked().contains(following) ||
//                following.getBlocked().contains(currentUser)
//        );
//    }
//


}