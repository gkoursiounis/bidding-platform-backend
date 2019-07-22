package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.BackEndApplication;
import com.Auctions.backEnd.TestUtils;
import com.Auctions.backEnd.configs.TestConfig;
import com.Auctions.backEnd.models.Account;
import com.Auctions.backEnd.models.User;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;


import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = {TestConfig.class, BackEndApplication.class})
@AutoConfigureMockMvc

public class AdminControllerTest {

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    private String user1;
    private String user2;
    private String user3;
    private String admin;

    @BeforeEach
    private void before() throws Exception {

        testUtils.clearDB();

        mvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        user1 = TestUtils.createAccount(mvc, "user1", "myPwd123", "FirstName1", "LastName1", "email1@di.uoa.gr");
        user2 = TestUtils.createAccount(mvc, "user2", "myPwd123", "FistName2", "LastName2", "email2@di.uoa.gr");
        user3 = TestUtils.createAccount(mvc, "user3", "myPwd123", "FirstName3", "LastName3", "email3@di.uoa.gr");
    }


    private void unverify(final String username) {

        Account account = accountRepository.findByUsername(username);
        assertNotNull(account);
        account.setVerified(false);
        accountRepository.save(account);
    }

    private void makeAdmin(final String username){

        Account user = accountRepository.findByUsername(username);
        assertNotNull(user);
        user.setAdmin(true);
        accountRepository.save(user);
    }



    /**
     * Admin gets the list of unverified users
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Get pending registers - one user")
    public void getPendingRegisters1() throws Exception {

        makeAdmin("user3");
        unverify("user1");

        mvc.perform(get("/admin/pendingRegisters")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(1)));
    }


    /**
     * Admin gets the list of unverified users
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Get pending registers - no user")
    public void getPendingRegisters2() throws Exception {

        makeAdmin("user3");

        mvc.perform(get("/admin/pendingRegisters")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(0)));
    }


    /**
     * Admin gets the list of unverified users
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Get pending registers - two users")
    public void getPendingRegisters3() throws Exception {

        makeAdmin("user3");
        unverify("user1");
        unverify("user2");

        mvc.perform(get("/admin/pendingRegisters")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(2)));
    }


    /**
     * A User gets the list of unverified users
     * We should get back an <HTTP>UNAUTHORIZED</HTTP>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Get pending registers - no admin")
    public void getPendingRegisters4() throws Exception {

        mvc.perform(get("/admin/pendingRegisters")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isUnauthorized());
    }


    /**
     * A User gets the list of unverified users using invalid token
     * We should get back an <HTTP>BAD REQUEST</HTTP>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Get pending registers - invalid token")
    public void getPendingRegisters5() throws Exception {

        mvc.perform(get("/admin/pendingRegisters")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "invalidToken"))
                .andExpect(status().isBadRequest());
    }


    /**
     * Admin gets the list of all users except himself
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Get all users")
    public void getAllUsers1() throws Exception {

        makeAdmin("user3");

        mvc.perform(get("/admin/allUsers")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(2)));
    }


    /**
     * Admin gets the list of all users except himself
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Get all users - get other admins")
    public void getAllUsers2() throws Exception {

        makeAdmin("user3");
        makeAdmin("user2");

        mvc.perform(get("/admin/allUsers")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(2)));
    }


    /**
     * Admin gets the list of all users using invalid token
     * We should get back an <HTTP>BAD REQUEST</HTTP>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Get all registers - invalid token")
    public void getAllUsers3() throws Exception {

        mvc.perform(get("/admin/allUsers")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "invalidToken"))
                .andExpect(status().isBadRequest());
    }


    /**
     * A User gets the list of all users
     * We should get back an <HTTP>UNAUTHORIZED</HTTP>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Get pending registers - no admin")
    public void getAllUsers4() throws Exception {

        mvc.perform(get("/admin/allUsers")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isUnauthorized());
    }


    /**
     * A User gets the list of all users
     * We should get back an <HTTP>UNAUTHORIZED</HTTP>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Get pending registers - no admin")
    public void verifyAllUsers1() throws Exception {

        makeAdmin("user3");
        unverify("user1");
        unverify("user2");

        mvc.perform(patch("/admin/verifyAll")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user3))
                .andExpect(status().isOk());

        mvc.perform(get("/admin/pendingRegisters")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(0)));
    }
}