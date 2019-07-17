package com.Auctions.backEnd.responses;

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


@SpringBootTest(classes = {TestConfig.class, BackEndApplication.class})
@AutoConfigureMockMvc
public class LoginResTest {

    @Autowired
    private TestUtils testUtils;



    private LoginRes loginRes;

    @BeforeEach
    public void before() {

        this.testUtils.clearDB();
        loginRes = new LoginRes("token", new FormattedUser());
    }

    @Test
    @DisplayName("Creation")
    public void creation() {
        FormattedUser formattedUser = new FormattedUser();
        LoginRes loginRes1 = new LoginRes();
        loginRes1.setToken("token2");
        loginRes1.setUser(formattedUser);

        assertEquals(formattedUser, loginRes1.getUser());
        assertEquals("token2", loginRes1.getToken());

    }

    @Test
    @DisplayName("Getter method")
    public void getterMethod() {
        assertEquals("token", loginRes.getToken());

    }

}
