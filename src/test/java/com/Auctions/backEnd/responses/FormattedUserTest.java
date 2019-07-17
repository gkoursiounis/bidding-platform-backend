package com.Auctions.backEnd.responses;

import com.Auctions.backEnd.BackEndApplication;
import com.Auctions.backEnd.TestUtils;
import com.Auctions.backEnd.configs.TestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.Assert.assertEquals;


@SpringBootTest(classes = {TestConfig.class, BackEndApplication.class})
@AutoConfigureMockMvc
public class FormattedUserTest {

    @Autowired
    private TestUtils testUtils;



    private FormattedUser formattedUser;

//    @BeforeEach
//    public void before() {
//
//        this.testUtils.clearDB();
//        formattedUser = new FormattedUser((long) 1, "lol", "lolo", "william", "/media/lol", "bio", false, false, new Date(System.currentTimeMillis()), 1, 2, false, true);
//
//    }

//    @Test
//    @DisplayName("Creation")
//    public void creation() {
//        User user = new User();
//        user.setAccount(new Account());
//        user.setId((long) 1);
//        user.getAccount().setUsername("lolo");
//        user.setFirstName("Alibaba");
//        user.setLastName("Neuman");
//        user.setImgPath("/bin/user");
//        user.setBio("bio");
//        user.setPrivateProfile(true);
//        user.getAccount().setVerified(false);
//        user.setFollowers(new HashSet<>());
//        user.setFollowed(true);
//        user.setFollowing(new HashSet<>());
//        user.setRequestSent(false);
//
//        FormattedUser formattedUser1 = new FormattedUser(user);
//        assertEquals("bio", formattedUser1.getBio());
//        assertEquals("Alibaba", formattedUser1.getFirstName());
//
//    }

//    @Test
//    @DisplayName("Getter method")
//    public void getterMethod() {
//        assertEquals("bio", formattedUser.getBio());
//        assertEquals("lolo", formattedUser.getFirstName());
//
//    }

//    @Test
//    @DisplayName("Setter method")
//    public void setterMethod() {
//
//        formattedUser.setFirstName("Alibaba");
//        formattedUser.setLastName("Neuman");
//        formattedUser.setImgPath("/bin/user");
//        formattedUser.setBio("bio");
//        formattedUser.setPrivateProfile(true);
//        formattedUser.setFollowers(4);
//        formattedUser.setFollowed(true);
//        formattedUser.setFollowing(1);
//        formattedUser.setRequestSent(false);
//
//        assertEquals("bio", formattedUser.getBio());
//        assertEquals("Alibaba", formattedUser.getFirstName());
//    }


}
