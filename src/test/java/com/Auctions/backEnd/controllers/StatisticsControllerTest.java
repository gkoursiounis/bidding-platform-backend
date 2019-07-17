package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.BackEndApplication;
import com.Auctions.backEnd.TestUtils;
import com.Auctions.backEnd.configs.TestConfig;
import com.Auctions.backEnd.models.Account;

import com.Auctions.backEnd.repositories.AccountRepository;

import com.Auctions.backEnd.repositories.UserRepository;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.FixMethodOrder;
import org.junit.jupiter.api.*;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {TestConfig.class, BackEndApplication.class})
@AutoConfigureMockMvc
@FixMethodOrder(MethodSorters.DEFAULT)
public class StatisticsControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private TestUtils testUtils;

    @Autowired
    private AccountRepository accountRepository;



    @Autowired
    private UserRepository userRepository;


    private String user1;
    private String user2;
    private String user3;

    @BeforeEach
    private void before() throws Exception {
        testUtils.clearDB();


        user1 = TestUtils.createAccount(mvc, "user1", "myPwd123", "FirstName1", "LastName1", "email1@usi.ch");
        user2 = TestUtils.createAccount(mvc, "user2", "myPwd123", "FirstName2", "LastName2", "email2@usi.ch");
        user3 = TestUtils.createAccount(mvc, "user3", "myPwd123", "FirstName3", "LastName3", "email3@usi.ch");
    }

    @AfterEach
    private void after() {
        testUtils.clearDB();
    }

    private void makeAdmin(final String username){

        Account user = accountRepository.findByUsername(username);
        assertNotNull(user);
        user.setAdmin(true);
        accountRepository.save(user);
    }

    /**
     * Admin checks statistics of 3 public users with no posts
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Admin statistics - users with no posts")
    public void getStatistics1() throws Exception{

        makeAdmin("user1");

        mvc.perform(get("/statistics/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("totalNumberUser", is(3)))
                .andExpect(jsonPath("numberPublicProfile", is(3)))
                .andExpect(jsonPath("totalPost", is(0)));
    }


    /**
     * Admin checks statistics regarding public/private profiles
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Admin statistics - type of profiles")
    public void getStatistics2() throws Exception{

        makeAdmin("user1");

        String user4 = TestUtils.createAccount(mvc, "user4", "myPwd123", "FirstName4", "LastName4", "email4@usi.ch");

        TestUtils.makePrivate(mvc, user4, true);

        mvc.perform(get("/statistics/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("totalNumberUser", is(4)))
                .andExpect(jsonPath("numberPublicProfile", is(3)))
                .andExpect(jsonPath("numberPrivateProfile", is(1)))
        ;
    }


    /**
     * Admin checks statistics regarding registration dates
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Admin statistics - check registration dates")
    public void getStatistics3() throws Exception{

        // Make user admin
        makeAdmin("user1");

        String pattern = "yyyy-MM-dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String today = simpleDateFormat.format(new Date());

        mvc.perform(get("/statistics/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("firstRegistrationDay", is(today)))
                .andExpect(jsonPath("lastRegistrationDay", is(today)));
    }


    /**
     * Admin checks statistics regarding posts
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Admin statistics - check posts")
    public void getStatistics4() throws Exception{

        // Make user admin
        makeAdmin("user1");

        String pattern = "yyyy-MM-dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String today = simpleDateFormat.format(new Date());

        TestUtils.makePost(mvc, user1).andExpect(status().isOk());
        TestUtils.makePost(mvc, user2).andExpect(status().isOk());

        mvc.perform(get("/statistics/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("totalPost", is(2)))
                .andExpect(jsonPath("firstPostDay", is(today)))
                .andExpect(jsonPath("lastPostDay", is(today)));
    }

    /**
     * Admin checks statistics regarding posts per week days
     *
     * @throws Exception - mvc.perform throws exception
     */
