package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.BackEndApplication;
import com.Auctions.backEnd.TestUtils;
import com.Auctions.backEnd.configs.TestConfig;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
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

    @Autowired
    private ItemCategoryRepository itemCategoryRepository;

    private String user1;
    private String user2;
    private String user3;

    @BeforeEach
    private void before() throws Exception {

        mvc = MockMvcBuilders.webAppContextSetup(this.wac)
                .alwaysDo(MockMvcResultHandlers.print())
                .build();

        user1 = TestUtils.createAccount(mvc, "user1", "myPwd123", "FirstName1", "LastName1", "email1@di.uoa.gr");
        user2 = TestUtils.createAccount(mvc, "user2", "myPwd123", "FistName2", "LastName2", "email2@di.uoa.gr");
        user3 = TestUtils.createAccount(mvc, "user3", "myPwd123", "FirstName3", "LastName3", "email3@di.uoa.gr");
    }

    @AfterEach
    public void  after() {
      //  this.testUtils.clearDB();
    }

    private void unverify(final String username) {

        Account account = accountRepository.findByUsername(username);
        assertNotNull(account);
        account.setVerified(false);
        accountRepository.save(account);
    }

    private void verify(final String username) {

        Account account = accountRepository.findByUsername(username);
        assertNotNull(account);
        account.setVerified(true);
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
     * Admin gets the list of unverified users
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Get pending registers - one user")
    public void getPendingRegisters1() throws Exception {

        makeAdmin("user3");
        unverify("user1");
        verify("user2");

        mvc.perform(get("/admin/pendingRegisters")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("content.*", hasSize(1)));
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
        verify("user1");
        verify("user2");

        mvc.perform(get("/admin/pendingRegisters")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("content.*", hasSize(0)));
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
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("content.*", hasSize(2)));
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
                .param("page", "0")
                .param("size", "10")
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
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "invalidToken"))
                .andExpect(status().isBadRequest());
    }


    /**
     * Admin gets the list of all users
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Get all users - get everyone")
    public void getAllUsers1() throws Exception {

        makeAdmin("user3");

        mvc.perform(get("/admin/allUsers")
                .contentType(MediaType.APPLICATION_JSON)
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", user3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("content.*", hasSize(4)));
    }


    /**
     * Admin gets the list of all users amd admins
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
                .param("page", "0")
                .param("size", "10")
                .header("Authorization", user3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("content.*", hasSize(4)));
    }


    /**
     * A User gets the list of all users using invalid token
     * We should get back an <HTTP>BAD REQUEST</HTTP>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Get all users - invalid token")
    public void getAllUsers3() throws Exception {

        mvc.perform(get("/admin/allUsers")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "invalidToken"))
                .andExpect(status().isBadRequest());
    }


    /**
     * A User gets the list of all users
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Get all users 1")
    public void getAllUsers4() throws Exception {

        for(int i=4; i<15; i++){
            TestUtils.createAccount(mvc, "user" + i, "myPwd123", "FirstName1",
                    "LastName1", "email" + i + "@di.uoa.gr");

        }
        makeAdmin("user3");

        mvc.perform(get("/admin/allUsers")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("content.*", hasSize(10)));

        mvc.perform(get("/admin/allUsers")
                .contentType(MediaType.APPLICATION_JSON)
                .param("page", "1")
                .param("size", "10")
                .header("Authorization", user3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("content.*", hasSize(5)));
    }


    /**
     * A User gets the list of all users
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Get all users 2")
    public void getAllUsers6() throws Exception {

        for(int i=4; i<15; i++){
            TestUtils.createAccount(mvc, "user" + i, "myPwd123", "FirstName1",
                    "LastName1", "email" + i + "@di.uoa.gr");

        }
        makeAdmin("user3");

        mvc.perform(get("/admin/allUsers")
                .param("page", "0")
                .param("size", "5")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("content.*", hasSize(5)));

        mvc.perform(get("/admin/allUsers")
                .contentType(MediaType.APPLICATION_JSON)
                .param("page", "1")
                .param("size", "5")
                .header("Authorization", user3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("content.*", hasSize(5)));

        mvc.perform(get("/admin/allUsers")
                .contentType(MediaType.APPLICATION_JSON)
                .param("page", "1")
                .param("size", "5")
                .header("Authorization", user3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("content.*", hasSize(5)));
    }


    @Test
    @DisplayName("Get all users - no admin")
    public void getAllUsers5() throws Exception {

        mvc.perform(get("/admin/allUsers")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isUnauthorized());
    }


    /**
     * Admin verifies all pending users in once
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Verify all users - successful")
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
                .andExpect(jsonPath("content.*", hasSize(0)));
    }


    /**
     * A User verifies all pending users
     * We should get back an <HTTP>UNAUTHORIZED</HTTP>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Verify all users - no admin")
    public void verifyAllUsers2() throws Exception {

        mvc.perform(patch("/admin/verifyAll")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isUnauthorized());
    }


    /**
     * A User verifies all pending users using invalid token
     * We should get back an <HTTP>BAD REQUEST</HTTP>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Verify all users - invalid token")
    public void verifyAllUsers3() throws Exception {

        mvc.perform(get("/admin/verifyAll")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "invalidToken"))
                .andExpect(status().isBadRequest());
    }


    /**
     * A User verifies another user
     * We should get back an <HTTP>UNAUTHORIZED</HTTP>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Verify user - no admin")
    public void verifyUser1() throws Exception {

        String user2_id = TestUtils.getUserToString(mvc, user2,"user2");

        mvc.perform(patch("/admin/verifyUser/" + user2_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isUnauthorized());
    }


    /**
     * A User verifies another user using invalid token
     * We should get back an <HTTP>BAD REQUEST</HTTP>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Verify user - invalid token")
    public void verifyUser2() throws Exception {

        String user2_id = TestUtils.getUserToString(mvc, user2,"user2");

        mvc.perform(patch("/admin/verifyUser/" + user2_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "invalidToken"))
                .andExpect(status().isBadRequest());
    }


    /**
     * Admin verifies a user using invalid userId
     * We should get back an <HTTP>NOT FOUND</HTTP>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Verify user - invalid user id")
    public void verifyUser3() throws Exception {

        makeAdmin("user3");

        mvc.perform(patch("/admin/verifyUser/123")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user3))
                .andExpect(status().isNotFound());
    }


    /**
     * Admin verifies an already verified user
     * We should get back an <HTTP>BAD REQUEST</HTTP>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Verify user - already verified")
    public void verifyUser4() throws Exception {

        makeAdmin("user3");
        verify("user2");
        String user2_id = TestUtils.getUserToString(mvc, user2,"user2");

        mvc.perform(patch("/admin/verifyUser/" + user2_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user3))
                .andExpect(status().isBadRequest());
    }


    /**
     * Admin successfully verifies a user
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Verify user - successful")
    public void verifyUser5() throws Exception {

        makeAdmin("user3");
        unverify("user2");
        verify("user1");

        String user2_id = TestUtils.getUserToString(mvc, user2,"user2");

        String ver_before = ((JSONObject) new JSONParser().parse(TestUtils.getUser(mvc, user2,"user2")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()))
                .get("verified").toString();

        assertEquals(ver_before, "false");

        mvc.perform(patch("/admin/verifyUser/" + user2_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user3))
                .andExpect(status().isOk());

        String ver_after = ((JSONObject) new JSONParser().parse(TestUtils.getUser(mvc, user2,"user2")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()))
                .get("verified").toString();

        assertEquals(ver_after, "true");
    }


    /**
     * Admin creates a new item category
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Create category - successful")
    public void createItemCategory1() throws Exception {

        makeAdmin("user3");

        ItemCategory ic = itemCategoryRepository.findItemCategoryByName("All categories");

        mvc.perform(post("/admin/newCategory/" + ic.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .param("name", "clothes")
                .header("Authorization", user3))
                .andExpect(status().isOk());

        mvc.perform(get("/item/allCategories")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk());
    }


    /**
     * Admin creates a new item category with a null name
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Create category - null name")
    public void createItemCategory2() throws Exception {

        makeAdmin("user3");

        mvc.perform(post("/admin/newCategory")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user3))
                .andExpect(status().isBadRequest());
    }


    /**
     * Admin creates a new item category with an empty name
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Create category - empty name")
    public void createItemCategory3() throws Exception {

        makeAdmin("user3");

        mvc.perform(post("/admin/newCategory")
                .contentType(MediaType.APPLICATION_JSON)
                .param("name", "")
                .header("Authorization", user3))
                .andExpect(status().isBadRequest());
    }


    /**
     * Admin creates a new item category but it already exists
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Create category - name exists")
    public void createItemCategory4() throws Exception {

        makeAdmin("user3");

        mvc.perform(post("/admin/newCategory")
                .contentType(MediaType.APPLICATION_JSON)
                .param("name", "clothes")
                .header("Authorization", user3))
                .andExpect(status().isOk());

        mvc.perform(post("/admin/newCategory")
                .contentType(MediaType.APPLICATION_JSON)
                .param("name", "clothes")
                .header("Authorization", user3))
                .andExpect(status().isBadRequest());
    }


    /**
     * A User creates a new item category
     * We should get back an <HTTP>UNAUTHORIZED</HTTP>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Create category - no admin")
    public void createItemCategory5() throws Exception {

        mvc.perform(post("/admin/newCategory")
                .contentType(MediaType.APPLICATION_JSON)
                .param("name", "clothes")
                .header("Authorization", user1))
                .andExpect(status().isUnauthorized());
    }


    /**
     * A User creates a new item category using invalid token
     * We should get back an <HTTP>BAD REQUEST</HTTP>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Create category - invalid token")
    public void createItemCategory6() throws Exception {

        mvc.perform(post("/admin/newCategory")
                .contentType(MediaType.APPLICATION_JSON)
                .param("name", "clothes")
                .header("Authorization", "invalidToken"))
                .andExpect(status().isBadRequest());
    }


    /**
     * Admin another user using invalid token
     * We should get back an <HTTP>BAD REQUEST</HTTP>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Delete user - invalid token")
    public void deleteUser1() throws Exception {

        String user2_id = TestUtils.getUserToString(mvc, user2,"user2");

        mvc.perform(delete("/admin/deleteUser/" + user2_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "invalidToken"))
                .andExpect(status().isBadRequest());
    }


    /**
     * A User deletes another user
     * We should get back an <HTTP>UNAUTHORIZED</HTTP>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Delete user - no admin")
    public void deleteUser2() throws Exception {

        String user2_id = TestUtils.getUserToString(mvc, user2,"user2");

        mvc.perform(delete("/admin/deleteUser/" + user2_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isUnauthorized());
    }


    /**
     * Admin deletes another admin
     * We should get back an <HTTP>BAD REQUEST</HTTP>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Delete admin")
    public void deleteUser3() throws Exception {

        makeAdmin("user3");
        makeAdmin("user2");
        String user2_id = TestUtils.getUserToString(mvc, user2, "user2");

        mvc.perform(delete("/admin/deleteUser/" + user2_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user3))
                .andExpect(status().isBadRequest());
    }


    /**
     * Admin deletes a user
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Delete successfully a user")
    public void deleteUser4() throws Exception {

        makeAdmin("user3");
        String user2_id = TestUtils.getUserToString(mvc, user2, "user2");

        mvc.perform(delete("/admin/deleteUser/" + user2_id)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user3))
                .andExpect(status().isOk());

        mvc.perform(get("/admin/allUsers")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("content.*", hasSize(3)));


        mvc.perform(get("/account/checkUsername")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .param("username", "user2"))
                .andExpect(status().isOk());
    }


    @Test
    @DisplayName("Get all users - invalid token")
    public void getAllItems() throws Exception {

        mvc.perform(get("/admin/allAuctions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk());
    }
}