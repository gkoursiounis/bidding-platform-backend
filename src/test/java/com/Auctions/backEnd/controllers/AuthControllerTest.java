package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.BackEndApplication;
import com.Auctions.backEnd.TestUtils;
import com.Auctions.backEnd.configs.TestConfig;
import com.Auctions.backEnd.models.Account;
import com.Auctions.backEnd.repositories.AccountRepository;
import org.json.simple  .JSONObject;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(classes = {TestConfig.class, BackEndApplication.class})
@AutoConfigureMockMvc
public class AuthControllerTest{

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private WebApplicationContext wac;

	@Autowired
	private MockMvc mvc;

    @Autowired
    private AccountRepository accountRepository;

    private String user1;
    private String user2;
    private String user3;


    @BeforeEach
    public void before() throws Exception {

        mvc = MockMvcBuilders.webAppContextSetup(this.wac)
                .alwaysDo(MockMvcResultHandlers.print())
                .build();

        user1 = TestUtils.createAccount(mvc, "user1", "myPwd123", "FirstName1", "LastName1", "email1@di.uoa.gr");
        user2 = TestUtils.createAccount(mvc, "user2", "myPwd123", "FirstName2", "LastName2", "email2@di.uoa.gr");
        user3 = TestUtils.createAccount(mvc, "user3", "myPwd123", "FirstName3", "LastName3", "email3@di.uoa.gr");
    }

