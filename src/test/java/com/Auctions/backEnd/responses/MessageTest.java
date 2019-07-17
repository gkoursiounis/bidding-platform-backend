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
public class MessageTest {

    @Autowired
    private TestUtils testUtils;

    private Message message;

    @BeforeEach
    public void before() {

        this.testUtils.clearDB();
        message = new Message("type", "text");
    }

    @Test
    @DisplayName("Creation")
    public void creation() {
        Message message1 = new Message();
        message1.setText("text");
        message1.setType("type");

        assertEquals("text", message1.getText());
        assertEquals("type", message1.getType());

    }

    @Test
    @DisplayName("Getter method")
    public void getterMethod() {
        assertEquals("text", message.getText());
        assertEquals("type", message.getType());

    }

}
