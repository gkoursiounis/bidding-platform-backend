package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.BackEndApplication;
import com.Auctions.backEnd.models.Account;
import com.Auctions.backEnd.repositories.AccountRepository;
import com.Auctions.backEnd.repositories.UserRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;


import static com.Auctions.backEnd.TestUtils.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = {com.Auctions.backEnd.configs.TestConfig.class, BackEndApplication.class})
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private com.Auctions.backEnd.TestUtils testUtils;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;


    private String user1;
    private String user2;
    private String user3;

    @BeforeEach
    private void before() throws Exception {

        testUtils.clearDB();


        user1 = createAccount(mvc, "user1", "myPwd123", "FirstName1", "LastName1", "email1@usi.ch");
        user2 = createAccount(mvc, "user2", "myPwd123", "FirstName2", "LastName2", "email2@usi.ch");
        user3 = createAccount(mvc, "user3", "myPwd123", "FirstName3", "LastName3", "email3@usi.ch");
    }

    @AfterEach
    private void after() {
        testUtils.clearDB();
    }

    private void makeAdmin(final String username){

        Account user = accountRepository.findByUsername(username);
        assertNotNull(user);
        user.setAdmin(true);
        accountRepository.save(user);
    }



    /**
     * Successful get user
     *
     * @throws Exception
     */
    @Test
    @DisplayName("Successful get user")
    public void getUser1() throws Exception {
        getUser(mvc, user1, "user2")
                .andExpect(status().isOk());

    }


    /**
     * User tries to get user details from a user who does not exist
     * We should get back an HTTP <code>NOT FOUND</code>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User doesn't exist")
    public void getUser2() throws Exception {
        getUser(mvc, user1, "user0")
                .andExpect(status().isNotFound());

    }


    /**
     * Check for correct results when user details requested
     *
     * @throws Exception
     */
    @Test
    @DisplayName("User is the requested User")
    public void getUser3() throws Exception {
        getUser(mvc, user3, "user3")
                .andExpect(status().isOk());

    }


    /**
     * User tries to get user details from a user he has blocked
     * We should get back an HTTP <code>NOT FOUND</code>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User gets user details from blocked user")
    public void getUserDetails1() throws Exception {

        //blockUser(mvc, user2, getUserToString(mvc, user2, "user1")).andExpect(status().isOk());

        getUser(mvc, user2, "user1")
                .andExpect(status().isNotFound());
    }


    /**
     * User tries to get user details from a user he is blocked by
     * We should get back an HTTP <code>NOT FOUND</code>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User gets user details from user he is blocked by")
    public void getUserDetails2() throws Exception {

       // blockUser(mvc, user1, getUserToString(mvc, user1, "user2")).andExpect(status().isOk());

        getUser(mvc, user2, "user1")
                .andExpect(status().isNotFound());
    }


    /**
     * User tries to get user details a non related user
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User gets user details from user he is blocked by")
    public void getUserDetails3() throws Exception {
        getUser(mvc, user1, "user2")
                .andExpect(jsonPath("requestSent", is(false)))
                .andExpect(status().isOk());
    }
    

    /**
     * User follows another user
     * So the latter should receive a following request
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Successful getFollowingRequest")
    public void getFollowingRequests() throws Exception {

       // makePrivate(mvc, user2, true);

       // follow(mvc, user1, "user2").andExpect(status().isOk());

        mvc.perform(get("/user/following-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user2))
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(status().isOk());
    }


    /**
     * User tries to accept/reject following request with invalid Id
     * We should get back an HTTP <code>NOT FOUND</code>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User accepts/rejects following request with invalid Id")
    public void acceptOrRejectFollowingRequest1() throws Exception {

        mvc.perform(post("/user/following-request/1234")
                .param("accept", "true")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user2))
                .andExpect(status().isNotFound());
    }






    /**
     * User rejects a following request
     * After the operation the request is deleted
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User rejects a following request")
    public void acceptOrRejectFollowingRequest4() throws Exception {

     //   makePrivate(mvc, user2, true);

     //   follow(mvc, user1, "user2").andExpect(status().isOk());
//        mvc.perform(post("/user/following-request/" + getLastRequest(mvc, user2))
//                .param("accept", "false")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", user2))
//                .andExpect(status().isOk());

        mvc.perform(get("/user/following-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user2))
                .andExpect(jsonPath("$.*", hasSize(0)))
                .andExpect(status().isOk());
    }


    /**
     * User accepts a following request
     * New follower should be added in the follower list
     * After the operation the request is deleted
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Following request acceptance - follower list update")
    public void acceptOrRejectFollowingRequest5() throws Exception {

   //     makePrivate(mvc, user2, true);

    //    follow(mvc, user1, "user2").andExpect(status().isOk());
//
//        mvc.perform(post("/user/following-request/" + getLastRequest(mvc, user2))
//                .param("accept", "true")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", user2))
 //               .andExpect(status().isOk());

        mvc.perform(get("/user/getFollowers/user2")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user2))
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(status().isOk());

        mvc.perform(get("/user/following-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user2))
                .andExpect(jsonPath("$.*", hasSize(0)))
                .andExpect(status().isOk());
    }


    /**
     * User accepts a following request
     * Followers of the user should receive a notification
     *
     * @throws Exception - mvc.perform throws exception
     */
