package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.BackEndApplication;
import com.Auctions.backEnd.TestUtils;
import com.Auctions.backEnd.models.Item;
import com.Auctions.backEnd.models.ItemCategory;
import com.Auctions.backEnd.repositories.AccountRepository;
import com.Auctions.backEnd.repositories.ItemCategoryRepository;
import com.Auctions.backEnd.repositories.ItemRepository;
import com.Auctions.backEnd.repositories.UserRepository;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.text.ParseException;
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

    @Autowired
    private ItemCategoryRepository itemCategoryRepository;

    @Autowired
    private ItemRepository itemRepository;


    private String user1;
    private String user2;
    private String user3;
    private String categoryId;

    @BeforeEach
    private void before() throws Exception {

        mvc = MockMvcBuilders.webAppContextSetup(this.wac)
                .alwaysDo(MockMvcResultHandlers.print())
                .build();

        user1 = createAccount(mvc, "user1", "myPwd123", "FirstName1", "LastName1", "email1@usi.ch");
        user2 = createAccount(mvc, "user2", "myPwd123", "FirstName2", "LastName2", "email2@usi.ch");
        user3 = createAccount(mvc, "user3", "myPwd123", "FirstName3", "LastName3", "email3@usi.ch");

        ItemCategory category = new ItemCategory();
        category.setName("cars");
        itemCategoryRepository.save(category);
        categoryId = category.getId().toString();
    }

    @AfterEach
    private void after() {
        testUtils.clearDB();
    }


    /**
     * Successful General error test
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("General error test")
    public void generalErrorTest() throws Exception {
        mvc.perform(post("/user/myAuctions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isBadRequest());
    }


    /**
     * Successful get user
     *
     * @throws Exception - mvc.perform throws exception
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
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User is the requested User")
    public void getUser3() throws Exception {
        getUser(mvc, user3, "user3")
                .andExpect(status().isOk());

    }


    /**
     * User gets a list of his auctions
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User gets a list of his auctions")
    public void getMyAuctions1() throws Exception {

        TestUtils.makeItem(mvc, categoryId, user1);
        TestUtils.makeItem(mvc, categoryId, user1);
        TestUtils.makeItem(mvc, categoryId, user3);

        TestUtils.makeExpiredItem(mvc, categoryId, user1);

        Thread.sleep(6000);
        mvc.perform(get("/user/myAuctions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(3)));
    }


    /**
     * User gets a list of his open auctions
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User gets a list of his open auctions")
    public void getMyOpenAuctions1() throws Exception {

        TestUtils.makeItem(mvc, categoryId, user1);
        TestUtils.makeItem(mvc, categoryId, user1);
        TestUtils.makeItem(mvc, categoryId, user3);

        TestUtils.makeExpiredItem(mvc, categoryId, user1);

        Thread.sleep(6000);
        mvc.perform(get("/user/myOpenAuctions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(2)));
    }


    /**
     * User gets a list of his notifications
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User gets a list of his notifications")
    public void getMyNotifications1() throws Exception {

        TestUtils.makeExpiredItem(mvc, categoryId, user1);

        Thread.sleep(10000);
        mvc.perform(get("/user/myNotifications")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(1)));
    }


    /**
     * User gets a list of his notifications
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User gets a list of his notifications 2")
    public void getMyNotifications2() throws Exception {

        mvc.perform(get("/user/myNotifications")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(0)));
    }


    /**
     * User gets a list of his notifications
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User gets a list of his notifications 3")
    public void getMyNotifications3() throws Exception {

        TestUtils.makeItem(mvc, categoryId, user3);
        TestUtils.makeItem(mvc, categoryId, user2);

        mvc.perform(get("/user/myNotifications")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(0)));
    }


    /**
     * User gets a list of his notifications
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User gets a list of his notifications 4")
    public void getMyNotifications4() throws Exception {

        Date date = new Date(new Date().getTime() + 3000);

        String item_id = ((JSONObject) new JSONParser().parse(
                mvc.perform(
                        post("/item")
                                .param("name", "item1")
                                .param("buyPrice", "10.4")
                                .param("firstBid", "5.3")
                                .param("categoriesId", categoryId)
                                .param("longitude", "23.76695")
                                .param("latitude", "37.968564")
                                .param("locationTitle", "Dit UoA")
                                .param("endsAt", "2019-08-03T19:48:50.000-04:00")
                                .param("description", "this is the description")
                                .header("Authorization", user1)
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString()))
                .get("id").toString();

        mvc.perform(post("/bid/makeBid/" + item_id)
                .contentType(MediaType.APPLICATION_JSON)
                .param("offer", "6.0")
                .header("Authorization", user2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("auctionCompleted", is(false)));

        Thread.sleep(6000);
        mvc.perform(get("/user/myNotifications")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(1)));
    }


    /**
     * User gets a list of his unseen notifications
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User gets a list of his unseen notifications")
    public void getUnseenNotifications1() throws Exception {

        TestUtils.makeExpiredItem(mvc, categoryId, user1);

        Thread.sleep(6000);
        mvc.perform(get("/user/unseenNotifications")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(1)));
    }


    /**
     * User gets a list of his unseen notifications
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User gets a list of his unseen notifications 2")
    public void getUnseenNotifications2() throws Exception {

        TestUtils.makeItem(mvc, categoryId, user1);
        TestUtils.makeExpiredItem(mvc, categoryId, user1);
        TestUtils.makeExpiredItem(mvc, categoryId, user2);

        Thread.sleep(6000);
        mvc.perform(get("/user/unseenNotifications")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(1)));
    }

////////////////////////////////
    @Test
    @DisplayName("User gets a list of his unseen notifications 2")
    public void get2() throws Exception {

        String item_id = TestUtils.makeItem(mvc, categoryId, user1);

        mvc.perform(post("/bid/makeBid/" + item_id)
                .contentType(MediaType.APPLICATION_JSON)
                .param("offer", "6.0")
                .header("Authorization", user2))
                .andExpect(status().isOk());

        mvc.perform(post("/bid/makeBid/" + item_id)
                .contentType(MediaType.APPLICATION_JSON)
                .param("offer", "7.0")
                .header("Authorization", user2))
                .andExpect(status().isOk());

        mvc.perform(post("/bid/makeBid/" + item_id)
                .contentType(MediaType.APPLICATION_JSON)
                .param("offer", "8.0")
                .header("Authorization", user3))
                .andExpect(status().isOk());

        mvc.perform(get("/user/test/test/" + item_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk());
    }
}

