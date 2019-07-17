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
import static org.junit.Assert.*;

@SpringBootTest(classes = {TestConfig.class, BackEndApplication.class})
@AutoConfigureMockMvc
public class AccountEmailTest {

    @Autowired
    private TestUtils testUtils;

	private AccountEmail accountEmail;

	@BeforeEach
    public void before() {

        this.testUtils.clearDB();
        accountEmail = new AccountEmail("folliga@usi.ch");

    }


    /**
     * Create another accountEmailTest
     *
     */
    @Test
    @DisplayName("Create AccountEmailTest")
    public void createAnotherAccountEmail() {
	    AccountEmail accountEmail1 = new AccountEmail();
	    assertTrue(accountEmail1.toString() != null);
    }

    /**
     * Test getter method of account email
     *
     */
    @Test
    @DisplayName("AccountEmailTest getter method")
    public void getterMethodAccountEmail() {
        assertEquals("folliga@usi.ch", accountEmail.getEmail());
    }

    /**
     * Test setter method of account email\
     *
     */
    @Test
    @DisplayName("AccountEmailTest getter method")
    public void setterMethodAccountEmail() {
        accountEmail.setEmail("folliga@hotmail.com");
        assertEquals("folliga@hotmail.com", accountEmail.getEmail());
    }


}