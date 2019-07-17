package com.Auctions.backEnd.requests;

import com.Auctions.backEnd.BackEndApplication;
import com.Auctions.backEnd.TestUtils;
import com.Auctions.backEnd.configs.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SpringBootTest(classes = {TestConfig.class, BackEndApplication.class})
@AutoConfigureMockMvc
public class SignUpTest {


    @Autowired
    private TestUtils testUtils;

    private SignUp signUp;

    @BeforeEach
    public void before() {

        this.testUtils.clearDB();
        signUp = new SignUp();
        signUp.setFirstName("Gorge");
        signUp.setLastName("Lol");
        signUp.setEmail("lol@gmail.com");
        signUp.setPassword("password");
        signUp.setUsername("lolo");

    }

    /**
     * Create another accountRequest with no args
     *
     */
    @Test
    @DisplayName("Create sign up with no args")
    public void createAnotherAccountEmail() {
        SignUp signUp1 = new SignUp();
        signUp1.setLastName("Lol");
        assertEquals("Lol", signUp1.getLastName());
    }

    /**
     * Test getter method of accountRequest
     *
     */
    @Test
    @DisplayName("sign up getter method")
    public void getterMethodAccountEmail() {

        assertEquals("Gorge",signUp.getFirstName());
        assertEquals("Lol",signUp.getLastName());
        assertEquals("lol@gmail.com",signUp.getEmail());
        assertEquals("password",signUp.getPassword());
        assertEquals("lolo",signUp.getUsername());
    }

    /**
     * Test setter method of accountRequest
     *
     */
    @Test
    @DisplayName("accountRequest getter method")
    public void setterMethodAccountEmail() {
        signUp.setFirstName("Gorge1");
        signUp.setLastName("Lol1");
        signUp.setEmail("lol1@gmail.com");
        signUp.setPassword("password1");
        signUp.setUsername("lolo1");

        assertEquals("Gorge1",signUp.getFirstName());
        assertEquals("Lol1",signUp.getLastName());
        assertEquals("lol1@gmail.com",signUp.getEmail());
        assertEquals("password1",signUp.getPassword());
        assertEquals("lolo1",signUp.getUsername());

    }
}
