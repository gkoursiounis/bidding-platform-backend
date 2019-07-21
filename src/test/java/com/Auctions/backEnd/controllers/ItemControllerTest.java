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
public class ItemControllerTest {

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

        testUtils.clearDB();

        mvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        user1 = createAccount(mvc, "user1", "myPwd123", "FirstName1", "LastName1", "email1@usi.ch");
        user2 = createAccount(mvc, "user2", "myPwd123", "FirstName2", "LastName2", "email2@usi.ch");
        user3 = createAccount(mvc, "user3", "myPwd123", "FirstName3", "LastName3", "email3@usi.ch");
    }

    @AfterEach
    private void after() {
        testUtils.clearDB();
    }

    private void unverify(final String username) {

        Account account = accountRepository.findByUsername(username);
        assertNotNull(account);
        account.setVerified(false);
        accountRepository.save(account);
    }


    /**
     * User successfully tries to create an item/auction
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Successful item creation")
    public void createItem1() throws Exception {
        mvc.perform(
                post("/item")
                        .param("name", "item1")
                        .param("buyPrice", "10.4")
                        .param("firstBid", "5.3")
                        .param("endsAt", "2015-09-26T01:30:00.000-04:00")
                        .header("Authorization", user1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }


    /**
     * User tries to create an item with missing parameters
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Name parameter missing")
    public void createItem2() throws Exception {
        mvc.perform(
                post("/item")
                        .param("buyPrice", "10.4")
                        .param("firstBid", "5.3")
                        .param("endsAt", "2015-09-26T01:30:00.000-04:00")
                        .header("Authorization", user1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }


    /**
     * User tries to create an item with missing parameters
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Date parameter missing")
    public void createItem3() throws Exception {
        mvc.perform(
                post("/item")
                        .param("name", "item1")
                        .param("buyPrice", "10.4")
                        .param("firstBid", "5.3")
                        .header("Authorization", user1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }


    /**
     * User tries to create an item with missing parameters
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("firstBid parameter missing")
    public void createItem4() throws Exception {
        mvc.perform(
                post("/item")
                        .param("name", "item1")
                        .param("buyPrice", "10.4")
                        .param("endsAt", "2015-09-26T01:30:00.000-04:00")
                        .header("Authorization", user1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }


    /**
     * User tries to create an item without being verified
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Unverified user tries to create an item")
    public void createItem5() throws Exception {

        unverify("user1");

        mvc.perform(
                post("/item")
                        .param("name", "item1")
                        .param("buyPrice", "10.4")
                        .param("firstBid", "5.3")
                        .param("endsAt", "2015-09-26T01:30:00.000-04:00")
                        .header("Authorization", user1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }


    /**
     * User tries to create an item as a visitor
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Visitor tries to create an item/auction")
    public void createItem6() throws Exception {

        String user4 = createAccount(mvc, "user4", "myPwd123", "FirstName4", "LastName4", "email4@usi.ch", true);

        mvc.perform(
                post("/item")
                        .param("name", "item1")
                        .param("buyPrice", "10.4")
                        .param("firstBid", "5.3")
                        .param("endsAt", "2015-09-26T01:30:00.000-04:00")
                        .header("Authorization", user4)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }


    /**
     * User tries to create an item as a visitor
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Visitor tries to create an item/auction")
    public void createItem7() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "media",
                "Bloodhound.jpg",
                "image/jpeg",
                new FileInputStream("media/Bloodhound.jpg"));

        mvc.perform(
                multipart("/item/test")
                        .file(file)
                        .secure(true)
                        .header("Authorization", user1)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());


    }


}