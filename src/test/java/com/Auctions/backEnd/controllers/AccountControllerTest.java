package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.BackEndApplication;
import com.Auctions.backEnd.TestUtils;
import com.Auctions.backEnd.configs.TestConfig;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = {TestConfig.class, BackEndApplication.class})
@AutoConfigureMockMvc

public class AccountControllerTest {

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

    @BeforeEach
    private void before() throws Exception {

        mvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        user1 = TestUtils.createAccount(mvc, "user1", "myPwd123", "FirstName1", "LastName1", "email1@di.uoa.gr");
        user2 = TestUtils.createAccount(mvc, "user2", "myPwd123", "FistName2", "LastName2", "email2@di.uoa.gr");
        user3 = TestUtils.createAccount(mvc, "user3", "myPwd123", "FirstName3", "LastName3", "email3@di.uoa.gr");
    }

    @AfterEach
    private void  after() {
        testUtils.clearDB();
    }


    /**
     * Check if the Username is already in the DB
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Check Username already exists")
    public void checkUsername1() throws Exception {

        mvc.perform(get("/account/checkUsername")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .param("username", "user1"))
                .andExpect(status().isBadRequest());
    }


    /**
     * Check if the Username is already in the DB
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Check Username does not exist")
    public void checkUsername2() throws Exception {

        mvc.perform(get("/account/checkUsername")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .param("username", "uniOfAthens"))
                .andExpect(status().isOk());
    }


    /**
     * Check if the mail is already in the DB
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Check mail already exist")
    public void checkMail1() throws Exception {

        mvc.perform(get("/account/checkEmail")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .param("email", "email1@di.uoa.gr"))
                .andExpect(status().isBadRequest());
    }


    /**
     * Check if the mail is already in the DB
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Check mail does not exist")
    public void checkMail2() throws Exception {

        mvc.perform(get("/account/checkEmail")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .param("email", "hello@hello.com"))
                .andExpect(status().isOk());
    }


    /**
     * User tries to access his account details with invalid token
     * We should get back an HTTP <code>FORBIDDEN</code>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User accesses his account details with invalid token")
    public void getAccount1() throws Exception {

        mvc.perform(get("/account")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "invalidToken"))
                .andExpect(status().isBadRequest());
    }


    /**
     * Check for the token assigned to a User
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("check token Bad Request")
    public void getAccount2() throws Exception {

        mvc.perform(get("/account")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", user1))
                .andExpect(jsonPath("username", is("user1")))
                .andExpect(status().isOk());
    }


    /**
     * User tries to change his password but his uses an wrong old password
     * We should get back an HTTP <code>BAD REQUEST</code>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User tries to change his password but his uses an wrong old password")
    public void changePassword1() throws Exception {

        mvc.perform(put("/account/changePassword")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", user1)
                .content("{ " +
                            "\"newPassword\": \"newPassword\",\n" +
                            "\"oldPassword\": \"wrongPassword\"\n" +
                         "}"))
                .andExpect(status().isForbidden());
    }


    /**
     * User successfully tries to change his password
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User successfully tries to change his password")
    public void changePassword2() throws Exception {

        mvc.perform(put("/account/changePassword")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("{ " +
                        "\"newPassword\": \"newPassword\",\n" +
                        "\"oldPassword\": \"myPwd123\"\n" +
                        "}")
                .header("Authorization", user1))
                .andExpect(status().isOk());
    }
}
