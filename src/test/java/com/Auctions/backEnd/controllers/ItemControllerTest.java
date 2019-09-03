package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.BackEndApplication;
import com.Auctions.backEnd.TestUtils;
import com.Auctions.backEnd.models.Account;
import com.Auctions.backEnd.models.ItemCategory;
import com.Auctions.backEnd.repositories.AccountRepository;
import com.Auctions.backEnd.repositories.ItemCategoryRepository;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import static com.Auctions.backEnd.TestUtils.*;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
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

    @Autowired
    private ItemCategoryRepository itemCategoryRepository;

    private String user1;
    private String user2;
    private String user3;
    private String categoryId;

    @BeforeEach
    private void before() throws Exception {

        mvc = MockMvcBuilders.webAppContextSetup(this.wac)
                .alwaysDo(MockMvcResultHandlers.print())
                .build();

        user1 = createAccount(mvc, "user1", "myPwd123", "FirstName1", "LastName1", "email1@di.uoa.gr");
        user2 = createAccount(mvc, "user2", "myPwd123", "FirstName2", "LastName2", "email2@di.uoa.gr");
        user3 = createAccount(mvc, "user3", "myPwd123", "FirstName3", "LastName3", "email3@di.uoa.gr");

        ItemCategory category = new ItemCategory();
        category.setName("cars");
        itemCategoryRepository.save(category);
        categoryId = category.getId().toString();
    }

    @AfterEach
    private void after() {
        testUtils.clearDB();
    }