//    @Test
//    @DisplayName("Admin statistics - posts per week days")
//    public void getStatistics5() throws Exception{
//
//
//        makeAdmin("user1");
//
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
//
//        // Wednesday
//        Date date1 = simpleDateFormat.parse("2019-05-08");
//
//        Post post1 = new Post(date1);
//        post1.setTitle("title1");
//        post1.setCaption("caption1");
//        postRepository.save(post1);
//
//        // Wednesday
//        Date date2 = simpleDateFormat.parse("2019-05-15");
//
//        Post post2 = new Post(date2);
//        post2.setTitle("title2");
//        post2.setCaption("caption2");
//        postRepository.save(post2);
//
//        // Thursday
//        Date date3 = simpleDateFormat.parse("2019-05-16");
//
//        Post post3 = new Post(date3);
//        post3.setTitle("title3");
//        post3.setCaption("caption3");
//        postRepository.save(post3);
//
//        mvc.perform(get("/statistics/admin")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", user1))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("totalPost", is(3)))
//                .andExpect(jsonPath("postPerWeekDays.4", is(2)))
//                .andExpect(jsonPath("postPerWeekDays.5", is(1)));
//    }


    /**
     * Admin checks statistics regarding total weeks between first & last post
     *
     * @throws Exception - mvc.perform throws exception
     */
//    @Test
//    @DisplayName("Admin statistics - total weeks between first & last post")
//    public void getStatistics6() throws Exception {
//
//        makeAdmin("user1");
//
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
//
//        // Thursday
//        Date date1 = simpleDateFormat.parse("2019-01-01");
//
//        Post post1 = new Post(date1);
//        post1.setTitle("title1");
//        post1.setCaption("caption1");
//        postRepository.save(post1);
//
//        // Wednesday
//        Date date2 = simpleDateFormat.parse("2019-02-01");
//
//        Post post2 = new Post(date2);
//        post2.setTitle("title2");
//        post2.setCaption("caption2");
//        postRepository.save(post2);
//
//        mvc.perform(get("/statistics/admin")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", user1))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("totalPost", is(2)))
//                .andExpect(jsonPath("totalWeeksBetweenFirstLastPost", is(4)));
//    }


    /**
     * User tries to get the admin statistics
     * We should get an HTTP <code>UNAUTHORIZED</code>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Admin statistics - users with no posts")
    public void getStatistics8() throws Exception{

        mvc.perform(get("/statistics/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isUnauthorized());
    }


    /**
     * User successfully tries to get his statistics
     * but he has no followers/followings and posts
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User statistics - users with no posts and no followers/followings")
    public void getUserStatistic1() throws Exception{

        mvc.perform(get("/statistics/user")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("numberFollowing", is(0)))
                .andExpect(jsonPath("numberFollower", is(0)))
                .andExpect(jsonPath("mostLikedPost", empty()))
                .andExpect(jsonPath("mostCommentPost", empty()));
    }


    /**
     * User successfully tries to get his statistics
     * with correct numbers regarding his followings
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User statistics - check followings")
    public void getUserStatistic2() throws Exception{

        TestUtils.follow(mvc, user1, "user2").andExpect(status().isOk());

        TestUtils.follow(mvc, user1, "user3").andExpect(status().isOk());
        TestUtils.makePrivate(mvc, user3, true);

        mvc.perform(get("/statistics/user")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("numberFollowing", is(2)))
                .andExpect(jsonPath("numberPrivateFollowing", is(1)))
                .andExpect(jsonPath("numberPublicFollowing", is(1)))

                .andExpect(jsonPath("numberFollower", is(0)))
                .andExpect(jsonPath("numberPrivateFollower", is(0)))
                .andExpect(jsonPath("numberPublicFollower", is(0)));
    }


    /**
     * User successfully tries to get his statistics
     * with correct numbers regarding his followers
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User statistics - check followers")
    public void getUserStatistic3() throws Exception{

        TestUtils.follow(mvc, user2, "user1").andExpect(status().isOk());

        TestUtils.makePrivate(mvc, user3, true);
        TestUtils.follow(mvc, user3, "user1").andExpect(status().isOk());

        mvc.perform(get("/statistics/user")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("numberFollower", is(2)))
                .andExpect(jsonPath("numberPrivateFollower", is(1)))
                .andExpect(jsonPath("numberPublicFollower", is(1)));
    }


    /**
     * User successfully tries to get his statistics
     * with correct numbers regarding his posts per day
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User statistics - check posts per week")
    public void getUserStatistic4() throws Exception{

        TestUtils.follow(mvc, user2, "user1").andExpect(status().isOk());

        TestUtils.makePrivate(mvc, user3, true);
        TestUtils.follow(mvc, user3, "user1").andExpect(status().isOk());

        mvc.perform(get("/statistics/user")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("numberFollower", is(2)))
                .andExpect(jsonPath("numberPrivateFollower", is(1)))
                .andExpect(jsonPath("numberPublicFollower", is(1)));
    }


    /**
     * User successfully tries to get his statistics
     * with correct numbers regarding his posts
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User statistics - check posts")
    public void getUserStatistic5() throws Exception{

        TestUtils.makePost(mvc, user1)
            .andExpect(status().isOk())
            .andDo( mvcResult ->
                    TestUtils.makePost(mvc, user1)
                            .andExpect(status().isOk())
                            .andDo( mvcResult1 ->

                                    mvc.perform(get("/statistics/user")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .header("Authorization", user1))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("totalPostNumber", is(2)))
                            ));
    }


    /**
     * User successfully tries to get his statistics
     * with correct numbers regarding his followers per day
     *
     * @throws Exception - mvc.perform throws exception
     */
