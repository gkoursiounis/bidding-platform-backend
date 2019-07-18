package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.BackEndApplication;
import com.Auctions.backEnd.TestUtils;
import com.Auctions.backEnd.configs.TestConfig;
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

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(classes = {TestConfig.class, BackEndApplication.class})
@AutoConfigureMockMvc
public class AuthControllerTest{

    @Autowired
    private TestUtils testUtils;

	@Autowired
	private MockMvc mvc;

    private String user1;
    private String user2;
    private String user3;

	@BeforeEach
    public void before() throws Exception {

        this.testUtils.clearDB();

        user1 = TestUtils.createAccount(mvc, "user1", "myPwd123", "FirstName1", "LastName1", "email1@usi.ch");
        user2 = TestUtils.createAccount(mvc, "user2", "myPwd123", "FirstName2", "LastName2", "email2@usi.ch");
        user3 = TestUtils.createAccount(mvc, "user3", "myPwd123", "FirstName3", "LastName3", "email3@usi.ch");
    }

    @AfterEach
    public void  after() {
        this.testUtils.clearDB();
    }

    /**
     * Helper function to perform a signup
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
                                "\"email\" : \"email0@usi.ch\", " +
                                "\"firstName\" : \"FirstName1\", " +
                                "\"lastName\" : \"LastName1\", " +
                                "\"telNumber\" : \"1234567890\", " +
                                "\"taxNumber\" : \"123345\" " +
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
                                "\"email\" : \"email1@usi.ch\", " +
                                "\"firstName\" : \"FirstName1\", " +
                                "\"lastName\" : \"LastName1\", " +
                                "\"telNumber\" : \"1234567890\", " +
                                "\"taxNumber\" : \"123345\" " +
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

	    final String email = "email0@usi.ch";

	    final String content = "{" +
                                "\"password\" : \"myPwd123\", " +
                                "\"email\" : \"" + email + "\", " +
                                "\"firstName\" : \"FirstName0\", " +
                                "\"lastName\" : \"LastName1\", " +
                                "\"telNumber\" : \"1234567890\", " +
                                "\"taxNumber\" : \"123345\" " +
                                "}";

        performSignup(content)
                .andExpect(status().isBadRequest());

        mvc.perform(get("/account/checkEmail")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("\"email\" : \"" + email + "\""))
                .andExpect(status().isBadRequest());
    }

    /**
     * Signup without email. User should not exists after signing up
     *
     * @throws Exception - mvc.perform in performSignup() and checkUsername() throws exception
     */
    @Test
    @DisplayName("Signup without email")
    public void signup4() throws Exception {

        final String username = "user0" ;

        final String content = "{" +
                "\"username\" : \"" + username +"\", " +
                "\"password\" : \"myPwd123\", " +
                "\"firstName\" : \"FirstName11\", " +
                "\"lastName\" : \"LastName1\", " +
                "\"telNumber\" : \"1234567890\", " +
                "\"taxNumber\" : \"123345\" " +
                "}";

        performSignup(content)
                .andExpect(status().isBadRequest());

        checkUsername(username)
                .andExpect(status().isBadRequest());
    }

    /**
     * Signup without password. User should not exists after signing up
     *
     * @throws Exception - mvc.perform in performSignup() and checkUsername() throws exception
     */
    @Test
    @DisplayName("Signup without password")
    public void signup5() throws Exception {

        final String username = "user0" ;

        final String content = "{" +
                "\"username\" : \"" + username +"\", " +
                "\"email\" : \"email0@usi.ch\", " +
                "\"firstName\" : \"FirstName01\", " +
                "\"lastName\" : \"LastName1\", " +
                "\"telNumber\" : \"1234567890\", " +
                "\"taxNumber\" : \"123345\" " +
                "}";

        performSignup(content)
                .andExpect(status().isBadRequest());

        checkUsername(username)
                .andExpect(status().isBadRequest());
    }

