package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.BackEndApplication;
import com.Auctions.backEnd.TestUtils;
import com.Auctions.backEnd.configs.TestConfig;
import com.Auctions.backEnd.models.Account;
import com.Auctions.backEnd.repositories.AccountRepository;
import com.Auctions.backEnd.repositories.ConfirmationTokenRepository;
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


import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
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
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConfirmationTokenRepository confirmationTokenRepository;

    private String user1;
    private String user2;
    private String user3;

    @BeforeEach
    private void before() throws Exception {

        testUtils.clearDB();

        user1 = TestUtils.createAccount(mvc, "user1", "myPwd123", "FirstName1", "LastName1", "email1@usi.ch");
        user2 = TestUtils.createAccount(mvc, "user2", "myPwd123", "FistName2", "LastName2", "email2@usi.ch");
        user3 = TestUtils.createAccount(mvc, "user3", "myPwd123", "FirstName3", "LastName3", "email3@usi.ch");
    }

    @AfterEach
    private void  after() {
        testUtils.clearDB();
    }

    private void verify(final String username) {

        Account account = accountRepository.findByUsername(username);
        assertNotNull(account);
        account.setVerified(true);
        accountRepository.save(account);
    }

    private void unverify(final String username) {

        Account account = accountRepository.findByUsername(username);
        assertNotNull(account);
        account.setVerified(false);
        accountRepository.save(account);
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
                .param("username", "Jon Snow Muore"))
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
                .param("email", "email1@usi.ch"))
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
                .andExpect(status().isForbidden());
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


//    /**
//     * Check for the token assigned to a User
//     *
//     * @throws Exception - mvc.perform throws exception
//     */
//    @Test
//    @DisplayName("check token Bad Request")
//    public void confirmation1() throws Exception {
//
//        mvc.perform(get("/account/confirm")
//                .contentType(MediaType.APPLICATION_JSON_VALUE)
//                .param("token", "Hello"))
//                .andExpect(status().isBadRequest());
//    }
//
//
//    /**
//     * Users tries to confirm himself while he is already isVerified
//     * We should get back an HTTP <code>BAD REQUEST</code>
//     *
//     * @throws Exception - mvc.perform throws exception
//     */
//    @Test
//    @DisplayName("Users confirms himself while he is already isVerified")
//    public void confirmation2() throws Exception {
//
//        verify("user1");
//
//        String token = confirmationTokenRepository.findByAccount_Id(
//                Long.parseLong(TestUtils.getAccountId(mvc, user1))).getConfirmationToken();
//
//        mvc.perform(get("/account/confirm")
//                .contentType(MediaType.APPLICATION_JSON_VALUE)
//                .param("token", token))
//                .andExpect(status().isBadRequest());
//    }
//
//
//    /**
//     * Users successfully tries to confirm himself
//     *
//     * @throws Exception - mvc.perform throws exception
//     */
//    @Test
//    @DisplayName("Users successfully tries to confirm himself")
//    public void confirmation3() throws Exception {
//
//        unverify("user1");
//
//        String token = confirmationTokenRepository.findByAccount_Id(
//                Long.parseLong(TestUtils.getAccountId(mvc, user1))).getConfirmationToken();
//
//        mvc.perform(get("/account/confirm")
//                .contentType(MediaType.APPLICATION_JSON_VALUE)
//                .param("token", token))
//                .andExpect(status().isOk());
//    }


    /**
     * Users tries to confirm himself while his account is deleted
     * We should get back an HTTP <code>BAD REQUEST</code>
     *
     * @throws Exception - mvc.perform throws exception
     */
//    @Test
//    @DisplayName("Users confirms himself while his account is deleted")
//    public void confirmation4() throws Exception {
//
//        ConfirmationToken token = confirmationTokenRepository.findByAccount_Id(
//                Long.parseLong(getAccountId(mvc, user1)));
//
//        accountRepository.deleteAll();
//        accountRepository.flush();
//
//        mvc.perform(get("/account/confirm")
//                .contentType(MediaType.APPLICATION_JSON_VALUE)
//                .param("token", token.getConfirmationToken()))
//                .andExpect(status().isOk());
//    }


    /**
     * check verification account
     *
     * @throws Exception - mvc.perform throws exception
     */
