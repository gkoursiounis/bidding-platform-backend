package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.BackEndApplication;
import com.Auctions.backEnd.TestUtils;
import com.Auctions.backEnd.models.Account;
import com.Auctions.backEnd.models.ItemCategory;
import com.Auctions.backEnd.repositories.AccountRepository;
import com.Auctions.backEnd.repositories.ItemCategoryRepository;
import com.Auctions.backEnd.repositories.UserRepository;

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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.FileInputStream;
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

        mvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        user1 = createAccount(mvc, "user1", "myPwd123", "FirstName1", "LastName1", "email1@di.uoa.gr");
        user2 = createAccount(mvc, "user2", "myPwd123", "FirstName2", "LastName2", "email2@di.uoa.gr");
        user3 = createAccount(mvc, "user3", "myPwd123", "FirstName3", "LastName3", "email3@di.uoa.gr");

        ItemCategory category = new ItemCategory();
        category.setName("cat1");
        itemCategoryRepository.save(category);
        categoryId = category.getId().toString();
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
                .andExpect(status().isBadRequest());
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
}