    /**
     * Signup without first name. User should not exists after signing up
     *
     * @throws Exception - mvc.perform in performSignup() and checkUsername() throws exception
     */
    @Test
    @DisplayName("Signup without first name")
    public void signup6() throws Exception {

        final String username = "user4" ;

        final String content = "{" +
                "\"username\" : \"" + username +"\", " +
                "\"email\" : \"email4@usi.ch\", " +
                "\"password\" : \"myPwd123\", " +
                "\"lastName\" : \"LastName1\", " +
                "\"telNumber\" : \"1234567890\", " +
                "\"taxNumber\" : \"123345\" " +
                "}";

        performSignup(content)
                .andExpect(status().isBadRequest());

        checkUsername(username)
                .andExpect(status().isBadRequest());
    }

    /**
     * Signup without last name. User should not exists after signing up
     *
     * @throws Exception - mvc.perform in performSignup() and checkUsername() throws exception
     */
    @Test
    @DisplayName("Signup without last name")
    public void signup7() throws Exception {

        final String username = "user0" ;

        final String content = "{" +
                "\"username\" : \"" + username + "\", " +
                "\"email\" : \"email0@usi.ch\", " +
                "\"firstName\" : \"FirstName01\", " +
                "\"password\" : \"myPwd123\", " +
                "\"telNumber\" : \"1234567890\", " +
                "\"taxNumber\" : \"123345\" " +
                "}";

        performSignup(content)
                .andExpect(status().isBadRequest());

        checkUsername(username);
    }

    /**
     * Signup using black list words as username (eg. signup, home, user etc)
     *
     * @throws Exception - mvc.perform in performSignup() throws exception
     */
    @Test
    @DisplayName("Signup using blacklisted name")
    public void signup8() throws Exception {

        final String content = "{" +
                                "\"username\" : \"signup\", " +
                                "\"password\" : \"myPwd123\", " +
                                "\"email\" : \"email0@usi.ch\", " +
                                "\"firstName\" : \"FirstName1\", " +
                                "\"lastName\" : \"LastName1\", " +
                                "\"telNumber\" : \"1234567890\", " +
                                "\"taxNumber\" : \"123345\" " +
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
                "\"email\" : \"email4@usi.ch\", " +
                "\"firstName\" : \"FirstName1\", " +
                "\"lastName\" : \"LastName1\", " +
                "\"telNumber\" : \"1234567890\", " +
                "\"taxNumber\" : \"123345\" " +
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
                "\"email\" : \"email4@usi.ch\", " +
                "\"firstName\" : \"FirstName1\", " +
                "\"lastName\" : \"LastName1\", " +
                "\"telNumber\" : \"1234567890\", " +
                "\"taxNumber\" : \"123345\" " +
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
                "\"email\" : \"email4@usi.ch\", " +
                "\"firstName\" : \"" + firstName + "\", " +
                "\"lastName\" : \"" + lastName + "\", " +
                "\"telNumber\" : \"" + telNumber + "\", " +
                "\"taxNumber\" : \"" + taxNumber + "\" " +
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
     * Signup using 'visitor' as username
     *
     * @throws Exception - mvc.perform in performSignup() throws exception
     */
    @Test
    @DisplayName("Signup using visitor as name")
    public void signup12() throws Exception {

        final String content = "{" +
                "\"username\" : \"visitor\", " +
                "\"password\" : \"myPwd123\", " +
                "\"email\" : \"email5@usi.ch\", " +
                "\"firstName\" : \"FirstName1\", " +
                "\"lastName\" : \"LastName1\", " +
                "\"telNumber\" : \"1234567890\", " +
                "\"taxNumber\" : \"123345\" " +
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
                "\"email\" : \"wrongmail@usi.ch\", " +
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

        final String email = "email2@usi.ch";

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
                .header("Authorization", token))
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

        mvc.perform(get("/auth/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("test for chat token")
    public void getChatToken() throws Exception{
        mvc.perform(get("/auth/chatkitToken")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("token").exists());
    }

    @Test
    @DisplayName("visitor login")
    public void loginAsVisitor() throws Exception{
        mvc.perform(post("/auth/visitorLogin")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}