//    @Test
//    @DisplayName("User statistics - check followers per day")
//    public void getUserStatistic6() throws Exception{
//
//        User User1 = userRepository.findByAccount_UserName("user1");
//        User User2 = userRepository.findByAccount_UserName("user2");
//        User User3 = userRepository.findByAccount_UserName("user3");
//
//        long oneDayInMillis = 86400000;
//        String pattern = "yyyy-MM-dd";
//        DateFormat simpleDateFormat = new SimpleDateFormat(pattern);
//
//        //7 days before including today
//        String sevenDaysAgo = simpleDateFormat.format(new Date().getTime() - 6*oneDayInMillis);
//        Follow follow1 = new Follow(User1, User2, simpleDateFormat.parse(sevenDaysAgo));
//        followRepository.save(follow1);
//
//        System.out.println(sevenDaysAgo);
//
//        //6 days before including today
//        String sixDaysAgo = simpleDateFormat.format(new Date().getTime() - 5*oneDayInMillis);
//        Follow follow2 = new Follow(User3, User2, simpleDateFormat.parse(sixDaysAgo));
//        followRepository.save(follow2);
//
//        System.out.println(sixDaysAgo);
//
//        mvc.perform(get("/statistics/user")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", user2))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("numberFollower", is(2)))
//                .andExpect(jsonPath("followersLastSevenDays.1", is(1)))
//                .andExpect(jsonPath("followersLastSevenDays.2", is(1)));
//    }


    /**
     * User successfully tries to get his statistics
     * with correct numbers regarding his followers per day
     *
     * @throws Exception - mvc.perform throws exception
     */
