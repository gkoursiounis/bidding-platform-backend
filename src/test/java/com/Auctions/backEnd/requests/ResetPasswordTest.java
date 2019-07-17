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
public class ResetPasswordTest {


    @Autowired
    private TestUtils testUtils;

    private ResetPassword resetPassword;

    @BeforeEach
    public void before() {

        this.testUtils.clearDB();
        resetPassword = new ResetPassword("password","token");

    }

    /**
     * Create another accountRequest with no args
     *
     */
    @Test
    @DisplayName("Create accountRequest with no args")
    public void createAnotherAccountEmail() {
        ResetPassword resetPassword1 = new ResetPassword();
        assertTrue(resetPassword1.toString() != null);
    }

    /**
     * Test getter method of accountRequest
     *
     */
    @Test
    @DisplayName("accountRequest getter method")
    public void getterMethodAccountEmail() {

        assertEquals("password",resetPassword.getNewPassword());
        assertEquals("token",resetPassword.getToken());

    }

    /**
     * Test setter method of accountRequest
     *
     */
    @Test
    @DisplayName("accountRequest getter method")
    public void setterMethodAccountEmail() {
        resetPassword.setNewPassword("new_password");
        resetPassword.setToken("token2");
        assertEquals("new_password", resetPassword.getNewPassword());
        assertEquals("token2", resetPassword.getToken());
    }
}