//    @Test
//    @DisplayName("resend-verify")
//    public void resendVerify1() throws Exception {
//
//        mvc.perform(get("/account/resend-verify")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", user1))
//                .andExpect(status().isBadRequest());
//    }
//
//
//    /**
//     * check verification account token forbidden
//     *
//     * @throws Exception - mvc.perform throws exception
//     */
//    @Test
//    @DisplayName("resend-verify forbidden")
//    public void resendVerify2() throws Exception {
//        mvc.perform(get("/account/resend-verify")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Token"))
//                .andExpect(status().isForbidden());
//
//    }
//
//
//    /**
//     * User successfully re-sends account verification
//     *
//     * @throws Exception - mvc.perform throws exception
//     */
//    @Test
//    @DisplayName("resend-verify ok")
//    public void resendVerify3() throws Exception {
//
//        unverify("user1");
//
//        mvc.perform(get("/account/resend-verify")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", user1))
//                .andExpect(status().isOk());
//
//    }
//
//
//    /**
//     * check verification account token forbidden
//     *
//     * @throws Exception - mvc.perform throws exception
//     */
//    @Test
//    @DisplayName("resend-verify ok")
//    public void resendVerify4() throws Exception {
//
//        unverify("user1");
//
//        confirmationTokenRepository.deleteAll();
//        confirmationTokenRepository.flush();
//
//        mvc.perform(get("/account/resend-verify")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", user1))
//                .andExpect(status().isBadRequest());
//    }


    /**
     * User tries to change his password but his uses an wrong old password
     * We should get back an HTTP <code>BAD REQUEST</code>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User tries to change his password but his uses an wrong old password")
    public void changePassword1() throws Exception {

        mvc.perform(put("/account/change-password")
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

        mvc.perform(put("/account/change-password")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("{ " +
                        "\"newPassword\": \"newPassword\",\n" +
                        "\"oldPassword\": \"myPwd123\"\n" +
                        "}")
                .header("Authorization", user1))
                .andExpect(status().isOk());
    }


    /**
     * post reset-password request with correct email
     *
     * @throws Exception - mvc.perform throws exception
     */
//
//    @Test
//    @DisplayName("get-reset-password")
//    public void getResetPassword1() throws Exception {
//        final String email = "email1@usi.ch";
//        final String content = "{" +
//                "\"email\" : \"" + email + "\"}";
//        mvc.perform(post("/account/get-reset-password")
//                .contentType(MediaType.APPLICATION_JSON_VALUE)
//                .header("authorization", user1)
//                .content(content))
//                .andExpect(status().isOk());
//    }
//
//    /**
//     * post reset-password request with wrong email
//     *
//     * @throws Exception - mvc.perform throws exception
//     */
//
//    @Test
//    @DisplayName("get-reset-password Wrong")
//    public void getResetPassword2() throws Exception {
//        final String email = "pinco@pallino.com";
//        final String content = "{" +
//                "\"email\" : \"" + email + "\"}";
//        mvc.perform(post("/account/get-reset-password")
//                .contentType(MediaType.APPLICATION_JSON_VALUE)
//                .header("authorization", user1)
//                .content(content))
//                .andExpect(status().isNotFound());
//    }
//
//    /**
//     * post reset-password request with wrong email
//     *
//     * @throws Exception - mvc.perform throws exception
//     */
//
//    @Test
//    @DisplayName("retry-reset-password")
//    public void retryResetPassword1() throws Exception {
//        final String email = "email1@usi.ch";
//        final String content = "{" +
//                "\"email\" : \"" + email + "\"}";
//        mvc.perform(post("/account/retry-reset-password")
//                .contentType(MediaType.APPLICATION_JSON_VALUE)
//                .header("authorization", user1)
//                .content(content))
//                .andExpect(status().isOk());
//    }
//
//    /**
//     * post reset-password request with wrong email
//     *
//     * @throws Exception - mvc.perform throws exception
//     */
//
//    @Test
//    @DisplayName("retry-reset-password Wrong")
//    public void retryResetPassword2() throws Exception {
//        final String email = "pinco@pallino.com";
//        final String content = "{" +
//                "\"email\" : \"" + email + "\"}";
//        mvc.perform(post("/account/retry-reset-password")
//                .contentType(MediaType.APPLICATION_JSON_VALUE)
//                .header("authorization", user1)
//                .content(content))
//                .andExpect(status().isBadRequest());
//    }
//
//
//    /**
//     * Users tries to reset-password request but request is not present
//     * We should get back an HTTP <code>BAD REQUEST</code>
//     *
//     * @throws Exception - mvc.perform throws exception
//     */
//
//    @Test
//    @DisplayName("retry-reset-password not present")
//    public void retryResetPassword3() throws Exception {
//
//        confirmationTokenRepository.deleteAll();
//
//        mvc.perform(post("/account/retry-reset-password")
//                .contentType(MediaType.APPLICATION_JSON_VALUE)
//                .header("authorization", user1)
//                .content("{\"email\": \"email1@usi.ch\"}"))
//                .andExpect(status().isBadRequest());
//    }


//    @Test
//    @DisplayName("reset-password")
//    public void resetPassword1() throws Exception {
//
//        final String token = "Invalid Token";
//        final String content = "{" +
//                "\"token\" : \"" + token + "\"}";
//
//        mvc.perform(post("/account/reset-password")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("authorization", user1)
//                .content(content))
//                .andExpect(status().isBadRequest());
//    }
//
//
//    @Test
//    @DisplayName("reset-password")
//    public void resetPassword2() throws Exception {
//
//        String token = confirmationTokenRepository.findByAccount_Id(
//                Long.parseLong(TestUtils.getAccountId(mvc, user1))).getConfirmationToken();
//
//        mvc.perform(post("/account/reset-password")
//                .contentType(MediaType.APPLICATION_JSON_VALUE)
//                .header("authorization", user1)
//                .content("{ " +
//                        "\"newPassword\": \"newPassword\",\n" +
//                        "\"token\": \"" + token + "\"\n" +
//                        "}"))
//                .andExpect(status().isOk());
//    }
}