//    @Test
//    @DisplayName("User statistics - check followers per day(1)")
//    public void getUserStatistic7() throws Exception{
//
//        User User1 = userRepository.findByAccount_UserName("user1");
//        User User2 = userRepository.findByAccount_UserName("user2");
//        User User3 = userRepository.findByAccount_UserName("user3");
//
//        long oneDayInMillis = 86400000;
//        String pattern = "yyyy-MM-dd";
//        DateFormat simpleDateFormat = new SimpleDateFormat(pattern);
//
//        //yesterday (2 days ago including today)
//        String yesterday = simpleDateFormat.format(new Date().getTime() - oneDayInMillis);
//        Follow follow1 = new Follow(User1, User2, simpleDateFormat.parse(yesterday));
//        followRepository.save(follow1);
//
//
//        //3 days ago including today
//        String threeDaysAgo = simpleDateFormat.format(new Date().getTime() - 2*oneDayInMillis);
//        Follow follow2 = new Follow(User3, User2, simpleDateFormat.parse(threeDaysAgo));
//        followRepository.save(follow2);
//
//        mvc.perform(get("/statistics/user")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", user2))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("numberFollower", is(2)))
//                .andExpect(jsonPath("followersLastSevenDays.6", is(1)))
//                .andExpect(jsonPath("followersLastSevenDays.5", is(1)));
//    }


    /**
     * User successfully tries to get his statistics
     * with correct numbers regarding his followers per day
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User statistics - check followers per day(2)")
    public void getUserStatistic8() throws Exception{

        TestUtils.follow(mvc, user1, "user2").andExpect(status().isOk());
        TestUtils.follow(mvc, user3, "user2").andExpect(status().isOk());

        mvc.perform(get("/statistics/user")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("numberFollower", is(2)))
                .andExpect(jsonPath("followersLastSevenDays.7", is(2)));
    }


    /**
     * User successfully tries to get his statistics
     * with correct numbers regarding his followers per day
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User statistics - check followers per day(3)")
    public void getUserStatistic9() throws Exception{

        TestUtils.follow(mvc, user1, "user2").andExpect(status().isOk());
        TestUtils.follow(mvc, user3, "user2").andExpect(status().isOk());

        TestUtils.unfollow(mvc, user1, "user2").andExpect(status().isOk());
        TestUtils.unfollow(mvc, user3, "user2").andExpect(status().isOk());

        mvc.perform(get("/statistics/user")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("numberFollower", is(0)))
                .andExpect(jsonPath("followersLastSevenDays.7", is(0)));
    }




    /**
     * User successfully tries to get his statistics
     * with correct numbers regarding his most liked post
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User statistics - check most liked post")
    public void getUserStatistic10() throws Exception{

        String user4 = TestUtils.createAccount(mvc, "user4", "myPwd123", "FirstName4", "LastName4", "email4@usi.ch");

        String post1 = TestUtils.makePost2(mvc, user1);
        String post2 = TestUtils.makePost2(mvc, user1);
        String post3 = TestUtils.makePost2(mvc, user1);
        String post4 = TestUtils.makePost2(mvc, user1);

        TestUtils.like(mvc, post1, user2).andExpect(status().isOk());
        TestUtils.like(mvc, post1, user3).andExpect(status().isOk());
        TestUtils.like(mvc, post1, user4).andExpect(status().isOk());

        TestUtils.like(mvc, post2, user2).andExpect(status().isOk());
        TestUtils.like(mvc, post2, user3).andExpect(status().isOk());

        TestUtils.like(mvc, post3, user2).andExpect(status().isOk());

        mvc.perform(get("/statistics/user")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("totalPostNumber", is(4)))
                .andExpect(jsonPath("mostLikedPost.*", hasSize(3)))

                .andExpect(jsonPath("mostLikedPost[0].nLikes", is(3)))
                .andExpect(jsonPath("mostLikedPost[0].id", is(Integer.valueOf(post1))))

                .andExpect(jsonPath("mostLikedPost[1].nLikes", is(2)))
                .andExpect(jsonPath("mostLikedPost[1].id", is(Integer.valueOf(post2))))

                .andExpect(jsonPath("mostLikedPost[2].nLikes", is(1)))
                .andExpect(jsonPath("mostLikedPost[2].id", is(Integer.valueOf(post3))));
    }


    /**
     * User successfully tries to get his statistics
     * with correct numbers regarding his most liked post
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User statistics - check most liked post(1)")
    public void getUserStatistic11() throws Exception{

        TestUtils.makePost(mvc, user1);
        TestUtils.makePost(mvc, user1);
        TestUtils.makePost(mvc, user1);

        mvc.perform(get("/statistics/user")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("totalPostNumber", is(3)))
                .andExpect(jsonPath("mostLikedPost.*", hasSize(3)))
                .andExpect(jsonPath("mostLikedPost[0].nLikes", is(0)))
                .andExpect(jsonPath("mostLikedPost[1].nLikes", is(0)))
                .andExpect(jsonPath("mostLikedPost[2].nLikes", is(0)));

    }


    /**
     * User successfully tries to get his statistics
     * with correct numbers regarding his most liked post
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User statistics - check most liked post(2)")
    public void getUserStatistic15() throws Exception{

        TestUtils.makePost(mvc, user1)
                .andDo( mvcResult -> {
                    mvc.perform(get("/statistics/user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", user1))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("totalPostNumber", is(1)))
                            .andExpect(jsonPath("mostCommentPost.*", hasSize(1)))
                            .andExpect(jsonPath("mostCommentPost[0].nComments", is(0)))

                            .andExpect(jsonPath("mostLikedPost.*", hasSize(1)))
                            .andExpect(jsonPath("mostLikedPost[0].nLikes", is(0)));
                });
    }


    /**
     * User successfully tries to get his statistics
     * with correct numbers regarding his most liked post
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User statistics - check most liked post(3)")
    public void getUserStatistic16() throws Exception{

        String post_id = TestUtils.makePost2(mvc, user1);
        TestUtils.makePost(mvc, user1);
        TestUtils.makePost(mvc, user1);

        TestUtils.like(mvc, post_id, user2).andExpect(status().isOk());

        mvc.perform(get("/statistics/user")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("totalPostNumber", is(3)))
                .andExpect(jsonPath("mostLikedPost.*", hasSize(3)))
                .andExpect(jsonPath("mostLikedPost[0].id", is(Integer.valueOf(post_id))))
                .andExpect(jsonPath("mostLikedPost[0].nLikes", is(1)))
                .andExpect(jsonPath("mostLikedPost[1].nLikes", is(0)))
                .andExpect(jsonPath("mostLikedPost[2].nLikes", is(0)));
    }


    /**
     * User successfully tries to get his statistics
     * with correct numbers regarding his most commented post
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User statistics - check most commented post")
    public void getUserStatistic12() throws Exception{

        String user4 = TestUtils.createAccount(mvc, "user4", "myPwd123", "FirstName4", "LastName4", "email4@usi.ch");

        String post1 = TestUtils.makePost2(mvc, user1);
        String post2 = TestUtils.makePost2(mvc, user1);
        String post3 = TestUtils.makePost2(mvc, user1);
        String post4 = TestUtils.makePost2(mvc, user1);

        TestUtils.comment(mvc, post1, user2, "comment").andExpect(status().isOk());
        TestUtils.comment(mvc, post1, user3, "comment").andExpect(status().isOk());
        TestUtils.comment(mvc, post1, user4, "comment").andExpect(status().isOk());

        TestUtils.comment(mvc, post2, user3, "comment").andExpect(status().isOk());
        TestUtils.comment(mvc, post2, user4, "comment").andExpect(status().isOk());

        TestUtils.comment(mvc, post3, user3, "comment").andExpect(status().isOk());

        mvc.perform(get("/statistics/user")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("totalPostNumber", is(4)))
                .andExpect(jsonPath("mostCommentPost.*", hasSize(3)))

                .andExpect(jsonPath("mostCommentPost[0].nComments", is(3)))
                .andExpect(jsonPath("mostCommentPost[0].id", is(Integer.valueOf(post1))))

                .andExpect(jsonPath("mostCommentPost[1].nComments", is(2)))
                .andExpect(jsonPath("mostCommentPost[1].id", is(Integer.valueOf(post2))))

                .andExpect(jsonPath("mostCommentPost[2].nComments", is(1)))
                .andExpect(jsonPath("mostCommentPost[2].id", is(Integer.valueOf(post3))));
    }


    /**
     * User successfully tries to get his statistics
     * with correct numbers regarding his most commented post
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User statistics - check most commented post(1)")
    public void getUserStatistic13() throws Exception{

        TestUtils.makePost(mvc, user1);
        TestUtils.makePost(mvc, user1);
        TestUtils.makePost(mvc, user1);

        mvc.perform(get("/statistics/user")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("totalPostNumber", is(3)))
                .andExpect(jsonPath("mostCommentPost.*", hasSize(3)))
                .andExpect(jsonPath("mostCommentPost[0].nComments", is(0)))
                .andExpect(jsonPath("mostCommentPost[1].nComments", is(0)))
                .andExpect(jsonPath("mostCommentPost[2].nComments", is(0)));
    }


    /**
     * User successfully tries to get his statistics
     * with correct numbers regarding his most commented post
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("User statistics - check most commented post(3)")
    public void getUserStatistic18() throws Exception{

        String post_id = TestUtils.makePost2(mvc, user1);
        TestUtils.makePost(mvc, user1);
        TestUtils.makePost(mvc, user1);

        TestUtils.comment(mvc, post_id, user2, "comment").andExpect(status().isOk());

        mvc.perform(get("/statistics/user")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("totalPostNumber", is(3)))
                .andExpect(jsonPath("mostCommentPost.*", hasSize(3)))
                .andExpect(jsonPath("mostCommentPost[0].id", is(Integer.valueOf(post_id))))
                .andExpect(jsonPath("mostCommentPost[0].nComments", is(1)))
                .andExpect(jsonPath("mostCommentPost[1].nComments", is(0)))
                .andExpect(jsonPath("mostCommentPost[2].nComments", is(0)));
    }



    /**
     * Admin checks statistics regarding posts per month
     *
     * @throws Exception - mvc.perform throws exception
     */