//    @Test
//    @DisplayName("Following request acceptance - follower notification")
//    public void acceptOrRejectFollowingRequest6() throws Exception {
//
//        follow(mvc, user1, "user2").andExpect(status().isOk());
//
//        makePrivate(mvc, user3, true);
//
//        follow(mvc, user2, "user3").andExpect(status().isOk());
//
//        mvc.perform(post("/user/following-request/" + getLastRequest(mvc, user3))
//                .param("accept", "true")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", user3))
//                .andExpect(status().isOk());
//
//        getActivities(mvc, user1)
//                .andExpect(jsonPath("activities", is(1)))
//                .andExpect(status().isOk());
//    }
//
//
//    /**
//     * User tries to accept/reject a following request with invalid token
//     * We should get back an HTTP <code>FORBIDDEN</code>
//     *
//     * @throws Exception - mvc.perform throws exception
//     */
//    @Test
//    @DisplayName("User accepts/rejects a following request with invalid token")
//    public void acceptOrRejectFollowingRequest7() throws Exception {
//
//        makePrivate(mvc, user3, true);
//
//        follow(mvc, user2, "user3").andExpect(status().isOk());
//
//        mvc.perform(post("/user/following-request/" + getLastRequest(mvc, user3))
//                .param("accept", "true")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "invalidToken"))
//                .andExpect(status().isForbidden());
//    }
//
//
//    /**
//     * User successfully accepts/rejects a following request
//     * If he already follows his follower then he should
//     * not receive a notification about the notification
//     *
//     * @throws Exception - mvc.perform throws exception
//     */
//    @Test
//    @DisplayName("User accepts/rejects followreq - check did not receive notification")
//    public void acceptOrRejectFollowingRequest8() throws Exception {
//
//        follow(mvc, user1, "user2").andExpect(status().isOk());
//
//        makePrivate(mvc, user1, true);
//
//        follow(mvc, user2, "user1").andExpect(status().isOk());
//
//        mvc.perform(post("/user/following-request/" + getLastRequest(mvc, user1))
//                .param("accept", "true")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", user1))
//                .andExpect(status().isOk());
//
//        mvc.perform(get("/user/activities/following")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", user1))
//                .andExpect(jsonPath("$.*", hasSize(0)))
//                .andExpect(status().isOk());
//    }


    /**
     * User tries to search for partial matching with invalid token
     * We should get back an HTTP <code>FORBIDDEN</code>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User searches for partial matching with invalid token")
    public void getPartialMatchedUsers1() throws Exception {

        mvc.perform(get("/user/search")
                .param("name", "user1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "invalidToken"))
                .andExpect(status().isForbidden());
    }


    /**
     * User tries successfully to search for partial matching
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Successful partial matching")
    public void getPartialMatchedUsers2() throws Exception {

        mvc.perform(get("/user/search")
                .param("name", "user")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(jsonPath("$.*", hasSize(2)))
                .andExpect(status().isOk());
    }


    /**
     * User tries to search for partial matching with empty name
     * We should get back an HTTP <code>BAD REQUEST</code>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Partial matching with empty name")
    public void getPartialMatchedUsers3() throws Exception {

        mvc.perform(get("/user/search")
                .param("name", "")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isBadRequest());
    }


    /**
     * User tries to search for himself using partial matching
     * Nothing should be returned as result
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User tries to search for himself in partial matching")
    public void getPartialMatchedUsers4() throws Exception {

        mvc.perform(get("/user/search")
                .param("name", "user1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(jsonPath("$.*", hasSize(0)))
                .andExpect(status().isOk());
    }


    /**
     * User tries to search for a blocked user using partial matching
     * Nothing should be returned as result
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User searches for a blocked user in partial matching")
    public void getPartialMatchedUsers5() throws Exception {

      //  blockUser(mvc, user1, getAccountId(mvc, user2));

        mvc.perform(get("/user/search")
                .param("name", "user2")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(jsonPath("$.*", hasSize(0)))
                .andExpect(status().isOk());
    }


    /**
     * User tries to search for a user that he has
     * blocked the initial user
     * Nothing should be returned as result
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User searches blocked-by user")
    public void getPartialMatchedUsers6() throws Exception {

    //    blockUser(mvc, user2, getAccountId(mvc, user1));

        mvc.perform(get("/user/search")
                .param("name", "user2")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(jsonPath("$.*", hasSize(0)))
                .andExpect(status().isOk());
    }


    /**
     * User searches for keyword that matches with many usernames
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Get multiple partial match")
    public void getPartialMatchedUsers7() throws Exception {

        mvc.perform(get("/user/search")
                .param("name", "use")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(jsonPath("$.*", hasSize(2)))
                .andExpect(status().isOk());
    }


    /**
     * User tries to follow an invalid user
     * We should get back an HTTP <code>NOT FOUND</code>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User tries to follow an invalid user")
    public void followUser1() throws Exception {

        mvc.perform(post("/user/follows/invalidUser")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isNotFound());
    }


    /**
     * User tries to follow himself
     * We should get back an HTTP <code>BAD REQUEST</code>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User follows himself")
    public void followUser2() throws Exception {

       // follow(mvc, user1, "user1")
      //          .andExpect(status().isBadRequest());
    }


    /**
     * User tries to follow a user he has blocked
     * We should get back an HTTP <code>NOT FOUND</code>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User follows a blocked user")
    public void followUser3() throws Exception {

//        blockUser(mvc, user1, getUserToString(mvc, user1, "user2")).andExpect(status().isOk());
//
//        follow(mvc, user1, "user2")
//                .andExpect(status().isNotFound());
    }


    /**
     * User tries to follow a user who has blocked
     * the initial user
     * We should get back an HTTP <code>NOT FOUND</code>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User follows user who blocked the initial user")
    public void followUser4() throws Exception {
//        blockUser(mvc, user2, getUserToString(mvc, user2, "user1")).andExpect(status().isOk());
//
//        follow(mvc, user1, "user2")
//                .andExpect(status().isNotFound());

    }


    /**
     * User follows another public user
     * After the operation both users should have
     * updated their following and follower lists
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User follows public user - follow list update")
    public void followUser5() throws Exception {

//        follow(mvc, user1, "user2")
//                .andExpect(status().isOk());


        mvc.perform(get("/user/getFollowers/user2")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user2))
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(status().isOk());

        mvc.perform(get("/user/getFollowing/user1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(status().isOk());
    }


    /**
     * User follows another public user
     * After the operations the public user should
     * have received a notification and the followers
     * of the follower an notification notification
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User follows public user - follower notification")
    public void followUser6() throws Exception {
//
//        follow(mvc, user1, "user2")
//                .andExpect(status().isOk());
//
//        follow(mvc, user2, "user3")
//                .andExpect(status().isOk());

        getActivities(mvc, user1)
                .andExpect(jsonPath("activities", is(1)))
                .andExpect(status().isOk());

        mvc.perform(get("/user/activities/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user3))
                .andExpect(jsonPath("$[0].type", is("follow")))
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(status().isOk());
    }


    /**
     * User follows another private user and tries again
     * We should get back an HTTP <code>BAD REQUEST</code>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Multiple follow requests to private user")
    public void followUser7() throws Exception {

//        makePrivate(mvc, user2, true);
//
//        follow(mvc, user1, "user2")
//                .andExpect(status().isOk());
//
//        follow(mvc, user1, "user2")
//                .andExpect(status().isBadRequest());

    }


    /**
     * User follows another private user who should
     * have received a following request notification
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Check if follow request notification received")
    public void followUser8() throws Exception {

//        makePrivate(mvc, user2, true);
//
//        follow(mvc, user1, "user2")
//                .andExpect(status().isOk());

        mvc.perform(get("/user/following-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user2))
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(status().isOk());
    }


    /**
     * UserA follows userB
     * If userB already follows userA then he
     * should not receive an notification notification
     * but only a personal notification
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Following follows follower - no notification notification")
    public void followUser9() throws Exception {
//
//        follow(mvc, user1, "user2").andExpect(status().isOk());
//
//        follow(mvc, user2, "user1").andExpect(status().isOk());

        mvc.perform(get("/user/activities/following")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(jsonPath("$.*", hasSize(0)))
                .andExpect(status().isOk());
    }


    /**
     * An unverified user tries to follow another user
     * We should get back an HTTP <code>BAD REQUEST</code>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Following follows follower - no activity notification")
    public void followUser10() throws Exception {

        /* Not a legal way to check it but we want to avoid creating another route */
        Account account = accountRepository.findByUsername("user1");
        account.setVerified(false);
        accountRepository.save(account);

