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
public class AccountRequestTest {

    @Autowired
    private TestUtils testUtils;

	private AccountRequest accountRequest;

	@BeforeEach
    public void before() {

        this.testUtils.clearDB();
        accountRequest = new AccountRequest("password","old_password");

    }

    /**
     * Create another accountRequest with no args
     *
     */
    @Test
    @DisplayName("Create accountRequest with no args")
    public void createAnotherAccountEmail() {
	    AccountRequest accountRequest1 = new AccountRequest();
	    assertTrue(accountRequest1.toString() != null);
    }

    /**
     * Test getter method of accountRequest
     *
     */
    @Test
    @DisplayName("accountRequest getter method")
    public void getterMethodAccountEmail() {

        assertEquals("password",accountRequest.getNewPassword());
        assertEquals("old_password",accountRequest.getOldPassword());
    }

    /**
     * Test setter method of accountRequest
     *
     */
    @Test
    @DisplayName("accountRequest getter method")
    public void setterMethodAccountEmail() {
        accountRequest.setNewPassword("new_password");
        accountRequest.setOldPassword("password");
        assertEquals("new_password", accountRequest.getNewPassword());
        assertEquals("password", accountRequest.getOldPassword());
    }


}