//    @Test
//    @DisplayName("Admin statistics - posts per month")
//    public void getStatisticMonth1() throws Exception {
//
//        makeAdmin("user1");
//
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
//
//        Calendar now = Calendar.getInstance();
//        now.add(Calendar.MONTH, -2);
//
//        //2 months ago (not including the current month)
//        Date date = simpleDateFormat.parse(
//                 now.get(Calendar.YEAR) + "-" +
//                        (now.get(Calendar.MONTH) + 1) + "-" +
//                        now.get(Calendar.DATE));
//
//        System.out.println(date);
//        Post post1 = new Post(date);
//        post1.setTitle("title1");
//        post1.setCaption("caption1");
//        postRepository.save(post1);
//
//        //11 months ago (not including the current month)
//        now = Calendar.getInstance();
//        now.add(Calendar.MONTH, -11);
//        date = simpleDateFormat.parse(
//                now.get(Calendar.YEAR) + "-" +
//                        (now.get(Calendar.MONTH) + 1) + "-" +
//                        now.get(Calendar.DATE));
//        System.out.println(date);
//
//        Post post2 = new Post(date);
//        post2.setTitle("title2");
//        post2.setCaption("caption2");
//        postRepository.save(post2);
//
//        //5 months ago (not including the current month)
//        now = Calendar.getInstance();
//        now.add(Calendar.MONTH, -5);
//        date = simpleDateFormat.parse(
//                now.get(Calendar.YEAR) + "-" +
//                        (now.get(Calendar.MONTH) + 1) + "-" +
//                        now.get(Calendar.DATE));
//        System.out.println(date);
//
//        Post post3 = new Post(date);
//        post3.setTitle("title3");
//        post3.setCaption("caption3");
//        postRepository.save(post3);
//
//        Post post4 = new Post(date);
//        post4.setTitle("title4");
//        post4.setCaption("caption4");
//        postRepository.save(post4);
//
//        assertEquals(
//                ((JSONObject) new JSONParser().parse(mvc.perform(get("/statistics/admin/month")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .header("Authorization", user1))
//                        .andExpect(status().isOk())
//                        .andReturn().getResponse().getContentAsString()
//                )).get("Posts").toString()
//                , "[1,0,0,0,0,0,2,0,0,1,0,0]");
//    }


    /**
     * Admin checks statistics regarding registrations per month
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Admin statistics - registrations per month")
    public void getStatisticMonth2() throws Exception {

        makeAdmin("user1");

        assertEquals(
                ((JSONObject) new JSONParser().parse(mvc.perform(get("/statistics/admin/month")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", user1))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString()
                )).get("Registration").toString()
                , "[0,0,0,0,0,0,0,0,0,0,0,3]");
    }


    /**
     * User tries to access statistics regarding registrations per month
     * We should get back an HTTP <code>UNAUTHORIZED</code>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Admin statistics - access denied")
    public void getStatisticMonth3() throws Exception {

        mvc.perform(get("/statistics/admin/month")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isUnauthorized());
    }


    /**
     * Admin tries to access statistics regarding registrations per month
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Admin statistics - access denied")
    public void getStatisticMonth4() throws Exception {

        mvc.perform(get("/statistics/admin/month")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isUnauthorized());
    }



    /**
     * Admin checks statistics regarding posts per week
     *
     * @throws Exception - mvc.perform throws exception
     */
