package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.BackEndApplication;
import com.Auctions.backEnd.TestUtils;
import com.Auctions.backEnd.models.Account;
import com.Auctions.backEnd.models.ItemCategory;
import com.Auctions.backEnd.repositories.AccountRepository;
import com.Auctions.backEnd.repositories.ItemCategoryRepository;
import com.Auctions.backEnd.repositories.UserRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static com.Auctions.backEnd.TestUtils.*;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = {com.Auctions.backEnd.configs.TestConfig.class, BackEndApplication.class})
@AutoConfigureMockMvc
public class SearchControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private com.Auctions.backEnd.TestUtils testUtils;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemCategoryRepository itemCategoryRepository;

    private String user1;
    private String user2;
    private String user3;
    private String categoryId;

    @BeforeEach
    private void before() throws Exception {

        mvc = MockMvcBuilders.webAppContextSetup(this.wac)
                .alwaysDo(MockMvcResultHandlers.print())
                .build();

        user1 = createAccount(mvc, "user1", "myPwd123", "FirstName1", "LastName1", "email1@di.uoa.gr");
        user2 = createAccount(mvc, "user2", "myPwd123", "FirstName2", "LastName2", "email2@di.uoa.gr");
        user3 = createAccount(mvc, "user3", "myPwd123", "FirstName3", "LastName3", "email3@di.uoa.gr");

        ItemCategory category = itemCategoryRepository.findItemCategoryByName("All categories");
        categoryId = category.getId().toString();

        verify("user1");
        verify("user2");
        verify("user3");
    }

    @AfterEach
    private void after() { testUtils.clearDB(); }

    private void verify(final String username) {

        Account account = accountRepository.findByUsername(username);
        assertNotNull(account);
        account.setVerified(true);
        accountRepository.save(account);
    }

    /**
     * User gets a list of results using the search bar
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Search bar 1")
    public void searchBar1() throws Exception {

        TestUtils.makeDetailedItem
                (mvc, categoryId, "fancy dress", "this is a nice dress", user1);

        TestUtils.makeDetailedItem
                (mvc, categoryId, "item no2", "fancy item", user1);

        TestUtils.makeDetailedItem
                (mvc, categoryId, "item no3", "hello world!", user1);

        mvc.perform(get("/search/searchBar")
                .contentType(MediaType.APPLICATION_JSON)
                .content("fancy dress")
                .param("lower", "0")
                .param("upper", "10")
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("fancy dress")))
                .andExpect(jsonPath("$[1].name", is("item no2")));
    }


    /**
     * User gets a list of results using the search bar
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Search bar 2")
    public void searchBar2() throws Exception {

        TestUtils.makeDetailedItem
                (mvc, categoryId, "fancy dress", "this is a nice dress", user1);

        TestUtils.makeDetailedItem
                (mvc, categoryId, "item no2", "fancy item", user1);

        TestUtils.makeDetailedItem
                (mvc, categoryId, "item no3", "hello world!", user1);

        mvc.perform(get("/search/searchBar")
                .param("lower", "0")
                .param("upper", "2")
                .param("text", "fancy dress")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("fancy dress")))
                .andExpect(jsonPath("$[1].name", is("item no2")));
    }


    /**
     * User gets a list of results using the search bar
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Search bar 3")
    public void searchBar3() throws Exception {

        TestUtils.makeDetailedItem
                (mvc, categoryId, "fancy dress", "this is a nice dress", user1);

        TestUtils.makeDetailedItem
                (mvc, categoryId, "dreams!", "fancy item", user1);

        TestUtils.makeDetailedItem
                (mvc, categoryId, "item no3", "hello world!", user1);

        mvc.perform(get("/search/searchBar")
                .contentType(MediaType.APPLICATION_JSON)
                .param("lower", "0")
                .param("upper", "20")
                .param("text", "dream")
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(1)));
    }


    /**
     * User gets a list of results using the search bar
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Search bar 4")
    public void searchBar4() throws Exception {

        TestUtils.makeDetailedItem
                (mvc, categoryId, "fancy dress", "this is a nice dress", user1);

        TestUtils.makeDetailedItem
                (mvc, categoryId, "dress item", "fancy item", user1);

        TestUtils.makeDetailedItem
                (mvc, categoryId, "dress no3", "hello world!", user1);

        mvc.perform(get("/search/searchBar")
                .contentType(MediaType.APPLICATION_JSON)
                .param("lower", "0")
                .param("upper", "2")
                .param("text", "fancy dress")
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(2)));
    }


    /**
     * User gets a list of results using the search bar
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Search bar 5")
    public void searchBar5() throws Exception {

        TestUtils.makeDetailedItem
                (mvc, categoryId, "fancy dress", "this is a nice dress", user1);

        TestUtils.makeDetailedItem
                (mvc, categoryId, "dress item", "fancy item", user1);

        mvc.perform(get("/search/searchBar")
                .contentType(MediaType.APPLICATION_JSON)
                .param("text", "nice car")
                .param("lower", "0")
                .param("upper", "2")
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("fancy dress")))
                .andExpect(jsonPath("$[1].name", is("dress item")));
    }


    /**
     * User gets a list of all partially matched results
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Get partial match")
    public void getPartialMatchedSearch1() throws Exception {

        TestUtils.makeDetailedItem
                (mvc, categoryId, "fancy dress", "this is the description", user1);

        TestUtils.makeDetailedItem
                (mvc, categoryId, "item no2 dress!", "this is a nice dresss!11!", user1);

        TestUtils.makeDetailedItem
                (mvc, categoryId, "item dressy", "!dressara!", user1);

        mvc.perform(get("/search/partialMatch")
                .contentType(MediaType.APPLICATION_JSON)
                .param("keyword", "dress")
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(3)));
    }


    /**
     * User gets a list of all partially matched results
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Get partial match 2")
    public void getPartialMatchedSearch2() throws Exception {

        TestUtils.makeDetailedItem
                (mvc, categoryId, "fancy dress", "this is the description", user1);

        TestUtils.makeDetailedItem
                (mvc, categoryId, "item no2dress", "dream", user1);

        TestUtils.makeDetailedItem
                (mvc, categoryId, "drunk sinatra", "club", user1);

        mvc.perform(get("/search/partialMatch")
                .contentType(MediaType.APPLICATION_JSON)
                .param("keyword", "dre")
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(2)));
    }


    /**
     * User gets a list of all partially matched results
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Get partial match 3")
    public void getPartialMatchedSearch3() throws Exception {

        TestUtils.makeDetailedItem
                (mvc, categoryId, "fancy dress", "this is the description", user1);

        TestUtils.makeDetailedItem
                (mvc, categoryId, "item no2dress", "Does this thing work", user1);

        TestUtils.makeDetailedItem
                (mvc, categoryId, "hellod", "D", user1);

        mvc.perform(get("/search/partialMatch")
                .contentType(MediaType.APPLICATION_JSON)
                .param("keyword", "d")
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(3)));
    }


    /**
     * User gets a list of all partially matched results
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Get partial match 4")
    public void getPartialMatchedSearch4() throws Exception {

        TestUtils.makeDetailedItem
                (mvc, categoryId, "fancy dress", "this is the description", user1);

        TestUtils.makeDetailedItem
                (mvc, categoryId, "item no2", "hello", user1);

        TestUtils.makeDetailedItem
                (mvc, categoryId, "item dressy", "!dressara!", user1);

        mvc.perform(get("/search/partialMatch")
                .contentType(MediaType.APPLICATION_JSON)
                .param("keyword", "DReS")
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(2)));
    }


    /**
     * User gets a list of all partially matched results
     *
     * @throws Exception - mvc.perform
     */
    @Test
    @DisplayName("Get partial match 5")
    public void getPartialMatchedSearch5() throws Exception {

        mvc.perform(get("/search/partialMatch")
                .contentType(MediaType.APPLICATION_JSON)
                .param("keyword", "")
                .header("Authorization", user1))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("Filter search 1")
    public void filterSearch1() throws Exception {

        TestUtils.makeDetailedItem
                (mvc, categoryId, "fancy dress", "this is the description", user1);

        TestUtils.makeDetailedItem
                (mvc, categoryId, "item no2", "hello", user1);

        TestUtils.makeDetailedItem
                (mvc, categoryId, "item dressy", "!dressara!", user1);

        mvc.perform(get("/search/filters")
                .contentType(MediaType.APPLICATION_JSON)
                .param("categoriesId", "cars")
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(3)));
    }


    @Test
    @DisplayName("Filter search 2")
    public void filterSearch2() throws Exception {

        TestUtils.makeFullItem(mvc,categoryId,"item1",
                "desc","100.0","10.0", "Dit Uoa", user1);

        TestUtils.makeFullItem(mvc,categoryId,"item1",
                "desc","100.0","15.0", "Dit Uoa", user1);

        TestUtils.makeFullItem(mvc,categoryId,"item1",
                "desc","100.0","5.0", "Dit Uoa", user1);

        mvc.perform(get("/search/filters")
                .contentType(MediaType.APPLICATION_JSON)
                .param("lowerPrice", "8.0")
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(2)));

    }


    @Test
    @DisplayName("Filter search 3")
    public void filterSearch3() throws Exception {

        TestUtils.makeFullItem(mvc,categoryId,"item1",
                "desc","100.0","10.0", "Dit Uoa", user1);

        TestUtils.makeFullItem(mvc,categoryId,"item1",
                "desc","100.0","15.0", "Dit Uoa", user1);

        TestUtils.makeFullItem(mvc,categoryId,"item1",
                "desc","100.0","5.0", "Dit Uoa", user1);

        mvc.perform(get("/search/filters")
                .contentType(MediaType.APPLICATION_JSON)
                .param("lowerPrice", "10.1")
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(1)));

    }


    @Test
    @DisplayName("Filter search 4")
    public void filterSearch4() throws Exception {

        TestUtils.makeFullItem(mvc,categoryId,"item1",
                "desc","100.0","10.0", "Dit Uoa", user1);

        TestUtils.makeFullItem(mvc,categoryId,"item1",
                "desc","100.0","15.0", "Dit Uoa", user1);

        TestUtils.makeFullItem(mvc,categoryId,"item1",
                "desc","100.0","5.0", "Dit Uoa", user1);

        mvc.perform(get("/search/filters")
                .contentType(MediaType.APPLICATION_JSON)
                .param("lowerPrice", "11.0")
                .param("higherPrice", "21.0")
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(1)));

    }


    @Test
    @DisplayName("Filter search 5")
    public void filterSearch5() throws Exception {

        TestUtils.makeFullItem(mvc,categoryId,"item1",
                "desc","100.0","10.0", "Dit Uoa", user1);

        TestUtils.makeFullItem(mvc,categoryId,"item1",
                "desc","100.0","15.0", "Dit Uoa", user1);

        TestUtils.makeFullItem(mvc,categoryId,"item1",
                "desc","100.0","5.0", "Dit Uoa", user1);

        mvc.perform(get("/search/filters")
                .contentType(MediaType.APPLICATION_JSON)
                .param("lowerPrice", "5.0")
                .param("higherPrice", "15.0")
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(3)));
    }


    @Test
    @DisplayName("Filter search 6")
    public void filterSearch6() throws Exception {

        TestUtils.makeFullItem(mvc,categoryId,"item1",
                "desc","100.0","10.0", "Dit Uoa", user1);

        TestUtils.makeFullItem(mvc,categoryId,"item1",
                "desc","100.0","15.0", "Dit Uoa", user1);

        TestUtils.makeFullItem(mvc,categoryId,"item1",
                "desc","100.0","5.0", "Dit Uoa", user1);

        mvc.perform(get("/search/filters")
                .contentType(MediaType.APPLICATION_JSON)
                .param("higherPrice", "20.0")
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(3)));
    }


    @Test
    @DisplayName("Filter search 7")
    public void filterSearch7() throws Exception {

        TestUtils.makeFullItem(mvc,categoryId,"item1",
                "desc","100.0","10.0", "Dit Uoa", user1);

        TestUtils.makeFullItem(mvc,categoryId,"item1",
                "desc","100.0","15.0", "Dit Uoa", user1);

        TestUtils.makeFullItem(mvc,categoryId,"item1",
                "desc","100.0","5.0", "Dit Uoa", user1);

        mvc.perform(get("/search/filters")
                .contentType(MediaType.APPLICATION_JSON)
                .param("higherPrice", "14.0")
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(2)));
    }


    @Test
    @DisplayName("Filter search 8")
    public void filterSearch8() throws Exception {

        TestUtils.makeFullItem(mvc,categoryId,"item1",
                "desc","100.0","10.0", "Dit Uoa", user1);

        TestUtils.makeFullItem(mvc,categoryId,"item1",
                "desc","100.0","15.0", "Dit Uoa", user1);

        TestUtils.makeFullItem(mvc,categoryId,"item1",
                "desc","100.0","5.0", "Dit Uoa", user1);

        mvc.perform(get("/search/filters")
                .contentType(MediaType.APPLICATION_JSON)
                .param("locationTitle", "Dit Uoa")
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(3)));
    }


    @Test
    @DisplayName("Filter search 9")
    public void filterSearch9() throws Exception {

        TestUtils.makeFullItem(mvc,categoryId,"item1",
                "desc","100.0","10.0", "Dit Uoa", user1);

        TestUtils.makeFullItem(mvc,categoryId,"item1",
                "desc","100.0","15.0", "Dit Uoa", user1);

        TestUtils.makeFullItem(mvc,categoryId,"item1",
                "desc","100.0","5.0", "Dit Uoa", user1);

        mvc.perform(get("/search/filters")
                .contentType(MediaType.APPLICATION_JSON)
                .param("lowerPrice", "11.0")
                .param("locationTitle", "Dit Uoa")
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(1)));
    }


    @Test
    @DisplayName("Filter search 10")
    public void filterSearch10() throws Exception {

        TestUtils.makeFullItem(mvc,categoryId,"item1",
                "desc","100.0","10.0", "Dit Uoa", user1);

        TestUtils.makeFullItem(mvc,categoryId,"item1",
                "desc","100.0","15.0", "Dit Uoa", user1);

        TestUtils.makeFullItem(mvc,categoryId,"item1",
                "desc","100.0","5.0", "Dit Uoa", user1);

        mvc.perform(get("/search/filters")
                .contentType(MediaType.APPLICATION_JSON)
                .param("lowerPrice", "9.0")
                .param("higherPrice", "12.0")
                .param("locationTitle", "Dit Uoa")
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(1)));
    }


    @Test
    @DisplayName("Filter search 10")
    public void filterSearch11() throws Exception {

        TestUtils.makeFullItem(mvc,categoryId,"item1",
                "skata","100.0","10.0", "Dit Uoa", user1);

        TestUtils.makeFullItem(mvc,categoryId,"item1",
                "sta moutra mas","100.0","15.0", "Dit Uoa", user1);

        TestUtils.makeFullItem(mvc,categoryId,"item1",
                "skara","100.0","5.0", "Dit Uoa", user1);

        mvc.perform(get("/search/filters")
                .contentType(MediaType.APPLICATION_JSON)
                .param("lowerPrice", "9.0")
                .param("higherPrice", "12.0")
                .param("description", "ska")
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(1)));

        mvc.perform(get("/search/filters")
                .contentType(MediaType.APPLICATION_JSON)
                .param("lowerPrice", "14.0")
                .param("higherPrice", "50.0")
                .param("description", "ska")
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(0)));
    }

    @Test
    public void filterSearch12() throws Exception {

    verify("user1");
        TestUtils.makeFullItem(mvc,categoryId,"item1",
                "skata","100.0","10.0", "Dit Uoa", user1);

        TestUtils.makeFullItem(mvc,categoryId,"item1",
                "sta moutra mas","100.0","15.0", "Dit Uoa", user1);

        TestUtils.makeFullItem(mvc,categoryId,"item1",
                "skara","100.0","5.0", "Dit Uoa", user1);

        mvc.perform(get("/search/test")
                .contentType(MediaType.APPLICATION_JSON)
                .param("higherPrice", "11.0")
                .header("Authorization", user1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("content.*", hasSize(2)));
    }
}