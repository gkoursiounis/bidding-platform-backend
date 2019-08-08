package com.Auctions.backEnd;

import com.Auctions.backEnd.repositories.*;
import lombok.NoArgsConstructor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.ResultActions;

import java.io.FileInputStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@NoArgsConstructor
public class TestUtils {

	@Autowired
	private UserRepository userRepository;

	@Autowired
    private AccountRepository accountRepository;

	@Autowired
	private DBFileRepository dbFileRepository;

	@Autowired
	private GeolocationRepository geolocationRepository;

	@Autowired
	private ItemCategoryRepository itemCategoryRepository;

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private NotificationRepository notificationRepository;

	public void clearDB() {

		userRepository.deleteAll();
		userRepository.flush();

        accountRepository.deleteAll();
        accountRepository.flush();

		dbFileRepository.deleteAll();
		dbFileRepository.flush();

		geolocationRepository.deleteAll();
		geolocationRepository.flush();

		itemCategoryRepository.deleteAll();
		itemCategoryRepository.flush();

		itemRepository.deleteAll();
		itemRepository.flush();

		notificationRepository.deleteAll();
		notificationRepository.flush();
	}

	/**
	 * Utility function that creates an account and a user
	 *
	 * @param mvc - mvc
	 * @param username - username of the new account
	 * @param password - password of the new account
	 * @param firstName - first name of the new account
	 * @param lastName - last name of the new account
	 * @param email	- email
	 * @return user token
	 * @throws Exception - mvc.perform throws exception
	 */
	public static String createAccount(@NonNull final MockMvc mvc,
									   @NonNull final String  username,
									   @NonNull final String  password,
									   @NonNull final String  firstName,
									   @NonNull final String  lastName,
									   @NonNull final String  email) throws Exception {

		final String token = ((JSONObject) new JSONParser().parse(mvc.perform(post("/auth/signup")
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.content(String.format(
				"{" +
				"\"username\" : \"%s\", " +
				"\"password\" : \"%s\", " +
				"\"email\" : \"%s\", " +
				"\"firstName\" : \"%s\", " +
				"\"lastName\" : \"%s\", " +
				"\"telNumber\" : \"1234567890\", " +
				"\"taxNumber\" : \"123345\", " +
				"\"longitude\" : \"23.76695\", " +
				"\"latitude\" : \"37.968564\", " +
				"\"locationTitle\" : \"Dit Uoa\" " +
				"}",
			username, password, email, firstName, lastName)))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString()))
			.get("token").toString();

		return "Bearer " + token;
	}


	/**
	 * Utility function that creates an account and a user
	 *
	 * @param mvc - mvc
	 * @param username - username of the new account
	 * @param password - password of the new account
	 * @return user token
	 * @throws Exception - mvc.perform throws exception
	 */
	public static String login(@NonNull final MockMvc mvc,
							   @NonNull final String  username,
							   @NonNull final String  password) throws Exception {

		final String token = ((JSONObject) new JSONParser().parse(mvc.perform(post("/auth/login")
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.content(String.format(
						"{" +
						"\"username\" : \"%s\", " +
						"\"password\" : \"%s\" " +
						"}",
						username, password)))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString()))
				.get("token").toString();

		return "Bearer " + token;
	}



	/**
	 * Helper function
	 * Given an user token it returns the userId
	 *
	 * @param token - user token
	 * @return - returns the id of the user
	 * @throws Exception - mvc.perform throws Exception
	 */
	public static String getAccountId(@NonNull final MockMvc mvc,
									  @NonNull final String token) throws Exception {

		return ((JSONObject) new JSONParser().parse(
				mvc.perform(get("/account")
						.contentType(MediaType.APPLICATION_JSON)
						.header("Authorization", token))
						.andExpect(status().isOk())
						.andReturn().getResponse().getContentAsString()
		)).get("id").toString();
	}

	/**
	 * Gets the specified user
	 *
	 * @param mvc - mvc
	 * @param token - token of the user who did the request
	 * @param username - username of the user that we want to get
	 * @return ResultActions(response from the server)
	 * @throws Exception - mvc.perform throws exception
	 */
	public static ResultActions getUser(@NonNull final MockMvc mvc,
										@NonNull final String token,
										@NonNull final String username) throws Exception {

		final String url = "/user/" + username;
		return mvc.perform(get(url)
				.header("Authorization", token));
	}

