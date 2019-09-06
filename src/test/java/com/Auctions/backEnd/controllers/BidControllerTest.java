package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.BackEndApplication;
import com.Auctions.backEnd.TestUtils;
import com.Auctions.backEnd.configs.TestConfig;
import com.Auctions.backEnd.models.Account;
import com.Auctions.backEnd.models.ItemCategory;
import com.Auctions.backEnd.repositories.AccountRepository;
import com.Auctions.backEnd.repositories.ItemCategoryRepository;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(classes = {TestConfig.class, BackEndApplication.class})
@AutoConfigureMockMvc
public class BidControllerTest {

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ItemCategoryRepository itemCategoryRepository;

    private String user1;
    private String user2;
    private String user3;
    private String categoryId;


    @BeforeEach
    public void before() throws Exception {

        mvc = MockMvcBuilders.webAppContextSetup(this.wac)
                .alwaysDo(MockMvcResultHandlers.print())
                .build();

        user1 = TestUtils.createAccount(mvc, "user1", "myPwd123", "FirstName1", "LastName1", "email1@di.uoa.gr");
        user2 = TestUtils.createAccount(mvc, "user2", "myPwd123", "FirstName2", "LastName2", "email2@di.uoa.gr");
        user3 = TestUtils.createAccount(mvc, "user3", "myPwd123", "FirstName3", "LastName3", "email3@di.uoa.gr");

        ItemCategory category = new ItemCategory();
        category.setName("cat1");
        itemCategoryRepository.save(category);
        categoryId = category.getId().toString();
    }

    @AfterEach
    public void after() {
        this.testUtils.clearDB();
    }


    private void verify(final String username) {

        Account account = accountRepository.findByUsername(username);
        assertNotNull(account);
        account.setVerified(true);
        accountRepository.save(account);
    }

    /**
     * User successfully makes a bid of 6.0E with firstBid 5.4E
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Successful item creation")
    public void makeBid1() throws Exception {

        verify("user1");
        ItemCategory ic = itemCategoryRepository.findItemCategoryByName("All categories");
        String item_id = TestUtils.makeItem(mvc, ic.getId().toString(), user1);

        mvc.perform(post("/bid/makeBid/" + item_id)
                .contentType(MediaType.APPLICATION_JSON)
                .param("offer", "6.0")
                .header("Authorization", user2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("auctionCompleted", is(false)));

        mvc.perform(get("/user/myBids")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(1)));

        mvc.perform(get("/item/" + item_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk());

        mvc.perform(get("/user/myAuctions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(1)));
    }


    /**
     * User successfully makes a bid with invalid itemId
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Invalid itemId")
    public void makeBid2() throws Exception {

        mvc.perform(post("/bid/makeBid/12345")
                .contentType(MediaType.APPLICATION_JSON)
                .param("offer", "6.0")
                .header("Authorization", user2))
                .andExpect(status().isNotFound());
    }


    /**
     * User makes a bid at his own auction
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Seller bids")
    public void makeBid3() throws Exception {

        String item_id = TestUtils.makeItem(mvc, categoryId, user1);

        mvc.perform(post("/bid/makeBid/" + item_id)
                .contentType(MediaType.APPLICATION_JSON)
                .param("offer", "6.0")
                .header("Authorization", user1))
                .andExpect(status().isBadRequest());
    }


    /**
     * Multiple bids
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Multiple bids")
    public void makeBid4() throws Exception {

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

        mvc.perform(get("/item/" + item_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk());
    }


    /**
     * Offer > buyPrice
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Offer > buyPrice")
    public void makeBid5() throws Exception {

        String item_id = TestUtils.makeItem(mvc, categoryId, user1);

        mvc.perform(post("/bid/makeBid/" + item_id)
                .contentType(MediaType.APPLICATION_JSON)
                .param("offer", "15.0")
                .header("Authorization", user2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("auctionCompleted", is(true)));

        mvc.perform(get("/user/myNotifications")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(1)));

    }


    /**
     * User gets the details of a bid using invalid bid id
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Get bid details - invalid id")
    public void getBid1() throws Exception {

        mvc.perform(get("/bid/12345")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user2))
                .andExpect(status().isNotFound());

    }


    @Test
    public void test() throws Exception {

        mvc.perform(get("/recommend")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andDo( mvcResult ->
                        mvc.perform(get("/item/allAuctions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Authorization", user1))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.*", hasSize(10)))
                );




    }


    @Test
    public void lsh() throws Exception {

        mvc.perform(get("/recommend/lsh")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk());




    }
}