//    @Test
//    @DisplayName("Successful item creation")
//    public void createItem211() throws Exception {
//        for(int i=0;i<10;i++)
//        {
//            TestUtils.makeDetailedItem(mvc, categoryId, "item"+i, "description", user1);
//        }
//        for(int i=0;i<10;i++)
//        {
//            TestUtils.makeDetailedItem(mvc, categoryId, "item"+i, "description", user1);
//        }
//    }

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
        user.setVerified(true);
        accountRepository.save(user);
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
                        .param("categoriesId", categoryId)
                        .param("longitude", "23.76695")
                        .param("latitude", "37.968564")
                        .param("locationTitle", "Dit UoA")
                        .param("endsAt", "2021-09-26T01:30:00.000-04:00")
                        .param("description", "this is the description")
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
                        .param("firstBid", "5.3")
                        .param("categoriesId", categoryId)
                        .param("longitude", "23.76695")
                        .param("latitude", "37.968564")
                        .param("locationTitle", "Dit UoA")
                        .param("endsAt", "2021-09-26T01:30:00.000-04:00")
                        .param("description", "this is the description")
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
                        .param("categoriesId", categoryId)
                        .param("longitude", "23.76695")
                        .param("latitude", "37.968564")
                        .param("locationTitle", "Dit UoA")
                        .param("description", "this is the description")
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
                        .param("categoriesId", categoryId)
                        .param("longitude", "23.76695")
                        .param("latitude", "37.968564")
                        .param("locationTitle", "Dit UoA")
                        .param("endsAt", "2021-09-26T01:30:00.000-04:00")
                        .param("description", "this is the description")
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
    @DisplayName("BuyPrice missing")
    public void createItem9() throws Exception {
        mvc.perform(
                post("/item")
                        .param("name", "item1")
                        .param("firstBid", "5.3")
                        .param("categoriesId", categoryId)
                        .param("longitude", "23.76695")
                        .param("latitude", "37.968564")
                        .param("locationTitle", "Dit UoA")
                        .param("endsAt", "2021-09-26T01:30:00.000-04:00")
                        .param("description", "this is the description")
                        .header("Authorization", user1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
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
                        .param("categoriesId", categoryId)
                        .param("longitude", "23.76695")
                        .param("latitude", "37.968564")
                        .param("locationTitle", "Dit UoA")
                        .param("endsAt", "2021-09-26T01:30:00.000-04:00")
                        .param("description", "this is the description")
                        .header("Authorization", user1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }


    /**
     * User tries to create an item using invalid token
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("User tries to create an item using invalid token")
    public void createItem6() throws Exception {

        mvc.perform(
                post("/item")
                        .param("name", "item1")
                        .param("buyPrice", "10.4")
                        .param("firstBid", "5.3")
                        .param("categoriesId", categoryId)
                        .param("longitude", "23.76695")
                        .param("latitude", "37.968564")
                        .param("locationTitle", "Dit UoA")
                        .param("endsAt", "2021-09-26T01:30:00.000-04:00")
                        .param("description", "this is the description")
                        .header("Authorization", "invalidToken")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }


    /**
     * User tries to create an item with faulty coordinates
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Wrong coordinates")
    public void createItem8() throws Exception {
        mvc.perform(
                post("/item")
                        .param("name", "item1")
                        .param("buyPrice", "10.4")
                        .param("firstBid", "5.3")
                        .param("categoriesId", categoryId)
                        .param("longitude", "hello")
                        .param("latitude", "hello")
                        .param("locationTitle", "Dit UoA")
                        .param("endsAt", "2021-09-26T01:30:00.000-04:00")
                        .param("description", "this is the description")
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
    @DisplayName("Missing categoriesId")
    public void createItem10() throws Exception {
        mvc.perform(
                post("/item")
                        .param("name", "item1")
                        .param("firstBid", "5.3")
                        .param("buyPrice", "10.4")
                        .param("categoriesId", "")
                        .param("longitude", "23.76695")
                        .param("latitude", "37.968564")
                        .param("locationTitle", "Dit UoA")
                        .param("endsAt", "2021-09-26T01:30:00.000-04:00")
                        .param("description", "this is the description")
                        .header("Authorization", user1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }


    /**
     * User tries to create an item/auction with wrong categoriesId
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Invalid categoriesId")
    public void createItem11() throws Exception {
        mvc.perform(
                post("/item")
                        .param("name", "item1")
                        .param("buyPrice", "10.4")
                        .param("firstBid", "5.3")
                        .param("categoriesId", "64323")
                        .param("longitude", "23.76695")
                        .param("latitude", "37.968564")
                        .param("locationTitle", "Dit UoA")
                        .param("endsAt", "2021-09-26T01:30:00.000-04:00")
                        .param("description", "this is the description")
                        .header("Authorization", user1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    /**
     * User tries to create an item/auction with categoriesId > 5
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("categoriesId > 5")
    public void createItem12() throws Exception {

        makeAdmin("user3");
        String cat2 = createCategory(mvc, user3, "cat2");
        String cat3 = createCategory(mvc, user3, "cat3");
        String cat4 = createCategory(mvc, user3, "cat4");
        String cat5 = createCategory(mvc, user3, "cat5");
        String cat6 = createCategory(mvc, user3, "cat6");
        String categories = categoryId + ", " + cat2 + ", " + cat3 + ", " + cat4 + ", " + cat5 + ", " + cat6;

        mvc.perform(
                post("/item")
                        .param("name", "item1")
                        .param("buyPrice", "10.4")
                        .param("firstBid", "5.3")
                        .param("categoriesId", categories)
                        .param("longitude", "23.76695")
                        .param("latitude", "37.968564")
                        .param("locationTitle", "Dit UoA")
                        .param("endsAt", "2021-09-26T01:30:00.000-04:00")
                        .param("description", "this is the description")
                        .header("Authorization", user1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }


    /**
     * User tries to create an item/auction with missing categoriesId
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Missing categoriesId")
    public void createItem13() throws Exception {
        mvc.perform(
                post("/item")
                        .param("name", "item1")
                        .param("buyPrice", "10.4")
                        .param("firstBid", "5.3")
                        .param("longitude", "23.76695")
                        .param("latitude", "37.968564")
                        .param("locationTitle", "Dit UoA")
                        .param("endsAt", "2021-09-26T01:30:00.000-04:00")
                        .param("description", "this is the description")
                        .header("Authorization", user1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }


    /**
     * User tries to create an item/auction with empty coordinates
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Empty coordinates")
    public void createItem14() throws Exception {
        mvc.perform(
                post("/item")
                        .param("name", "item1")
                        .param("buyPrice", "10.4")
                        .param("firstBid", "5.3")
                        .param("categoriesId", categoryId)
                        .param("longitude", "")
                        .param("latitude", "")
                        .param("locationTitle", "Dit UoA")
                        .param("endsAt", "2021-09-26T01:30:00.000-04:00")
                        .param("description", "this is the description")
                        .header("Authorization", user1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }


    /**
     * User tries to create an item/auction with empty parameters
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Empty name")
    public void createItem15() throws Exception {
        mvc.perform(
                post("/item")
                        .param("name", "")
                        .param("buyPrice", "10.4")
                        .param("firstBid", "5.3")
                        .param("categoriesId", categoryId)
                        .param("longitude", "23.76695")
                        .param("latitude", "37.968564")
                        .param("locationTitle", "Dit UoA")
                        .param("endsAt", "2021-09-26T01:30:00.000-04:00")
                        .param("description", "this is the description")
                        .header("Authorization", user1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }


    /**
     * User tries to create an item/auction with empty parameters
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Empty buyPrice")
    public void createItem16() throws Exception {
        mvc.perform(
                post("/item")
                        .param("name", "item1")
                        .param("buyPrice", "")
                        .param("firstBid", "5.3")
                        .param("categoriesId", categoryId)
                        .param("longitude", "23.76695")
                        .param("latitude", "37.968564")
                        .param("locationTitle", "Dit UoA")
                        .param("endsAt", "2021-09-26T01:30:00.000-04:00")
                        .param("description", "this is the description")
                        .header("Authorization", user1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }


    /**
     * User tries to create an item/auction with empty parameters
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Empty firstBid")
    public void createItem17() throws Exception {
        mvc.perform(
                post("/item")
                        .param("name", "item1")
                        .param("buyPrice", "10.4")
                        .param("firstBid", "")
                        .param("categoriesId", categoryId)
                        .param("longitude", "23.76695")
                        .param("latitude", "37.968564")
                        .param("locationTitle", "Dit UoA")
                        .param("endsAt", "2021-09-26T01:30:00.000-04:00")
                        .param("description", "this is the description")
                        .header("Authorization", user1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }


    /**
     * User tries to create an item/auction with empty parameters
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Empty description")
    public void createItem18() throws Exception {
        mvc.perform(
                post("/item")
                        .param("name", "item1")
                        .param("buyPrice", "10.4")
                        .param("firstBid", "5.3")
                        .param("categoriesId", categoryId)
                        .param("longitude", "23.76695")
                        .param("latitude", "37.968564")
                        .param("locationTitle", "Dit UoA")
                        .param("endsAt", "2021-09-26T01:30:00.000-04:00")
                        .param("description", "")
                        .header("Authorization", user1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }


    /**
     * User tries to create an item/auction with empty parameters
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Empty endsAt")
    public void createItem19() throws Exception {
        mvc.perform(
                post("/item")
                        .param("name", "item1")
                        .param("buyPrice", "10.4")
                        .param("firstBid", "5.3")
                        .param("categoriesId", categoryId)
                        .param("longitude", "23.76695")
                        .param("latitude", "37.968564")
                        .param("locationTitle", "Dit UoA")
                        .param("endsAt", "")
                        .param("description", "this is the description")
                        .header("Authorization", user1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }


    /**
     * User tries to create an item with a picture
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("User tries to create an item with a picture")
    public void createItem20() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "media",
                "Bloodhound.jpg",
                "image/jpeg",
                new FileInputStream("media/Bloodhound.jpg"));

        mvc.perform(
                multipart("/item")
                        .file(file)
                        .param("name", "item1")
                        .param("buyPrice", "10.4")
                        .param("firstBid", "5.3")
                        .param("categoriesId", categoryId)
                        .param("longitude", "23.76695")
                        .param("latitude", "37.968564")
                        .param("locationTitle", "Dit UoA")
                        .param("endsAt", "2021-09-26T01:30:00.000-04:00")
                        .param("description", "this is the description")
                        .header("Authorization", user1)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());
    }


    /**
     * User tries to create an item/auction with firstBid > buyPrice
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("firstBid > buyPrice")
    public void createItem21() throws Exception {
        mvc.perform(
                post("/item")
                        .param("name", "item1")
                        .param("buyPrice", "5.3")
                        .param("firstBid", "10.4")
                        .param("categoriesId", categoryId)
                        .param("longitude", "23.76695")
                        .param("latitude", "37.968564")
                        .param("locationTitle", "Dit UoA")
                        .param("endsAt", "2021-09-26T01:30:00.000-04:00")
                        .param("description", "this is the description")
                        .header("Authorization", user1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }


    /**
     * User successfully tries to create an item/auction by description
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Successful item creation by description")
    public void createItem22() throws Exception {
        String id = TestUtils.makeDetailedItem
                (mvc, categoryId, "item1", "this is\r\n the\r\ndescription", user1);

        String description = ((JSONObject) new JSONParser().parse(mvc.perform(get("/item/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()))
                .get("description").toString();

        assertEquals(description, "this is\r\n the\r\ndescription");
    }


    /**
     * User successfully tries to create an item/auction with integer firstBid
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Successful item creation with integer firstBid")
    public void createItem23() throws Exception {
        mvc.perform(
                post("/item")
                        .param("name", "item1")
                        .param("buyPrice", "10.0")
                        .param("firstBid", "5")
                        .param("categoriesId", categoryId)
                        .param("longitude", "23.76695")
                        .param("latitude", "37.968564")
                        .param("locationTitle", "Dit UoA")
                        .param("endsAt", "2021-09-26T01:30:00.000-04:00")
                        .param("description", "this is the description")
                        .header("Authorization", user1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }


    /**
     * User tries to create an item with a 2 pictures
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("User tries to create an item with 2 pictures")
    public void createItem24() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "media",
                "Bloodhound.jpg",
                "image/jpeg",
                new FileInputStream("media/Bloodhound.jpg"));

        MockMultipartFile file1 = new MockMultipartFile(
                "media",
                "Bloodhound.jpg",
                "image/jpeg",
                new FileInputStream("media/Bloodhound.jpg"));

        mvc.perform(
                multipart("/item")
                        .file(file)
                        .file(file1)
                        .param("name", "item1")
                        .param("buyPrice", "10.4")
                        .param("firstBid", "5.3")
                        .param("categoriesId", categoryId)
                        .param("longitude", "23.76695")
                        .param("latitude", "37.968564")
                        .param("locationTitle", "Dit UoA")
                        .param("endsAt", "2021-09-26T01:30:00.000-04:00")
                        .param("description", "this is the description")
                        .header("Authorization", user1)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());
    }


    /**
     * User tries to create an item with a 4 pictures
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("User tries to create an item with 4 pictures")
    public void createItem25() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "media",
                "Bloodhound.jpg",
                "image/jpeg",
                new FileInputStream("media/Bloodhound.jpg"));

        MockMultipartFile file1 = new MockMultipartFile(
                "media",
                "Bloodhound.jpg",
                "image/jpeg",
                new FileInputStream("media/Bloodhound.jpg"));

        MockMultipartFile file2 = new MockMultipartFile(
                "media",
                "Bloodhound.jpg",
                "image/jpeg",
                new FileInputStream("media/Bloodhound.jpg"));

        MockMultipartFile file3 = new MockMultipartFile(
                "media",
                "Bloodhound.jpg",
                "image/jpeg",
                new FileInputStream("media/Bloodhound.jpg"));

        mvc.perform(
                multipart("/item")
                        .file(file)
                        .file(file1)
                        .file(file2)
                        .file(file3)
                        .param("name", "item1")
                        .param("buyPrice", "10.4")
                        .param("firstBid", "5.3")
                        .param("categoriesId", categoryId)
                        .param("longitude", "23.76695")
                        .param("latitude", "37.968564")
                        .param("locationTitle", "Dit UoA")
                        .param("endsAt", "2021-09-26T01:30:00.000-04:00")
                        .param("description", "this is the description")
                        .header("Authorization", user1)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());
    }


    /**
     * User tries to create an item as application/pdf
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("User tries to create an item as application/pdf")
    public void createItem26() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "media",
                "Bloodhound.pdf",
                "application/pdf",
                new FileInputStream("media/Bloodhound.jpg"));

        mvc.perform(
                multipart("/item")
                        .file(file)
                        .param("name", "item1")
                        .param("buyPrice", "10.4")
                        .param("firstBid", "5.3")
                        .param("categoriesId", categoryId)
                        .param("longitude", "23.76695")
                        .param("latitude", "37.968564")
                        .param("locationTitle", "Dit UoA")
                        .param("endsAt", "2021-09-26T01:30:00.000-04:00")
                        .param("description", "this is the description")
                        .header("Authorization", user1)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }


    /**
     * User successfully deletes an item
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Successful item deletion")
    public void deleteItem1() throws Exception {

        String item_id = TestUtils.makeItem(mvc, categoryId, user1);

        mvc.perform(delete("/item/" + item_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk());

        mvc.perform(get("/user/myAuctions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(0)));
    }


    /**
     * User successfully deletes an item with invalid item id
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Deletion - invalid id")
    public void deleteItem2() throws Exception {

        mvc.perform(delete("/item/142345")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isNotFound());
    }

    /**
     * User deletes an item but he does not own it
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Deletion - unauthorized")
    public void deleteItem3() throws Exception {

        String item_id = TestUtils.makeItem(mvc, categoryId, user1);

        mvc.perform(delete("/item/" + item_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user2))
                .andExpect(status().isUnauthorized());
    }

    /**
     * User deletes an item but the auction is already completed
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Deletion - completed auction")
    public void deleteItem4() throws Exception {

        String item_id = TestUtils.makeExpiredItem(mvc, categoryId, user1);

        mvc.perform(delete("/item/" + item_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isForbidden());
    }


    /**
     * User deletes an item but the auction has bids
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Deletion - bids")
    public void deleteItem5() throws Exception {

        String item_id = TestUtils.makeItem(mvc, categoryId, user1);

        mvc.perform(post("/bid/makeBid/" + item_id)
                .contentType(MediaType.APPLICATION_JSON)
                .param("offer", "6.0")
                .header("Authorization", user2))
                .andExpect(status().isOk());

        mvc.perform(delete("/item/" + item_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isForbidden());
    }


    /**
     * User gets item's details
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Get details")
    public void getItem1() throws Exception {

        String item_id = TestUtils.makeItem(mvc, categoryId, user1);

        mvc.perform(get("/item/" + item_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk());
    }


    /**
     * User gets item's details using invalid id
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Get details with invalid id")
    public void getItem2() throws Exception {

        mvc.perform(get("/item/123654")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isNotFound());
    }

//
// TODO remove
    /**
     * User gets item's details of an expired item
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Get details of expired item")
    public void getItem3() throws Exception {

        String item_id = TestUtils.makeExpiredItem(mvc, categoryId, user1);

        Thread.sleep(8000);
        mvc.perform(get("/item/" + item_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("auctionCompleted", is(true)));
    }


    /**
     * User gets a list of all open auctions
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Get all open auctions")
    public void getAllOpenAuctions1() throws Exception {

        String item1 = TestUtils.makeItem(mvc, categoryId, user1);
        String item2 = TestUtils.makeItem(mvc, categoryId, user1);
        String item3 = TestUtils.makeItem(mvc, categoryId, user1);

        String item4 = TestUtils.makeExpiredItem(mvc, categoryId, user1);

        Thread.sleep(6000);
        mvc.perform(get("/item/openAuctions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(3)));
    }

//TODO remove
    /**
     * User gets a list of all open auctions
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Get all open auctions 2")
    public void getAllOpenAuctions2() throws Exception {

        String item4 = TestUtils.makeExpiredItem(mvc, categoryId, user1);

        Thread.sleep(8000);
        mvc.perform(get("/item/openAuctions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(0)));
    }


    /**
     * User gets a list of all auctions
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Get all auctions")
    public void getAllAuctions1() throws Exception {

        String item1 = TestUtils.makeItem(mvc, categoryId, user2);
        String item2 = TestUtils.makeItem(mvc, categoryId, user3);
        String item3 = TestUtils.makeItem(mvc, categoryId, user3);

        String item4 = TestUtils.makeExpiredItem(mvc, categoryId, user1);

        mvc.perform(get("/item/allAuctions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(4)));
    }


    /**
     * User gets a list of all auctions
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Get all auctions 2")
    public void getAllAuctions2() throws Exception {

        String item4 = TestUtils.makeExpiredItem(mvc, categoryId, user1);

        mvc.perform(get("/item/allAuctions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(1)));
    }


    /**
     * User gets a list of all categories names
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Get categories names")
    public void getAllCategoriesNames1() throws Exception {

        makeAdmin("user3");

        mvc.perform(post("/admin/newCategory")
                .contentType(MediaType.APPLICATION_JSON)
                .param("name", "clothes")
                .header("Authorization", user3))
                .andExpect(status().isOk());

        mvc.perform(post("/admin/newCategory")
                .contentType(MediaType.APPLICATION_JSON)
                .param("name", "boats")
                .header("Authorization", user3))
                .andExpect(status().isOk());

        mvc.perform(get("/item/allCategories")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(3)));
    }


    /**
     * User gets a list of all categories names
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Get categories names 2")
    public void getAllCategoriesNames2() throws Exception {

        mvc.perform(get("/item/allCategories")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(1)));
    }


    /**
     * User modifies an item
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Modify item - name")
    public void modifyItem1() throws Exception {

        ItemCategory ic = itemCategoryRepository.findItemCategoryByName("All categories");
        String item = TestUtils.makeItem(mvc, ic.getId().toString(), user1);

        mvc.perform(patch("/item/" + item)
                .contentType(MediaType.APPLICATION_JSON)
                .param("name", "modified")
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is("modified")));
    }


    /**
     * User modifies an item
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Modify item - invalid id")
    public void modifyItem2() throws Exception {

        mvc.perform(patch("/item/12345")
                .contentType(MediaType.APPLICATION_JSON)
                .param("name", "modified")
                .header("Authorization", user1))
                .andExpect(status().isNotFound());
    }


    /**
     * User modifies an item
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Modify item - unauthorized user")
    public void modifyItem3() throws Exception {

        ItemCategory ic = itemCategoryRepository.findItemCategoryByName("All categories");
        String item = TestUtils.makeItem(mvc, ic.getId().toString(), user1);

        mvc.perform(patch("/item/" + item)
                .contentType(MediaType.APPLICATION_JSON)
                .param("name", "modified")
                .header("Authorization", user2))
                .andExpect(status().isUnauthorized());
    }


    /**
     * User modifies an item
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Modify item - after the first bid")
    public void modifyItem4() throws Exception {

        ItemCategory ic = itemCategoryRepository.findItemCategoryByName("All categories");
        String item = TestUtils.makeItem(mvc, ic.getId().toString(), user1);

        mvc.perform(post("/bid/makeBid/" + item)
                .contentType(MediaType.APPLICATION_JSON)
                .param("offer", "6.0")
                .header("Authorization", user2))
                .andExpect(status().isOk());

        mvc.perform(patch("/item/" + item)
                .contentType(MediaType.APPLICATION_JSON)
                .param("name", "modified")
                .header("Authorization", user1))
                .andExpect(status().isForbidden());
    }


    /**
     * User modifies an item
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Modify item - completed auction")
    public void modifyItem5() throws Exception {

        String item = TestUtils.makeExpiredItem(mvc, categoryId, user1);

        mvc.perform(patch("/item/" + item)
                .contentType(MediaType.APPLICATION_JSON)
                .param("name", "modified")
                .header("Authorization", user1))
                .andExpect(status().isForbidden());
    }


    /**
     * User modifies an item
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Modify item - description")
    public void modifyItem6() throws Exception {

        ItemCategory ic = itemCategoryRepository.findItemCategoryByName("All categories");
        String item = TestUtils.makeItem(mvc, ic.getId().toString(), user1);

        mvc.perform(patch("/item/" + item)
                .contentType(MediaType.APPLICATION_JSON)
                .param("description", "new description")
                .header("Authorization", user1))
                .andExpect(status().isOk());
    }


    /**
     * User modifies an item
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Modify item - new buyPrice")
    public void modifyItem7() throws Exception {

        ItemCategory ic = itemCategoryRepository.findItemCategoryByName("All categories");
        String item = TestUtils.makeItem(mvc, ic.getId().toString(), user1);

        mvc.perform(patch("/item/" + item)
                .contentType(MediaType.APPLICATION_JSON)
                .param("buyPrice", "15.0")
                .header("Authorization", user1))
                .andExpect(status().isOk());
    }


    /**
     * User modifies an item
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Modify item - buyPrice < item.firstBid")
    public void modifyItem8() throws Exception {

        String item = TestUtils.makeItem(mvc, categoryId, user1);

        mvc.perform(patch("/item/" + item)
                .contentType(MediaType.APPLICATION_JSON)
                .param("buyPrice", "3.0")
                .header("Authorization", user1))
                .andExpect(status().isBadRequest());
    }


    /**
     * User modifies an item
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Modify item - new buyPrice < new firstBid")
    public void modifyItem9() throws Exception {

        String item = TestUtils.makeItem(mvc, categoryId, user1);

        mvc.perform(patch("/item/" + item)
                .contentType(MediaType.APPLICATION_JSON)
                .param("buyPrice", "14.0")
                .param("firstBid", "16.2")
                .header("Authorization", user1))
                .andExpect(status().isBadRequest());
    }


    /**
     * User modifies an item
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Modify item - new buyPrice < new firstBid - 2")
    public void modifyItem10() throws Exception {

        String item = TestUtils.makeItem(mvc, categoryId, user1);

        mvc.perform(patch("/item/" + item)
                .contentType(MediaType.APPLICATION_JSON)
                .param("buyPrice", "2.1")
                .param("firstBid", "3.2")
                .header("Authorization", user1))
                .andExpect(status().isBadRequest());
    }


    /**
     * User modifies an item
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Modify item - new firstBid")
    public void modifyItem11() throws Exception {

        ItemCategory ic = itemCategoryRepository.findItemCategoryByName("All categories");
        String item = TestUtils.makeItem(mvc, ic.getId().toString(), user1);

        mvc.perform(patch("/item/" + item)
                .contentType(MediaType.APPLICATION_JSON)
                .param("firstBid", "7.0")
                .header("Authorization", user1))
                .andExpect(status().isOk());
    }


    /**
     * User modifies an item
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Modify item - firstBid > item.buyPrice")
    public void modifyItem12() throws Exception {

        String item = TestUtils.makeItem(mvc, categoryId, user1);

        mvc.perform(patch("/item/" + item)
                .contentType(MediaType.APPLICATION_JSON)
                .param("firstBid", "15.0")
                .header("Authorization", user1))
                .andExpect(status().isBadRequest());
    }


    /**
     * User modifies an item
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Modify item - new buyPrice > new firstBid")
    public void modifyItem13() throws Exception {

        String item = TestUtils.makeItem(mvc, categoryId, user1);

        mvc.perform(patch("/item/" + item)
                .contentType(MediaType.APPLICATION_JSON)
                .param("buyPrice", "19.0")
                .param("firstBid", "16.2")
                .header("Authorization", user1))
                .andExpect(status().isOk());
    }


    /**
     * User modifies an item
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Modify item - new buyPrice > new firstBid - 2")
    public void modifyItem14() throws Exception {

        String item = TestUtils.makeItem(mvc, categoryId, user1);

        mvc.perform(patch("/item/" + item)
                .contentType(MediaType.APPLICATION_JSON)
                .param("buyPrice", "3.1")
                .param("firstBid", "2.2")
                .header("Authorization", user1))
                .andExpect(status().isOk());
    }


    /**
     * User modifies an item
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Modify item - new buyPrice > new firstBid - 2")
    public void modifyItem15() throws Exception {

        String item = TestUtils.makeItem(mvc, categoryId, user1);

        mvc.perform(patch("/item/" + item)
                .contentType(MediaType.APPLICATION_JSON)
                .param("categoriesId", "000")
                .header("Authorization", user1))
                .andExpect(status().isNotFound());

        mvc.perform(get("/item/" + item)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("categories", hasSize(1)));

    }


    @Test
    @DisplayName("Get feed 1")
    public void getFeed1() throws Exception {

        for(int i = 0; i < 10; i++){
            TestUtils.makeDetailedItem
                    (mvc, categoryId, "item" + i, "fancy item", user1);
            Thread.sleep(100);
        }

        mvc.perform(get("/item/feed")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(5)))
                .andExpect(jsonPath("$[0].name", is("item9")))
                .andExpect(jsonPath("$[1].name", is("item8")))
                .andExpect(jsonPath("$[2].name", is("item7")))
                .andExpect(jsonPath("$[3].name", is("item6")))
                .andExpect(jsonPath("$[4].name", is("item5")));
    }


    @Test
    @DisplayName("Get feed 2")
    public void getFeed2() throws Exception {

        TestUtils.makeDetailedItem
                (mvc, categoryId, "item1", "fancy item", user1);
        TestUtils.makeDetailedItem
                (mvc, categoryId, "item2", "fancy item", user1);
        TestUtils.makeDetailedItem
                (mvc, categoryId, "item3", "fancy item", user1);


        mvc.perform(get("/item/feed")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(3)))
                .andExpect(jsonPath("$[0].name", is("item3")))
                .andExpect(jsonPath("$[1].name", is("item2")))
                .andExpect(jsonPath("$[2].name", is("item1")));
    }


    @Test
    @DisplayName("Get feed 3")
    public void getFeed3() throws Exception {

        mvc.perform(get("/item/feed")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(0)));
    }


    @Test
    @DisplayName("Get older auctions 1")
    public void getOlderAuctions1() throws Exception {

        mvc.perform(get("/item/older/12334556")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isNotFound());
    }


    @Test
    @DisplayName("Get older auctions 2")
    public void getOlderAuctions2() throws Exception {

        List<String> ids = new ArrayList();
        for(int i = 0; i < 10; i++){
            ids.add(TestUtils.makeDetailedItem
                    (mvc, categoryId, "item" + i, "fancy item", user1));
            Thread.sleep(100);
        }

        mvc.perform(get("/item/feed")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(5)))
                .andExpect(jsonPath("$[0].name", is("item9")))
                .andExpect(jsonPath("$[1].name", is("item8")))
                .andExpect(jsonPath("$[2].name", is("item7")))
                .andExpect(jsonPath("$[3].name", is("item6")))
                .andExpect(jsonPath("$[4].name", is("item5")));

        mvc.perform(get("/item/older/" + ids.get(5))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(5)))
                .andExpect(jsonPath("$[0].name", is("item4")))
                .andExpect(jsonPath("$[1].name", is("item3")))
                .andExpect(jsonPath("$[2].name", is("item2")))
                .andExpect(jsonPath("$[3].name", is("item1")))
                .andExpect(jsonPath("$[4].name", is("item0")));
    }


    @Test
    @DisplayName("Get older auctions 3")
    public void getOlderAuctions3() throws Exception {

        List<String> ids = new ArrayList();
        for(int i = 0; i < 8; i++){
            ids.add(TestUtils.makeDetailedItem
                    (mvc, categoryId, "item" + i, "fancy item", user1));
            Thread.sleep(100);
        }

        mvc.perform(get("/item/feed")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(5)))
                .andExpect(jsonPath("$[0].name", is("item7")))
                .andExpect(jsonPath("$[1].name", is("item6")))
                .andExpect(jsonPath("$[2].name", is("item5")))
                .andExpect(jsonPath("$[3].name", is("item4")))
                .andExpect(jsonPath("$[4].name", is("item3")));

        mvc.perform(get("/item/older/" + ids.get(3))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(3)))
                .andExpect(jsonPath("$[0].name", is("item2")))
                .andExpect(jsonPath("$[1].name", is("item1")))
                .andExpect(jsonPath("$[2].name", is("item0")));
    }


    @Test
    @DisplayName("Get older auctions 4")
    public void getOlderAuctions4() throws Exception {

        List<String> ids = new ArrayList();
        for(int i = 0; i < 5; i++){
            ids.add(TestUtils.makeDetailedItem
                    (mvc, categoryId, "item" + i, "fancy item", user1));
            Thread.sleep(100);
        }

        mvc.perform(get("/item/feed")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(5)))
                .andExpect(jsonPath("$[0].name", is("item4")))
                .andExpect(jsonPath("$[1].name", is("item3")))
                .andExpect(jsonPath("$[2].name", is("item2")))
                .andExpect(jsonPath("$[3].name", is("item1")))
                .andExpect(jsonPath("$[4].name", is("item0")));

        mvc.perform(get("/item/older/" + ids.get(0))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(0)));
    }

    @Test
    @DisplayName("Get older auctions 1")
    public void category() throws Exception {

        mvc.perform(get("/item/allCategories")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk());
    }
}