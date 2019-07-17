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

import static org.junit.Assert.*;


@SpringBootTest(classes = {TestConfig.class, BackEndApplication.class})
@AutoConfigureMockMvc
public class ValidTest {

    @Autowired
    private TestUtils testUtils;



    private Valid valid;

    @BeforeEach
    public void before() {

        this.testUtils.clearDB();
        valid = new Valid(true);
    }

    @Test
    @DisplayName("creation")
    public void creation() {
        Valid valid1 = new Valid();
        valid1.setValid(false);
        assertFalse(valid1.isValid());


    }

    @Test
    @DisplayName("Getter method")
    public void getterMethod() {
        assertTrue(valid.isValid());

    }

}