    @AfterEach
    public void  after() {
        this.testUtils.clearDB();
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
     * Helper function to perform a signup
     *
     * @param content - the content given for the signup
     * @return  return the response
     * @throws Exception mvc perform throws Exception
     */
    private ResultActions performSignup(final String content) throws Exception {

	    return mvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content));
    }

    /**
     * Helper function to perform a signup
     *
     * @param username - the name for the login
     * @return  return the response
     * @throws Exception mvc perform throws Exception
     */
    private ResultActions checkUsername(final String username) throws Exception {

	    return mvc.perform(get("/account/checkUsername")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("\"username\" : \"" + username + "\""));
    }


    /**
     * Signup using an already taken username
     *
     * @throws Exception - mvc.perform in performSignup() throws exception
     */
    @Test
    @DisplayName("Username Taken")
    public void signup1() throws Exception {

        final String content = "{" +
                                "\"username\" : \"user1\", " +
                                "\"password\" : \"myPwd123\", " +
                                "\"email\" : \"email0@di.uoa.gr\", " +
                                "\"firstName\" : \"FirstName1\", " +
                                "\"lastName\" : \"LastName1\", " +
                                "\"telNumber\" : \"1234567890\", " +
                                "\"taxNumber\" : \"123345\", " +
                                "\"longitude\" : \"23.76695\", " +
                                "\"latitude\" : \"37.968564\", " +
                                "\"locationTitle\" : \"Dit Uoa\" " +
                                "}";

        performSignup(content)
                .andExpect(status().isBadRequest());
    }


    /**
     * Signup using an already taken email
     *
     * @throws Exception - mvc.perform in performSignup() throws exception
     */
    @Test
    @DisplayName("Email Taken")
    public void signup2() throws Exception {

	    final String content = "{" +
                                "\"username\" : \"user0\", " +
                                "\"password\" : \"myPwd123\", " +
                                "\"email\" : \"email1@di.uoa.gr\", " +
                                "\"firstName\" : \"FirstName1\", " +
                                "\"lastName\" : \"LastName1\", " +
                                "\"telNumber\" : \"1234567890\", " +
                                "\"taxNumber\" : \"123345\", " +
                                "\"longitude\" : \"23.76695\", " +
                                "\"latitude\" : \"37.968564\", " +
                                "\"locationTitle\" : \"Dit Uoa\" " +
                                "}";

        performSignup(content)
                .andExpect(status().isBadRequest());
    }


    /**
     * Signup without username. User should not exist after signing up
     *
     * @throws Exception - mvc.perform in performSignup() and checkEmail() throws exception
     */
    @Test
    @DisplayName("Signup without username")
    public void signup3() throws Exception {

	    final String email = "email0@di.uoa.gr";

	    final String content = "{" +
                                "\"password\" : \"myPwd123\", " +
                                "\"email\" : \"" + email + "\", " +
                                "\"firstName\" : \"FirstName0\", " +
                                "\"lastName\" : \"LastName1\", " +
                                "\"telNumber\" : \"1234567890\", " +
                                "\"taxNumber\" : \"123345\", " +
                                "\"longitude\" : \"23.76695\", " +
                                "\"latitude\" : \"37.968564\", " +
                                "\"locationTitle\" : \"Dit Uoa\" " +
                                "}";

        performSignup(content)
                .andExpect(status().isBadRequest());

        mvc.perform(get("/account/checkEmail")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("\"email\" : \"" + email + "\""))
                .andExpect(status().isBadRequest());
    }


    /**
     * Signup without email. User should not exist after signing up
     *
     * @throws Exception - mvc.perform in performSignup() and checkUsername() throws exception
     */
    @Test
    @DisplayName("Signup without email")
    public void signup4() throws Exception {

        final String username = "user0" ;

        final String content = "{" +
                "\"username\" : \"" + username + "\", " +
                "\"password\" : \"myPwd123\", " +
                "\"firstName\" : \"FirstName11\", " +
                "\"lastName\" : \"LastName1\", " +
                "\"telNumber\" : \"1234567890\", " +
                "\"taxNumber\" : \"123345\", " +
                "\"longitude\" : \"23.76695\", " +
                "\"latitude\" : \"37.968564\", " +
                "\"locationTitle\" : \"Dit Uoa\" " +
                "}";

        performSignup(content)
                .andExpect(status().isBadRequest());

        checkUsername(username)
                .andExpect(status().isBadRequest());
    }


    /**
     * Signup without password. User should not exist after signing up
     *
     * @throws Exception - mvc.perform in performSignup() and checkUsername() throws exception
     */
    @Test
    @DisplayName("Signup without password")
    public void signup5() throws Exception {

        final String username = "user0" ;

        final String content = "{" +
                "\"username\" : \"" + username +"\", " +
                "\"email\" : \"email0@di.uoa.gr\", " +
                "\"firstName\" : \"FirstName01\", " +
                "\"lastName\" : \"LastName1\", " +
                "\"telNumber\" : \"1234567890\", " +
                "\"taxNumber\" : \"123345\", " +
                "\"longitude\" : \"23.76695\", " +
                "\"latitude\" : \"37.968564\", " +
                "\"locationTitle\" : \"Dit Uoa\" " +
                "}";

        performSignup(content)
                .andExpect(status().isBadRequest());

        checkUsername(username)
                .andExpect(status().isBadRequest());
    }


    /**
     * Signup without first name. User should not exist after signing up
     *
     * @throws Exception - mvc.perform in performSignup() and checkUsername() throws exception
     */
    @Test
    @DisplayName("Signup without first name")
    public void signup6() throws Exception {

        final String username = "user4" ;

        final String content = "{" +
                "\"username\" : \"" + username +"\", " +
                "\"email\" : \"email4@di.uoa.gr\", " +
                "\"password\" : \"myPwd123\", " +
                "\"lastName\" : \"LastName1\", " +
                "\"telNumber\" : \"1234567890\", " +
                "\"taxNumber\" : \"123345\", " +
                "\"longitude\" : \"23.76695\", " +
                "\"latitude\" : \"37.968564\", " +
                "\"locationTitle\" : \"Dit Uoa\" " +
                "}";

        performSignup(content)
                .andExpect(status().isBadRequest());

        checkUsername(username)
                .andExpect(status().isBadRequest());
    }


    /**
     * Signup without last name. User should not exist after signing up
     *
     * @throws Exception - mvc.perform in performSignup() and checkUsername() throws exception
     */
    @Test
    @DisplayName("Signup without last name")
    public void signup7() throws Exception {

        final String username = "user0" ;

        final String content = "{" +
                "\"username\" : \"" + username + "\", " +
                "\"email\" : \"email0@di.uoa.gr\", " +
                "\"firstName\" : \"FirstName01\", " +
                "\"password\" : \"myPwd123\", " +
                "\"telNumber\" : \"1234567890\", " +
                "\"taxNumber\" : \"123345\", " +
                "\"longitude\" : \"23.76695\", " +
                "\"latitude\" : \"37.968564\", " +
                "\"locationTitle\" : \"Dit Uoa\" " +
                "}";

        performSignup(content)
                .andExpect(status().isBadRequest());

        checkUsername(username)
                .andExpect(status().isBadRequest());
    }

    /**
     * Signup using black list words as username (eg. signup, admin, user etc)
     *
     * @throws Exception - mvc.perform in performSignup() throws exception
     */
    @Test
    @DisplayName("Signup using blacklisted name")
    public void signup8() throws Exception {

        final String content = "{" +
                                "\"username\" : \"signup\", " +
                                "\"password\" : \"myPwd123\", " +
                                "\"email\" : \"email0@di.uoa.gr\", " +
                                "\"firstName\" : \"FirstName1\", " +
                                "\"lastName\" : \"LastName1\", " +
                                "\"telNumber\" : \"1234567890\", " +
                                "\"taxNumber\" : \"123345\", " +
                                "\"longitude\" : \"23.76695\", " +
                                "\"latitude\" : \"37.968564\", " +
                                "\"locationTitle\" : \"Dit Uoa\" " +
                                "}";

        performSignup(content)
                .andExpect(status().isBadRequest());
    }


    /**
     * Signup with username less than 5 characters
     *
     * @throws Exception - mvc.perform in performSignup() throws exception
     */
    @Test
    @DisplayName("Signup with short username")
    public void signup9() throws Exception {

        final String content = "{" +
                "\"username\" : \"u\", " +
                "\"password\" : \"myPwd123\", " +
                "\"email\" : \"email4@di.uoa.gr\", " +
                "\"firstName\" : \"FirstName1\", " +
                "\"lastName\" : \"LastName1\", " +
                "\"telNumber\" : \"1234567890\", " +
                "\"taxNumber\" : \"123345\", " +
                "\"longitude\" : \"23.76695\", " +
                "\"latitude\" : \"37.968564\", " +
                "\"locationTitle\" : \"Dit Uoa\" " +
                "}";

        performSignup(content)
                .andExpect(status().isBadRequest());
    }

    /**
     * Signup with username more than 15 characters
     *
     * @throws Exception - mvc.perform in performSignup() throws exception
     */
    @Test
    @DisplayName("Signup with long username")
    public void signup10() throws Exception {

        final String content = "{" +
                "\"username\" : \"thisIsALargeUsername\", " +
                "\"password\" : \"myPwd123\", " +
                "\"email\" : \"email4@di.uoa.gr\", " +
                "\"firstName\" : \"FirstName1\", " +
                "\"lastName\" : \"LastName1\", " +
                "\"telNumber\" : \"1234567890\", " +
                "\"taxNumber\" : \"123345\", " +
                "\"longitude\" : \"23.76695\", " +
                "\"latitude\" : \"37.968564\", " +
                "\"locationTitle\" : \"Dit Uoa\" " +
                "}";

        performSignup(content)
                .andExpect(status().isBadRequest());
    }

    /**
     * Successful signup
     *
     * @throws Exception - mvc.perform in performSignup() throws exception
     */
    @Test
    @DisplayName("Successful signup")
    public void signup11() throws Exception {

        final String username = "user4";
        final String firstName = "firstName";
        final String lastName = "lastName";
        final String telNumber = "1234567890";
        final String taxNumber = "12345";

        final String content = "{" +
                "\"username\" : \"" + username + "\", " +
                "\"password\" : \"myPwd123\", " +
                "\"email\" : \"email4@di.uoa.gr\", " +
                "\"firstName\" : \"" + firstName + "\", " +
                "\"lastName\" : \"" + lastName + "\", " +
                "\"telNumber\" : \"" + telNumber + "\", " +
                "\"taxNumber\" : \"" + taxNumber + "\", " +
                "\"longitude\" : \"23.76695\", " +
                "\"latitude\" : \"37.968564\", " +
                "\"locationTitle\" : \"Dit Uoa\" " +
                "}";

        performSignup(content)
                .andExpect(status().isOk())
                .andExpect(jsonPath("user.username", is(username)))
                .andExpect(jsonPath("user.firstName", is(firstName)))
                .andExpect(jsonPath("user.lastName", is(lastName)))
                .andExpect(jsonPath("user.telNumber", is(telNumber)))
                .andExpect(jsonPath("user.taxNumber", is(taxNumber)));
    }


    /**
     * Signup using black list words as username (eg. signup, admin, user etc)
     *
     * @throws Exception - mvc.perform in performSignup() throws exception
     */
    @Test
    @DisplayName("Signup using blacklisted name - admin")
    public void signup13() throws Exception {

        final String content = "{" +
                "\"username\" : \"admin\", " +
                "\"password\" : \"myPwd123\", " +
                "\"email\" : \"email7@di.uoa.gr\", " +
                "\"firstName\" : \"FirstName1\", " +
                "\"lastName\" : \"LastName1\", " +
                "\"telNumber\" : \"1234567890\", " +
                "\"taxNumber\" : \"123345\", " +
                "\"longitude\" : \"23.76695\", " +
                "\"latitude\" : \"37.968564\", " +
                "\"locationTitle\" : \"Dit Uoa\" " +
                "}";

        performSignup(content)
                .andExpect(status().isBadRequest());
    }


    /**
     * Signup without phone number. User should not exist after signing up
     *
     * @throws Exception - mvc.perform in performSignup() and checkUsername() throws exception
     */
    @Test
    @DisplayName("Signup without phone number")
    public void signup14() throws Exception {

        final String content = "{" +
                "\"username\" : \"user0\", " +
                "\"email\" : \"email0@di.uoa.gr\", " +
                "\"firstName\" : \"FirstName01\", " +
                "\"lastName\" : \"LastName1\", " +
                "\"password\" : \"myPwd123\", " +
                "\"taxNumber\" : \"123345\", " +
                "\"longitude\" : \"23.76695\", " +
                "\"latitude\" : \"37.968564\", " +
                "\"locationTitle\" : \"Dit Uoa\" " +
                "}";

        performSignup(content)
                .andExpect(status().isBadRequest());
    }


    /**
     * Signup without tax number. User should not exist after signing up
     *
     * @throws Exception - mvc.perform in performSignup() and checkUsername() throws exception
     */
    @Test
    @DisplayName("Signup without tax number")
    public void signup15() throws Exception {

        final String username = "user0" ;

        final String content = "{" +
                "\"username\" : \"" + username + "\", " +
                "\"email\" : \"email0@di.uoa.gr\", " +
                "\"firstName\" : \"FirstName01\", " +
                "\"lastName\" : \"LastName1\", " +
                "\"password\" : \"myPwd123\", " +
                "\"telNumber\" : \"1234567890\", " +
                "\"longitude\" : \"23.76695\", " +
                "\"latitude\" : \"37.968564\", " +
                "\"locationTitle\" : \"Dit Uoa\" " +
                "}";

        performSignup(content)
                .andExpect(status().isBadRequest());

        checkUsername(username)
                .andExpect(status().isBadRequest());
    }


    /**
     * Signup with short phone number. User should not exist after signing up
     *
     * @throws Exception - mvc.perform in performSignup() and checkUsername() throws exception
     */
    @Test
    @DisplayName("Signup with short phone number")
    public void signup16() throws Exception {

        final String content = "{" +
                "\"username\" : \"user0\", " +
                "\"email\" : \"email0@di.uoa.gr\", " +
                "\"firstName\" : \"FirstName01\", " +
                "\"lastName\" : \"LastName1\", " +
                "\"password\" : \"myPwd123\", " +
                "\"telNumber\" : \"11880\", " +
                "\"taxNumber\" : \"123345\", " +
                "\"longitude\" : \"23.76695\", " +
                "\"latitude\" : \"37.968564\", " +
                "\"locationTitle\" : \"Dit Uoa\" " +
                "}";

        performSignup(content)
                .andExpect(status().isBadRequest());
    }


    /**
     * Login with email that does not exist
     * We should receive HTTP <code>NOT FOUND</code>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Login with non-existent email")
    public void authorize1() throws Exception {

        final String content = "{" +
                "\"email\" : \"wrongmail@di.uoa.gr\", " +
                "\"password\" : \"myPwd123\" " +
                "}";

        mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
                .andExpect(status().isNotFound());
    }

    /**
     * Login with username that does not exist
     * We should receive HTTP <code>NOT FOUND</code>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Login with non-existent username")
    public void authorize2() throws Exception {

        final String content = "{" +
                "\"username\" : \"wrongUser\", " +
                "\"password\" : \"myPwd123\" " +
                "}";

        mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
                .andExpect(status().isNotFound());
    }

    /**
     * Successful login with username
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Successful login with username")
    public void authorize3() throws Exception {

        final String username = "user1";

        final String content = "{" +
                "\"username\" : \"" + username + "\", " +
                "\"password\" : \"myPwd123\" " +
                "}";

        mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("user.username", is(username)));
    }

    /**
     * Successful login with email
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Successful login with email")
    public void authorize4() throws Exception {

        final String email = "email2@di.uoa.gr";

        final String content = "{" +
                "\"email\" : \"" + email + "\", " +
                "\"password\" : \"myPwd123\" " +
                "}";

        final String token = "Bearer " + ((JSONObject) new JSONParser().parse(
                mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()))
                .get("token").toString();


        mvc.perform(get("/account")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("email", is(email)));

    }

    /**
     * Login with both username and email
     * As credentials may mismatch or belong different users we should
     * get back an HTTP <Code>BAD REQUEST</Code>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Login with both username and email")
    public void authorize5() throws Exception {

        final String username = "user1";
        final String email = "email2@usi.ch";

        final String content = "{" +
                "\"username\" : \"" + username + "\", " +
                "\"email\" : \"" + email + "\", " +
                "\"password\" : \"myPwd123\" " +
                "}";

        mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
                .andExpect(status().isBadRequest());
    }


    /**
     * Login with invalid credential
     * We should get back an HTTP <Code>UNAUTHORIZED</Code>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Login with wrong credentials")
    public void authorize6() throws Exception {

        final String content = "{" +
                "\"username\" : \"user1\", " +
                "\"password\" : \"wrongPassword\" " +
                "}";

        mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(content))
                .andExpect(status().isUnauthorized());
    }

    /**
     * User accesses secure endpoint and the JWTFilter validates the token
     * This service is called at startup of the app to check
     * if the jwt token is still valid
     * We should get back an HTTP <code>NO CONTENT</code>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Authenticate")
    public void authenticate() throws Exception{

        mvc.perform(get("/auth/authenticate").secure(true)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isNoContent());
    }


    /**
     * Get chat token
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Get chat token")
    public void getChatToken() throws Exception{

        mvc.perform(get("/auth/chatkitToken")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("token").exists());
    }
}