//    @Test
//    @DisplayName("Admin statistics - posts per week")
//    public void getStatisticWeek1() throws Exception {
//
//        makeAdmin("user1");
//
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
//
//        Calendar now = Calendar.getInstance();
//        now.add(Calendar.WEEK_OF_MONTH, -2);
//
//        //2 weeks ago (not including the current week)
//        Date date = simpleDateFormat.parse(
//                now.get(Calendar.YEAR) + "-" +
//                        (now.get(Calendar.MONTH) + 1) + "-" +
//                        now.get(Calendar.DATE));
//
//        Post post1 = new Post(date);
//        post1.setTitle("title1");
//        post1.setCaption("caption1");
//        postRepository.save(post1);
//
//        //11 weeks ago (not including the current week)
//        now = Calendar.getInstance();
//        now.add(Calendar.WEEK_OF_MONTH, -11);
//        date = simpleDateFormat.parse(
//                now.get(Calendar.YEAR) + "-" +
//                        (now.get(Calendar.MONTH) + 1) + "-" +
//                        now.get(Calendar.DATE));
//
//        Post post2 = new Post(date);
//        post2.setTitle("title2");
//        post2.setCaption("caption2");
//        postRepository.save(post2);
//
//        //5 weeks ago (not including the current week)
//        now = Calendar.getInstance();
//        now.add(Calendar.WEEK_OF_MONTH, -5);
//        date = simpleDateFormat.parse(
//                now.get(Calendar.YEAR) + "-" +
//                        (now.get(Calendar.MONTH) + 1) + "-" +
//                        now.get(Calendar.DATE));
//
//        Post post3 = new Post(date);
//        post3.setTitle("title3");
//        post3.setCaption("caption3");
//        postRepository.save(post3);
//
//        Post post4 = new Post(date);
//        post4.setTitle("title4");
//        post4.setCaption("caption4");
//        postRepository.save(post4);
//
//        assertEquals(
//                ((JSONObject) new JSONParser().parse(mvc.perform(get("/statistics/admin/week")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .header("Authorization", user1))
//                        .andExpect(status().isOk())
//                        .andReturn().getResponse().getContentAsString()
//                )).get("Posts").toString()
//                , "[1,0,0,0,0,0,2,0,0,1,0,0]");
//    }


    /**
     * Admin checks statistics regarding registrations per week
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Admin statistics - registrations per week")
    public void getStatisticWeek2() throws Exception {

        makeAdmin("user1");

        assertEquals(
                ((JSONObject) new JSONParser().parse(mvc.perform(get("/statistics/admin/week")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", user1))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString()
                )).get("Registration").toString()
                , "[0,0,0,0,0,0,0,0,0,0,0,3]");
    }


    /**
     * User tries to access statistics regarding registrations per week
     * We should get back an HTTP <code>UNAUTHORIZED</code>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Admin statistics - access denied")
    public void getStatisticWeek3() throws Exception {

        mvc.perform(get("/statistics/admin/week")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isUnauthorized());
    }


    /**
     * Admin checks statistics regarding posts per day
     *
     * @throws Exception - mvc.perform throws exception
     */