//        follow(mvc, user1, "user2")
//                .andExpect(status().isBadRequest());
    }


    /**
     * User tries to unfollow an invalid user
     * We should get back an HTTP <code>NOT FOUND</code>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User unfollows invalid user")
    public void unfollowUser1() throws Exception {

//        unfollow(mvc, user1, "invalidUser")
//                .andExpect(status().isNotFound());
    }







    /**
     * User tries to get followers of a non-existing user
     * We should get back an HTTP <code>NOT FOUND</code>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Get followers of non-existing user")
    public void getFolowers1() throws Exception {

        mvc.perform(get("/user/getFollowers/invalidUser")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user2))
                .andExpect(status().isNotFound());

    }




    /**
     * User tries to get followings of a non-existing user
     * We should get back an HTTP <code>NOT FOUND</code>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Get followers of non-existing user")
    public void getFollowingactivities1() throws Exception {

        mvc.perform(get("/user/getFollowing/invalidUser")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user2))
                .andExpect(status().isNotFound());

    }


    /**
     * User gets a list of blocked users with invalid token
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User gets a list of blocked users with invalid token")
    public void getMyBlockedUser2() throws Exception {

        mvc.perform(get("/user/blocked")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "invalidToken"))
                .andExpect(status().isForbidden());
    }


    /**
     * User makes his account private
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User makes his account private")
    public void updateDetails1() throws Exception {

        mvc.perform(put("/user/updateDetails")
                .param("privateProfile", "true")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk());

        getUser(mvc, user1, "user1")
                .andExpect(jsonPath("privateProfile", is(true)))
                .andExpect(status().isOk());
    }


    /**
     * User sets new biography with less than 500 characters
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("New bio less than 500 characters")
    public void updateDetails3() throws Exception {

        final String newBio = "this is my new bio";

        mvc.perform(put("/user/updateDetails")
                .param("bio", newBio)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk());

        getUser(mvc, user1, "user1")
                .andExpect(jsonPath("bio", is(newBio)))
                .andExpect(status().isOk());
    }


    /**
     * User sets new biography with more than 500 characters
     * We should get back an HTTP <code>BAD REQUEST</code>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("New bio more than 500 characters")
    public void updateDetails4() throws Exception {

        String newBio = "";

        for (int i = 0; i < 501; i++) {
            newBio += "n";
        }

        mvc.perform(put("/user/updateDetails")
                .param("bio", newBio)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isBadRequest());
    }



    /**
     * Valid user take the list of followers which is 0
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Get followers relative to a user with 0 followers")
    public void getFollowers1() throws Exception {

        mvc.perform(get("/user/getFollowers/user1")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(jsonPath("$.*", hasSize(0)))
                .andExpect(status().isOk());
    }



    @Test
    @DisplayName("Follow a tag that doesn't exist")
    public void followTags1() throws Exception {
        final String tag = "tag1";
        followTag(mvc, user1, tag).andExpect(status().isNotFound());
        getTags(mvc, user1)
                .andExpect(jsonPath("$.*", hasSize(0)))
                .andExpect(status().isOk());

        makePostWithCaption(mvc, user3, "#tag1").andExpect(status().isOk());

        mvc.perform(get("/feed")
                .header("Authorization", user1))
                .andExpect(jsonPath("$.*", hasSize(0)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Follow a tag that does exist")
    public void followTags2() throws Exception {
        makePostWithCaption(mvc, user2, "#tag1").andExpect(status().isOk());
        final String tag = "tag1";
        followTag(mvc, user1, tag).andExpect(status().isOk());
        getTags(mvc, user1)
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(status().isOk());

        makePostWithCaption(mvc, user3, "#tag1").andExpect(status().isOk());

        mvc.perform(get("/feed")
                .header("Authorization", user1))
                .andExpect(jsonPath("$.*", hasSize(2)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Get tags without having any")
    public void getTags1() throws Exception {

        getTags(mvc, user2)
                .andExpect(jsonPath("$.*", hasSize(0)))
                .andExpect(status().isOk());

    }

    @Test
    @DisplayName("Get tags when you have some")
    public void getTags2() throws Exception {

        final String tag1 = "tag1";
        final String tag2 = "tag2";
        final String tag3 = "tag3";
        final String tag4 = "tag4";
        final String caption = "#" + tag1 + " #" + tag2 + " #" + tag3 + " #" + tag4;

        makePostWithCaption(mvc, user2, caption).andExpect(status().isOk());

        assertEquals("#" + tag1, followTag2(mvc, user1, tag1));
        assertEquals("#" + tag2, followTag2(mvc, user1, tag2));
        assertEquals("#" + tag3, followTag2(mvc, user1, tag3));
        assertEquals("#" + tag4, followTag2(mvc, user1, tag4));

        getTags(mvc, user1)
                .andExpect(jsonPath("$.*", hasSize(4)))
                .andExpect(status().isOk());

    }

    @Test
    @DisplayName("Unfollow tags when you have some")
    public void stopFollowTag1() throws Exception {

        final String tag1 = "tag1";
        final String tag2 = "tag2";

        final String caption = "#" + tag1 + " #" + tag2;

        makePostWithCaption(mvc, user2, caption).andExpect(status().isOk());

        assertEquals("#" + tag1, followTag2(mvc, user3, tag1));
        assertEquals("#" + tag2, followTag2(mvc, user3, tag2));

        getTags(mvc, user3)
                .andExpect(jsonPath("$.*", hasSize(2)))
                .andExpect(status().isOk());

        assertEquals("#" + tag2, unfollowTag2(mvc, user3, tag2));

        getTags(mvc, user3)
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Unfollow tags when you don't have any")
    public void stopFollowTag2() throws Exception {

        final String tag1 = "tag1";

        getTags(mvc, user3)
                .andExpect(jsonPath("$.*", hasSize(0)))
                .andExpect(status().isOk());

        unfollowTag(mvc, user3, tag1).andExpect(status().isNotFound());

        getTags(mvc, user3)
                .andExpect(jsonPath("$.*", hasSize(0)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Unfollow a tag you don't have")
    public void stopFollowTag3() throws Exception {

        final String tag1 = "tag1";
        final String tag2 = "tag2";

        final String caption = "#" + tag1 + " #" + tag2;

        makePostWithCaption(mvc, user2, caption).andExpect(status().isOk());

        assertEquals("#" + tag1, followTag2(mvc, user3, tag1));

        getTags(mvc, user3)
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(status().isOk());

        unfollowTag(mvc, user3, tag2).andExpect(status().isNotFound());

        getTags(mvc, user3)
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(status().isOk());
    }

    /**
     * User gets his friend recommendations based on schema
     * user1 <--> user2 <--> user3
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User gets his friend recommendations - simple schema")
    public void getRecommendedFriends1() throws Exception{

        makeFriends(user1, "user1", user2, "user2");
        makeFriends(user2, "user2", user3, "user3");

        getRecommended(user1)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(jsonPath("$.[0].friendsInCommon", is(1)));
    }


    /**
     * User gets his friend recommendations based on schema
     * user1 <--> user2 <--> user3
     * user1 <--> user4 <--> user3
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User gets his friend recommendations - multiple schema")
    public void getRecommendedFriends2() throws Exception{

        String user4 = createAccount(mvc, "user4", "myPwd123", "FirstName4", "LastName4", "email4@usi.ch");

        makeFriends(user1, "user1", user2, "user2");
        makeFriends(user2, "user2", user3, "user3");

        makeFriends(user1, "user1", user4, "user4");
        makeFriends(user4, "user4", user3, "user3");


        getRecommended(user1)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(jsonPath("$[0].friendsInCommon", is(2)));
    }


    /**
     * User gets his friend recommendations based on schema
     * user1 <--> user2 <--> user3
     * user1 <--> user3
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Get friend recommendations - friend is also a friend of friend")
    public void getRecommendedFriends3() throws Exception{

        makeFriends(user1, "user1", user2, "user2");
        makeFriends(user2, "user2", user3, "user3");
        makeFriends(user1, "user1", user3, "user3");

        getRecommended(user1)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(0)));
    }


    /**
     * User gets his friend recommendations based on schema
     * user1 <--> user2 <--> user3
     * user1 <--> user4 <--> user3
     * user1 <--> user5 <--> user6
     * We should get the sorted list of users
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Get friend recommendations - recommendation order")
    public void getRecommendedFriends4() throws Exception{

        String user4 = createAccount(mvc, "user4", "myPwd123", "FirstName4", "LastName4", "email4@usi.ch");
        String user5 = createAccount(mvc, "user5", "myPwd123", "FirstName5", "LastName5", "email5@usi.ch");
        String user6 = createAccount(mvc, "user6", "myPwd123", "FirstName6", "LastName6", "email6@usi.ch");

        makeFriends(user1, "user1", user2, "user2");
        makeFriends(user2, "user2", user3, "user3");

        makeFriends(user1, "user1", user4, "user4");
        makeFriends(user4, "user4", user3, "user3");

        makeFriends(user1, "user1", user5, "user5");
        makeFriends(user5, "user5", user6, "user6");

        getRecommended(user1)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(2)))
                .andExpect(jsonPath("$[0].friendsInCommon", is(2)))
                .andExpect(jsonPath("$[1].friendsInCommon", is(1)));
    }


    /**
     * User gets his friend recommendations based on schema
     * user1 <--> user2 <--> user3
     * user1 <--> user4 <--> user3
     * user1 <--> user5 <--> user6
     * user1 <--> user7 <--> user8
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Get friend recommendations - recommendation order - same numbers")
    public void getRecommendedFriends5() throws Exception{

        String user4 = createAccount(mvc, "user4", "myPwd123", "FirstName4", "LastName4", "email4@usi.ch");
        String user5 = createAccount(mvc, "user5", "myPwd123", "FirstName5", "LastName5", "email5@usi.ch");
        String user6 = createAccount(mvc, "user6", "myPwd123", "FirstName6", "LastName6", "email6@usi.ch");
        String user7 = createAccount(mvc, "user7", "myPwd123", "FirstName7", "LastName7", "email7@usi.ch");
        String user8 = createAccount(mvc, "user8", "myPwd123", "FirstName8", "LastName8", "email8@usi.ch");

        makeFriends(user1, "user1", user2, "user2");
        makeFriends(user2, "user2", user3, "user3");

        makeFriends(user1, "user1", user4, "user4");
        makeFriends(user4, "user4", user3, "user3");

        makeFriends(user1, "user1", user5, "user5");
        makeFriends(user5, "user5", user6, "user6");

        makeFriends(user1, "user1", user7, "user7");
        makeFriends(user7, "user7", user8, "user8");

        getRecommended(user1)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(3)))
                .andExpect(jsonPath("$[0].friendsInCommon", is(2)))
                .andExpect(jsonPath("$[1].friendsInCommon", is(1)))
                .andExpect(jsonPath("$[2].friendsInCommon", is(1)));
    }





    /**
     * User finds shortest path to another user who does not exist
     * We should get back an HTTP <code>NOT FOUND</code>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Get shorest path - user does not exist")
    public void getShortestPath1() throws Exception{

        mvc.perform(get("/user/friends/path/invalidUser")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isNotFound());
    }


    /**
     * User finds shortest path to another user who is already friend with
     * We should get back an HTTP <code>NOT FOUND</code>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Get shorest path - user does not exist")
    public void getShortestPath2() throws Exception{

        makeFriends(user1, "user1", user2, "user2");

        mvc.perform(get("/user/friends/path/user2")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isNotFound());
    }


    /**
     * User finds shortest path to another user based on the schema
     * user1 <--> user2 <--> user3
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Get shorest path - simple schema")
    public void getShortestPath3() throws Exception{

        makeFriends(user1, "user1", user2, "user2");
        makeFriends(user2, "user2", user3, "user3");

        mvc.perform(get("/user/friends/path/user3")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].*", hasSize(3)))
                .andExpect(jsonPath("$[0].[0].userName", is("user1")))
                .andExpect(jsonPath("$[0].[1].userName", is("user2")))
                .andExpect(jsonPath("$[0].[2].userName", is("user3")));
    }


    /**
     * User finds shortest path to another user based on the schema
     * user1 <--> user2 <--> user5 <--> user3
     * user1 <--> user4 <--> user3
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Get shortest path - multiple schema")
    public void getShortestPath4() throws Exception{

        String user4 = createAccount(mvc, "user4", "myPwd123", "FirstName4", "LastName4", "email4@usi.ch");
        String user5 = createAccount(mvc, "user5", "myPwd123", "FirstName5", "LastName5", "email5@usi.ch");


        makeFriends(user1, "user1", user2, "user2");
        makeFriends(user2, "user2", user5, "user5");
        makeFriends(user5, "user5", user3, "user3");

        makeFriends(user1, "user1", user4, "user4");
        makeFriends(user4, "user4", user3, "user3");

        mvc.perform(get("/user/friends/path/user3")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].*", hasSize(3)))
                .andExpect(jsonPath("$[0].[0].userName", is("user1")))
                .andExpect(jsonPath("$[0].[1].userName", is("user4")))
                .andExpect(jsonPath("$[0].[2].userName", is("user3")));
    }


    /**
     * User finds shortest path to another user based on the schema
     * user1 <--> user2 <--> user3
     * user1 <--> user4 <--> user3
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Get shortest path - multiple schema")
    public void getShortestPath5() throws Exception{

        String user4 = createAccount(mvc, "user4", "myPwd123", "FirstName4", "LastName4", "email4@usi.ch");

        makeFriends(user1, "user1", user2, "user2");
        makeFriends(user2, "user2", user3, "user3");

        makeFriends(user1, "user1", user4, "user4");
        makeFriends(user4, "user4", user3, "user3");

        mvc.perform(get("/user/friends/path/user3")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(2)));
    }



    /**
     * User finds shortest path to another user based on the schema
     * user1 <--> user2 <--> user4 <--> user3
     * user1 <--> user5 <--> user3
     * user1 <--> user7 <--> user3
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Get friend recommendations - recommendation order - same numbers")
    public void getShortestPath9() throws Exception{

        final String user4 = createAccount(mvc, "user4", "myPwd123", "FirstName4", "LastName4", "email4@usi.ch");
        final String user5 = createAccount(mvc, "user5", "myPwd123", "FirstName5", "LastName5", "email5@usi.ch");
        final String user7 = createAccount(mvc, "user7", "myPwd123", "FirstName7", "LastName7", "email7@usi.ch");

        makeFriends(user1, "user1", user2, "user2");
        makeFriends(user2, "user2", user4, "user4");
        makeFriends(user4, "user4", user3, "user3");

        makeFriends(user1, "user1", user5, "user5");
        makeFriends(user5, "user5", user3, "user3");

        makeFriends(user1, "user1", user7, "user7");
        makeFriends(user7, "user7", user3, "user3");

        mvc.perform(get("/user/friends/path/user3")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(2)));
    }


    /**
     * User finds shortest path to another user based on the schema
     * user1 <--> user2 <--> user3 <--> user5 <--> user6
     * user1 <--> user4 <--> user3
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Get shortest path - multiple schema")
    public void getShortestPath10() throws Exception{

        String user4 = createAccount(mvc, "user4", "myPwd123", "FirstName4", "LastName4", "email4@usi.ch");
        String user5 = createAccount(mvc, "user5", "myPwd123", "FirstName5", "LastName5", "email5@usi.ch");
        String user6 = createAccount(mvc, "user6", "myPwd123", "FirstName6", "LastName6", "email6@usi.ch");

        makeFriends(user1, "user1", user2, "user2");
        makeFriends(user2, "user2", user3, "user3");
        makeFriends(user3, "user3", user5, "user5");
        makeFriends(user5, "user5", user6, "user6");

        makeFriends(user1, "user1", user4, "user4");
        makeFriends(user4, "user4", user3, "user3");

        mvc.perform(get("/user/friends/path/user6")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(2)));
    }


    /**
     * User finds shortest path to another user based on the schema
     * user1 <--> user2 <--> user3 <--> user5 <- != -> user6
     * user1 <--> user4 <--> user3
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Get shortest path - multiple schema")
    public void getShortestPath11() throws Exception{

        String user4 = createAccount(mvc, "user4", "myPwd123", "FirstName4", "LastName4", "email4@usi.ch");
        String user5 = createAccount(mvc, "user5", "myPwd123", "FirstName5", "LastName5", "email5@usi.ch");
        String user6 = createAccount(mvc, "user6", "myPwd123", "FirstName6", "LastName6", "email6@usi.ch");

        makeFriends(user1, "user1", user2, "user2");
        makeFriends(user2, "user2", user3, "user3");
        makeFriends(user3, "user3", user5, "user5");

        makeFriends(user1, "user1", user4, "user4");
        makeFriends(user4, "user4", user3, "user3");

        mvc.perform(get("/user/friends/path/user6")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(0)));
    }


    /**
     * User finds shortest path to another user based on the schema
     * user1 <- != -> user2 <--> user3 <--> user5 <--> user6
     * user1 <- != -> user4 <--> user3
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Get shortest path - multiple schema")
    public void getShortestPath12() throws Exception{

        String user4 = createAccount(mvc, "user4", "myPwd123", "FirstName4", "LastName4", "email4@usi.ch");
        String user5 = createAccount(mvc, "user5", "myPwd123", "FirstName5", "LastName5", "email5@usi.ch");
        String user6 = createAccount(mvc, "user6", "myPwd123", "FirstName6", "LastName6", "email6@usi.ch");

        makeFriends(user2, "user2", user3, "user3");
        makeFriends(user3, "user3", user5, "user5");
        makeFriends(user5, "user5", user6, "user6");

        makeFriends(user4, "user4", user3, "user3");

        mvc.perform(get("/user/friends/path/user6")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(0)));
    }


    /**
     * User finds shortest path to another user based on the schema
     * user1 <--> user2 <--> user3 <--> user5 <--> user7
     * user1 <--> user4 <--> user3 <--> user6 <--> user7
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Get shortest path - multiple schema")
    public void getShortestPath13() throws Exception{

        String user4 = createAccount(mvc, "user4", "myPwd123", "FirstName4", "LastName4", "email4@usi.ch");
        String user5 = createAccount(mvc, "user5", "myPwd123", "FirstName5", "LastName5", "email5@usi.ch");
        String user6 = createAccount(mvc, "user6", "myPwd123", "FirstName6", "LastName6", "email6@usi.ch");
        String user7 = createAccount(mvc, "user7", "myPwd123", "FirstName7", "LastName7", "email7@usi.ch");

        makeFriends(user1, "user1", user2, "user2");
        makeFriends(user1, "user1", user3, "user3");

        makeFriends(user2, "user2", user4, "user4");
        makeFriends(user3, "user3", user4, "user4");

        makeFriends(user4, "user4", user5, "user5");
        makeFriends(user4, "user4", user6, "user6");

        makeFriends(user5, "user5", user7, "user7");
        makeFriends(user6, "user6", user7, "user7");

        mvc.perform(get("/user/friends/path/user7")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(4)));
    }

    /**
     * Gary's test with a lot of paths everywhere
     * user1 <--> user2
     *
     * user2 <--> user3 <--> user6 <--> user7
     * user2 <--> user4 <--> user6 <--> user7
     * user2 <--> user5 <--> user6 <--> user7
     *
     * user7 <--> user8 <--> user11 <--> user12
     * user7 <--> user9 <--> user11 <--> user12
     * user7 <--> user10 <--> user11 <--> user12
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Get shortest path - multiple schema")
    public void getShortestPath14() throws Exception{

        String user4 = createAccount(mvc, "user4", "myPwd123", "FirstName4", "LastName4", "email4@usi.ch");
        String user5 = createAccount(mvc, "user5", "myPwd123", "FirstName5", "LastName5", "email5@usi.ch");
        String user6 = createAccount(mvc, "user6", "myPwd123", "FirstName6", "LastName6", "email6@usi.ch");
        String user7 = createAccount(mvc, "user7", "myPwd123", "FirstName7", "LastName7", "email7@usi.ch");
        String user8 = createAccount(mvc, "user8", "myPwd123", "FirstName8", "LastName8", "email8@usi.ch");
        String user9 = createAccount(mvc, "user9", "myPwd123", "FirstName9", "LastName9", "email9@usi.ch");
        String user10 = createAccount(mvc, "user10", "myPwd123", "FirstName10", "LastName10", "email10@usi.ch");
        String user11 = createAccount(mvc, "user11", "myPwd123", "FirstName11", "LastName11", "email11@usi.ch");
        String user12 = createAccount(mvc, "user12", "myPwd123", "FirstName12", "LastName12", "email12@usi.ch");


        makeFriends(user1, "user1", user2, "user2");

        makeFriends(user2, "user2", user3, "user3");
        makeFriends(user2, "user2", user4, "user4");
        makeFriends(user2, "user2", user5, "user5");

        makeFriends(user6, "user6", user3, "user3");
        makeFriends(user6, "user6", user4, "user4");
        makeFriends(user6, "user6", user5, "user5");

        makeFriends(user6, "user6", user7, "user7");

        makeFriends(user7, "user7", user8, "user8");
        makeFriends(user7, "user7", user9, "user9");
        makeFriends(user7, "user7", user10, "user10");

        makeFriends(user11, "user11", user8, "user8");
        makeFriends(user11, "user11", user9, "user9");
        makeFriends(user11, "user11", user10, "user10");

        makeFriends(user11, "user11", user12, "user12");

        mvc.perform(get("/user/friends/path/user12")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(9)));
    }


    /**
     * User finds shortest path to another user based on the schema
     * user1 <--> user2 <--> user3 <--> user5 <--> user7
     * user1 <--> user5 <--> user7
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Get shortest path - multiple schema")
    public void getShortestPath15() throws Exception{

        String user5 = createAccount(mvc, "user5", "myPwd123", "FirstName5", "LastName5", "email5@usi.ch");
        String user7 = createAccount(mvc, "user7", "myPwd123", "FirstName7", "LastName7", "email7@usi.ch");

        makeFriends(user1, "user1", user2, "user2");
        makeFriends(user2, "user2", user3, "user3");
        makeFriends(user3, "user3", user5, "user5");
        makeFriends(user5, "user5", user7, "user7");


        makeFriends(user1, "user1", user5, "user5");
        makeFriends(user5, "user5", user7, "user7");

        mvc.perform(get("/user/friends/path/user7")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(jsonPath("$[0].*", hasSize(3)));
    }




    /**
     * Helper function to get the recommended friends
     * @param token - token of the user
     * @return - returns the response
     * @throws Exception - mvc.perform throws Exception
     */
    private ResultActions getRecommended(@NonNull final String token) throws Exception{
        return mvc.perform(get("/user/friends/recommendations")
                .header("Authorization", token));
    }

    /**
     * Helper function to get the recommended friends
     */
    private void makeFriends(@NonNull final String userTokenA,
                             @NonNull final String usernameA,
                             @NonNull final String userTokenB,
                             @NonNull final String usernameB) throws Exception {

       // follow(mvc, userTokenA, usernameB).andExpect(status().isOk());
       // follow(mvc, userTokenB, usernameA).andExpect(status().isOk());
    }

    private ResultActions getPost(final String id, final String token) throws Exception {
        return mvc.perform(get("/post/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token));
    }
}

