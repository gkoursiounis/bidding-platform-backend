package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.models.*;
import com.Auctions.backEnd.repositories.*;
import com.Auctions.backEnd.responses.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BidRepository bidRepository;
    private final BaseController baseController;

    @Autowired
    public UserController(UserRepository userRepository, ItemRepository itemRepository,
                          BidRepository bidRepository, BaseController baseController){
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.bidRepository = bidRepository;
        this.baseController = baseController;
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

        User requester = baseController.requestUser();

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
    @GetMapping("/myAuctions")
    public ResponseEntity getMyAuctions() {

        User requester = baseController.requestUser();
        return ResponseEntity.ok(requester.getItems());
    }


    /**
     * User can get a list of the bids he has made
     *
     * @return list of bids
     */
    @GetMapping("/myBids")
    public ResponseEntity getMyBids() {

        User requester = baseController.requestUser();
        return ResponseEntity.ok(requester.getBids());
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



}