//    @Test
//    @DisplayName("Admin statistics - posts per day")
//    public void getStatisticDay1() throws Exception {
//
//        makeAdmin("user1");
//
//        long oneDayInMillis = 86400000;
//        String pattern = "yyyy-MM-dd";
//        DateFormat simpleDateFormat = new SimpleDateFormat(pattern);
//
//        //7 days before including today
//        Date sevenDaysAgo = new Date(new Date().getTime() - 6*oneDayInMillis);
//
//        Post post1 = new Post(sevenDaysAgo);
//        post1.setTitle("title1");
//        post1.setCaption("caption1");
//        postRepository.save(post1);
//
//        Post post2 = new Post(sevenDaysAgo);
//        post2.setTitle("title1");
//        post2.setCaption("caption1");
//        postRepository.save(post2);
//
//        //8 days before including today
//        Date eightDaysAgo = new Date(new Date().getTime() - 7*oneDayInMillis);
//
//        Post post3 = new Post(eightDaysAgo);
//        post3.setTitle("title1");
//        post3.setCaption("caption1");
//        postRepository.save(post3);
//
//        makePost(mvc,user1);
//
//        assertEquals(
//                ((JSONObject) new JSONParser().parse(mvc.perform(get("/statistics/admin/day")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .header("Authorization", user1))
//                        .andExpect(status().isOk())
//                        .andReturn().getResponse().getContentAsString()
//                )).get("Posts").toString()
//                , "[2,0,0,0,0,0,1]");
//    }


    /**
     * Admin checks statistics regarding registrations per day
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Admin statistics - registrations per day")
    public void getStatisticDay2() throws Exception {

        makeAdmin("user1");

        assertEquals(
                ((JSONObject) new JSONParser().parse(mvc.perform(get("/statistics/admin/day")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", user1))
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString()
                )).get("Registration").toString()
                , "[0,0,0,0,0,0,3]");
    }


    /**
     * User tries to access statistics regarding registrations per day
     * We should get back an HTTP <code>UNAUTHORIZED</code>
     *
     * @throws Exception - mvc.perform throws exception
     */
    @Test
    @DisplayName("Admin statistics - access denied")
    public void getStatisticDay3() throws Exception {

        mvc.perform(get("/statistics/admin/day")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isUnauthorized());
    }
}

