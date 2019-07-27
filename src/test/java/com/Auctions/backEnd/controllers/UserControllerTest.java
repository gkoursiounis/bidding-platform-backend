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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

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
    private WebApplicationContext wac;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;


    private String user1;
    private String user2;
    private String user3;

    @BeforeEach
    private void before() throws Exception {

        mvc = MockMvcBuilders.webAppContextSetup(this.wac)
                .alwaysDo(MockMvcResultHandlers.print())
                .build();

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
//    @Test
//    @DisplayName("User gets user details from blocked user")
//    public void getUserDetails1() throws Exception {
//
//        //blockUser(mvc, user2, getUserToString(mvc, user2, "user1")).andExpect(status().isOk());
//
//        getUser(mvc, user2, "user1")
//                .andExpect(status().isNotFound());
//    }


    /**
     * User tries to get user details from a user he is blocked by
     * We should get back an HTTP <code>NOT FOUND</code>
     *
     * @throws Exception - mvc.perform throws exception
     */
//    @Test
//    @DisplayName("User gets user details from user he is blocked by")
//    public void getUserDetails2() throws Exception {
//
//       // blockUser(mvc, user1, getUserToString(mvc, user1, "user2")).andExpect(status().isOk());
//
//        getUser(mvc, user2, "user1")
//                .andExpect(status().isNotFound());
//    }


    /**
     * User tries to get user details a non related user
     *
     * @throws Exception - mvc.perform throws exception
     */
//    @Test
//    @DisplayName("User gets user details from user he is blocked by")
//    public void getUserDetails3() throws Exception {
//        getUser(mvc, user1, "user2")
//                .andExpect(jsonPath("requestSent", is(false)))
//                .andExpect(status().isOk());
//    }
//
//    /**
//     * User tries to search for partial matching with invalid token
//     * We should get back an HTTP <code>FORBIDDEN</code>
//     *
//     * @throws Exception - mvc.perform throws exception
//     */
//    @Test
//    @DisplayName("User searches for partial matching with invalid token")
//    public void getPartialMatchedUsers1() throws Exception {
//
//        mvc.perform(get("/user/search")
//                .param("name", "user1")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "invalidToken"))
//                .andExpect(status().isForbidden());
//    }
//
//
//    /**
//     * User tries successfully to search for partial matching
//     *
//     * @throws Exception - mvc.perform throws exception
//     */
//    @Test
//    @DisplayName("Successful partial matching")
//    public void getPartialMatchedUsers2() throws Exception {
//
//        mvc.perform(get("/user/search")
//                .param("name", "user")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", user1))
//                .andExpect(jsonPath("$.*", hasSize(2)))
//                .andExpect(status().isOk());
//    }
//
//
//    /**
//     * User tries to search for partial matching with empty name
//     * We should get back an HTTP <code>BAD REQUEST</code>
//     *
//     * @throws Exception - mvc.perform throws exception
//     */
//    @Test
//    @DisplayName("Partial matching with empty name")
//    public void getPartialMatchedUsers3() throws Exception {
//
//        mvc.perform(get("/user/search")
//                .param("name", "")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", user1))
//                .andExpect(status().isBadRequest());
//    }
//
//
//    /**
//     * User tries to search for himself using partial matching
//     * Nothing should be returned as result
//     *
//     * @throws Exception - mvc.perform throws exception
//     */
//    @Test
//    @DisplayName("User tries to search for himself in partial matching")
//    public void getPartialMatchedUsers4() throws Exception {
//
//        mvc.perform(get("/user/search")
//                .param("name", "user1")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", user1))
//                .andExpect(jsonPath("$.*", hasSize(0)))
//                .andExpect(status().isOk());
//    }
//
//
//    /**
//     * User tries to search for a blocked user using partial matching
//     * Nothing should be returned as result
//     *
//     * @throws Exception - mvc.perform throws exception
//     */
//    @Test
//    @DisplayName("User searches for a blocked user in partial matching")
//    public void getPartialMatchedUsers5() throws Exception {
//
//      //  blockUser(mvc, user1, getAccountId(mvc, user2));
//
//        mvc.perform(get("/user/search")
//                .param("name", "user2")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", user1))
//                .andExpect(jsonPath("$.*", hasSize(0)))
//                .andExpect(status().isOk());
//    }
//
//
//    /**
//     * User tries to search for a user that he has
//     * blocked the initial user
//     * Nothing should be returned as result
//     *
//     * @throws Exception - mvc.perform throws exception
//     */
//    @Test
//    @DisplayName("User searches blocked-by user")
//    public void getPartialMatchedUsers6() throws Exception {
//
//    //    blockUser(mvc, user2, getAccountId(mvc, user1));
//
//        mvc.perform(get("/user/search")
//                .param("name", "user2")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", user1))
//                .andExpect(jsonPath("$.*", hasSize(0)))
//                .andExpect(status().isOk());
//    }
//
//
//    /**
//     * User searches for keyword that matches with many usernames
//     *
//     * @throws Exception - mvc.perform throws exception
//     */
//    @Test
//    @DisplayName("Get multiple partial match")
//    public void getPartialMatchedUsers7() throws Exception {
//
//        mvc.perform(get("/user/search")
//                .param("name", "use")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", user1))
//                .andExpect(jsonPath("$.*", hasSize(2)))
//                .andExpect(status().isOk());
//    }
//
//
//
//    /**
//     * User gets a list of blocked users with invalid token
//     *
//     * @throws Exception - mvc.perform throws exception
//     */
//    @Test
//    @DisplayName("User gets a list of blocked users with invalid token")
//    public void getMyBlockedUser2() throws Exception {
//
//        mvc.perform(get("/user/blocked")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "invalidToken"))
//                .andExpect(status().isForbidden());
//    }
//
//
//    /**
//     * User makes his account private
//     *
//     * @throws Exception - mvc.perform throws exception
//     */
//    @Test
//    @DisplayName("User makes his account private")
//    public void updateDetails1() throws Exception {
//
//        mvc.perform(put("/user/updateDetails")
//                .param("privateProfile", "true")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", user1))
//                .andExpect(status().isOk());
//
//        getUser(mvc, user1, "user1")
//                .andExpect(jsonPath("privateProfile", is(true)))
//                .andExpect(status().isOk());
//    }
//
//
//    /**
//     * User sets new biography with less than 500 characters
//     *
//     * @throws Exception - mvc.perform throws exception
//     */
//    @Test
//    @DisplayName("New bio less than 500 characters")
//    public void updateDetails3() throws Exception {
//
//        final String newBio = "this is my new bio";
//
//        mvc.perform(put("/user/updateDetails")
//                .param("bio", newBio)
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", user1))
//                .andExpect(status().isOk());
//
//        getUser(mvc, user1, "user1")
//                .andExpect(jsonPath("bio", is(newBio)))
//                .andExpect(status().isOk());
//    }
//
//
//    /**
//     * User sets new biography with more than 500 characters
//     * We should get back an HTTP <code>BAD REQUEST</code>
//     *
//     * @throws Exception - mvc.perform throws exception
//     */
//    @Test
//    @DisplayName("New bio more than 500 characters")
//    public void updateDetails4() throws Exception {
//
//        String newBio = "";
//
//        for (int i = 0; i < 501; i++) {
//            newBio += "n";
//        }
//
//        mvc.perform(put("/user/updateDetails")
//                .param("bio", newBio)
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", user1))
//                .andExpect(status().isBadRequest());
//    }
//
//

//

//
//
//
//
//
//
//    /**
//     * Helper function to get the recommended friends
//     */
//    private void makeFriends(@NonNull final String userTokenA,
//                             @NonNull final String usernameA,
//                             @NonNull final String userTokenB,
//                             @NonNull final String usernameB) throws Exception {
//
//       // follow(mvc, userTokenA, usernameB).andExpect(status().isOk());
//       // follow(mvc, userTokenB, usernameA).andExpect(status().isOk());
//    }
//
//    private ResultActions getPost(final String id, final String token) throws Exception {
//        return mvc.perform(get("/post/" + id)
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", token));
//    }
}