    /**
     * Gets the id of the specified user
     * @param mvc - mvc
     * @param token - token of the user who did the request
     * @param username - username of the user that we want to get
     * @return - the id of the user we want to get
     * @throws Exception - mvc.perform throws Exception
     */
	public static String getUserToString(@NonNull final MockMvc mvc,
										 @NonNull final String token,
										 @NonNull final String username) throws Exception{

        return ((JSONObject) new JSONParser().parse(getUser(mvc, token, username)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()))
                .get("id").toString();
    }


	/**
	 * Gets the id of the specified user
	 * @param mvc - mvc
	 * @param token - token of the user who did the request
	 * @param categoryName - username of the user that we want to get
	 * @return - the id of the user we want to get
	 * @throws Exception - mvc.perform throws Exception
	 */
	public static String createCategory(@NonNull final MockMvc mvc,
										@NonNull final String token,
										@NonNull final String categoryName) throws Exception{

		return ((JSONObject) new JSONParser().parse(
					mvc.perform(post("/admin/newCategory")
							.contentType(MediaType.APPLICATION_JSON)
							.param("name", categoryName)
							.header("Authorization", token))
							.andExpect(status().isOk())
							.andReturn().getResponse().getContentAsString()))
				.get("id").toString();
	}


	/**
	 * Helper function
	 * Given a user token it creates a post of this user
	 * using the media/Bloodhound.jpg
	 * Whitelist and blacklist are empty
	 *
	 * @param token - token of the user
	 * @throws Exception - mvc.perform throws Exception
	 */
	public static String makeItem(@NonNull final MockMvc mvc,
								   @NonNull final String categoryId,
								   @NonNull final String token) throws Exception{
		return ((JSONObject) new JSONParser().parse(
				mvc.perform(
						post("/item")
								.param("name", "item1")
								.param("buyPrice", "10.4")
								.param("firstBid", "5.3")
								.param("categoriesId", categoryId)
								.param("longitude", "23.76695")
								.param("latitude", "37.968564")
								.param("locationTitle", "Dit UoA")
								.param("endsAt", "2025-09-26T01:30:00.000-04:00")
								.param("description", "this is the description")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON))
						.andExpect(status().isOk())
						.andReturn().getResponse().getContentAsString()))
				.get("id").toString();
	}



	public static String makeExpiredItem(@NonNull final MockMvc mvc,
								  @NonNull final String categoryId,
								  @NonNull final String token) throws Exception{
		return ((JSONObject) new JSONParser().parse(
				mvc.perform(
						post("/item")
								.param("name", "item1")
								.param("buyPrice", "10.4")
								.param("firstBid", "5.3")
								.param("categoriesId", categoryId)
								.param("longitude", "23.76695")
								.param("latitude", "37.968564")
								.param("locationTitle", "Dit UoA")
								.param("endsAt", "2018-09-26T01:30:00.000-04:00")
								.param("description", "this is the description")
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON))
						.andExpect(status().isOk())
						.andReturn().getResponse().getContentAsString()))
				.get("id").toString();
	}


	public static String makeDetailedItem(@NonNull final MockMvc mvc,
										  @NonNull final String categoryId,
										  @NonNull final String name,
										  @NonNull final String description,
										  @NonNull final String token) throws Exception{
		return ((JSONObject) new JSONParser().parse(
				mvc.perform(
						post("/item")
								.param("name", name)
								.param("buyPrice", "10.4")
								.param("firstBid", "5.3")
								.param("categoriesId", categoryId)
								.param("longitude", "23.76695")
								.param("latitude", "37.968564")
								.param("locationTitle", "Dit UoA")
								.param("endsAt", "2025-09-26T01:30:00.000-04:00")
								.param("description", description)
								.header("Authorization", token)
								.contentType(MediaType.APPLICATION_JSON))
						.andExpect(status().isOk())
						.andReturn().getResponse().getContentAsString()))
				.get("id").toString();
	}
}