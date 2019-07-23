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
				"\"visitor\" : \"false\" " +
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
	public static ResultActions makePost(@NonNull final MockMvc mvc,
										 @NonNull final String token) throws Exception{

		MockMultipartFile file = new MockMultipartFile(
				"media",
				"Bloodhound.jpg",
				"image/jpeg",
				new FileInputStream("media/Bloodhound.jpg"));

		return mvc.perform(
				multipart("/post")
						.file(file)
						.param("title", "title")
						.param("caption", "caption")
						.header("Authorization", token)
						.contentType(MediaType.MULTIPART_FORM_DATA));

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
	public static ResultActions makePost(@NonNull final MockMvc mvc,
										 @NonNull final String token,
										 @NonNull final String locationTitle) throws Exception{

		MockMultipartFile file = new MockMultipartFile(
				"media",
				"Bloodhound.jpg",
				"image/jpeg",
				new FileInputStream("media/Bloodhound.jpg"));

		return mvc.perform(
				multipart("/post")
						.file(file)
						.param("title", "title")
						.param("caption", "caption")
						.param("apiIdentifier", "0")
						.param("longitude", "0")
						.param("latitude", "0")
						.param("locationType", "dunno")
						.param("locationTitle", locationTitle)
						.header("Authorization", token)
						.contentType(MediaType.MULTIPART_FORM_DATA));
	}


	/**
	 *  Make a post with caption
	 * @param mvc - mvc
	 * @param token	- token of the user doing the request
	 * @param caption - caption of the post
	 * @return - the response of the server
	 * @throws Exception - mvc.perform throws Exception
	 */
	public static ResultActions makePostWithCaption(@NonNull final MockMvc mvc,
										 			@NonNull final String token,
										 			@NonNull final String caption) throws Exception {
		MockMultipartFile file = new MockMultipartFile(
				"media",
				"Bloodhound.jpg",
				"image/jpeg",
				new FileInputStream("media/Bloodhound.jpg"));

		return mvc.perform(
				multipart("/post")
						.file(file)
						.param("title", "title")
						.param("caption", caption)
						.param("apiIdentifier", "0")
						.param("longitude", "0")
						.param("latitude", "0")
						.param("locationType", "dunno")
						.param("locationTitle", "Lugano")
						.header("Authorization", token)
						.contentType(MediaType.MULTIPART_FORM_DATA));
	}

	/**
	 *  Make a post with caption
	 * @param mvc - mvc
	 * @param token	- token of the user doing the request
	 * @param caption - caption of the post
	 * @return - the id of the post in string format
	 * @throws Exception - mvc.perform throws Exception
	 */
	public static String makePostWithCaption2(@NonNull final MockMvc mvc,
											  @NonNull final String token,
											  @NonNull final String caption) throws Exception {


		return ((JSONObject) new JSONParser().parse(
				makePostWithCaption(mvc, token, caption)
						.andExpect(status().isOk())
						.andReturn().getResponse().getContentAsString()
		)).get("id").toString();
	}


	/**
	 * Helper function
	 * Given a user token, a whitelist and a blacklist
	 * it creates a post of this users that addresses to
	 * the user of the whitelist and excludes those of blacklist
	 *
	 * @param token - token of the user
	 * @param whiteList - post's white list
	 * @param blackList - post's black list
	 * @throws Exception - mvc.perform throws Exception
	 */
	public static ResultActions makePost(@NonNull final MockMvc mvc,
										 @NonNull final String token,
										 final String whiteList,
										 final String blackList) throws Exception{

		MockMultipartFile file = new MockMultipartFile(
				"media",
				"Bloodhound.jpg",
				"image/jpeg",
				new FileInputStream("media/Bloodhound.jpg"));

		return mvc.perform(
				multipart("/post")
						.file(file)
						.param("title", "title")
						.param("caption", "caption")
						.param("whiteList", whiteList)
						.param("blackList", blackList)
						.header("Authorization", token)
						.contentType(MediaType.MULTIPART_FORM_DATA));
	}

	/**
	 * Helper function
	 * Given a user token it creates a post of this user
	 * using the media/Bloodhound.jpg
	 *
	 * @param userToken - token of the user
	 * @throws Exception - mvc.perform throws Exception
	 */
	public static String makePost2(@NonNull final MockMvc mvc,
								   @NonNull final String userToken) throws Exception{

		return ((JSONObject) new JSONParser().parse(
				makePost(mvc,userToken)
						.andExpect(status().isOk())
						.andReturn().getResponse().getContentAsString()
		)).get("id").toString();
	}

	/**
	 * Helper function
	 * Given a user token it creates a post of this user
	 * using the media/Bloodhound.jpg
	 *
	 * @param userToken - token of the user
	 * @throws Exception - mvc.perform throws Exception
	 */
	public static String makePost2(@NonNull final MockMvc mvc,
								   @NonNull final String userToken,
								   final String whiteList,
								   final String blackList) throws Exception{

		return ((JSONObject) new JSONParser().parse(
				makePost(mvc,userToken,whiteList,blackList)
						.andExpect(status().isOk())
						.andReturn().getResponse().getContentAsString()
		)).get("id").toString();
	}

    /**
     * Delete a post of the user with the specific postId
     * @param mvc - mvc
     * @param token - token of the user
     * @param postId - id of the post to be deleted
     * @return - the response of the server
     * @throws Exception -mvc. perform throws Exception
     */
    public static ResultActions deletePost(@NonNull final MockMvc mvc,
                                           @NonNull final String token,
                                           @NonNull final String postId) throws Exception{

        return mvc.perform(delete("/post/" + postId)
                .header("Authorization", token));
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
	public static ResultActions makeAdvertisement(@NonNull final MockMvc mvc,
												  @NonNull final String token,
												  @NonNull final String link,
												  @NonNull final String date) throws Exception{

		MockMultipartFile file = new MockMultipartFile(
				"media",
				"Bloodhound.jpg",
				"image/jpeg",
				new FileInputStream("media/Bloodhound.jpg"));

		return mvc.perform(
				multipart("/post")
						.file(file)
						.param("title", "title")
						.param("caption", "caption")
						.param("adLink", link)
						.param("publicationDate", date)
						.header("Authorization", token)
						.contentType(MediaType.MULTIPART_FORM_DATA));

	}


	/**
	 * Helper function
	 * Given a user token, a listName and the followersId
	 * it creates a FriendList of that user
	 * FollowersId more than one should be separated with comma
	 *
	 * @param token - token of the user
	 * @param listName - name of the list to be created
	 * @param followersId - the id of the followers which will be inserted in the list
	 * @return - returns the id of the list
	 * @throws Exception - mvc.perform throws Exception
	 */
	public static String createList(@NonNull final MockMvc mvc,
									@NonNull final String token,
									@NonNull final String listName,
									@NonNull final String followersId) throws Exception{

		return ((JSONObject) new JSONParser().parse(
				mvc.perform(post("/list")
						.contentType(MediaType.APPLICATION_JSON_VALUE)
						.header("Authorization", token)
						.content("{" +
								"\"listName\": \"" + listName + "\"," +
								"\"followersId\": [" + followersId + "]" +
								"}"))
						.andExpect(status().isOk())
						.andReturn().getResponse().getContentAsString()))
				.get("id").toString();
	}


	/**
	 * Returns the list name of the list with the id given as a param
	 * @param mvc - mvc
	 * @param token - token of the owner
	 * @param list_id - the id of the list to get
	 * @return - returns the name of the list
	 * @throws Exception - mvc.perform throws exception
	 */
	public static ResultActions getList(@NonNull final MockMvc mvc,
										@NonNull final String token,
										@NonNull final String list_id) throws Exception{

		return mvc.perform(get("/list/"+ list_id)
				.header("Authorization", token));
	}

	/**
	 * Returns the lists of the user
	 * @param token - the id of the list to get
	 * @return - returns the response of the server containing all the lists
	 * @throws Exception - mvc.perform throws exception
	 */
	public static ResultActions getLists(@NonNull final MockMvc mvc,
										 @NonNull final String token) throws Exception{

		return mvc.perform(get("/list/all")
				.header("Authorization", token));

	}


	/**
	 * Helper function
	 * Update (i.e. replace) the list with a new one
	 * FollowersId more than one should be separated with comma
	 * @param token - token of the user
	 * @param listName - new name of the list
	 * @param listId - id of the list
	 * @param followersId - the id of the followers which will be inserted in the list
	 * @return - returns the id of the list
	 * @throws Exception - mvc.perform throws Exception
	 */
	public static ResultActions updateList(@NonNull final MockMvc mvc,
										   @NonNull final String token,
										   @NonNull final String listId,
										   @NonNull final String listName,
										   @NonNull final String followersId) throws Exception{

		return mvc.perform(put("/list/" + listId)
						.contentType(MediaType.APPLICATION_JSON_VALUE)
						.header("Authorization", token)
						.content("{" +
								"\"listName\": \"" + listName + "\"," +
								"\"followersId\": [" + followersId + "]" +
								"}"));
	}


	/**
	 * Helper function
	 * Put a new person in the list
	 * @param token - token of the user
	 * @param listId - id of the list
	 * @param followerId - the id of the follower which will be inserted in the list
	 * @return - returns the response of the server
	 * @throws Exception - mvc.perform throws Exception
	 */
	public static ResultActions putInList(@NonNull final MockMvc mvc,
										  @NonNull final String token,
										  @NonNull final String listId,
										  @NonNull final String followerId) throws Exception{

		return mvc.perform(put("/list/" + listId + "/" + followerId)
				.contentType(MediaType.APPLICATION_JSON_VALUE)
				.header("Authorization", token));
	}

	/**
	 * Get number of activities, notifications and following requests
	 * @param mvc - mvc
	 * @param token - token of the user
	 * @return - return the response
	 * @throws Exception - mvc.perform throws exception
	 */
	public static ResultActions getActivities(@NonNull final MockMvc mvc,
											  @NonNull final String token) throws Exception{

		return mvc.perform(get("/user/activities")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", token));
	}

	/**
	 * Comment on a post
	 * @param mvc - mvc
	 * @param token - token of the user
	 * @param id - id of the post to comment to
	 * @return - return the response
	 * @throws Exception - mvc.perform throws exception
	 */
	public static ResultActions comment(@NonNull final MockMvc mvc,
										@NonNull final String id,
										@NonNull final String token,
										@NonNull final String comment) throws Exception{
		return mvc.perform(post("/post/" + id + "/comment")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{ \"content\" : \"" + comment + "\"}")
				.header("Authorization", token));
	}

	/**
	 * Comment on a post and return the id
	 * @param mvc - mvc
	 * @param id - id of the post
	 * @param token - token of the user
	 * @param comment - comment object to be commented
	 * @return - return the id
	 * @throws Exception - mvc.perform throws Exception
	 */
	public static String comment2(@NonNull final MockMvc mvc,
                                  @NonNull final String id,
                                  @NonNull final String token,
                                  @NonNull final String comment) throws Exception{
		return ((JSONObject) new JSONParser().parse(
				mvc.perform(post("/post/" +id + "/comment")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{ \"content\" : \"" + comment + "\"}")
						.header("Authorization", token))
						.andExpect(status().isOk())
						.andReturn().getResponse().getContentAsString()
		)).get("id").toString();
	}


	/**
	 * Get all the comments
	 * @param mvc - mvc
	 * @param postId - id of the post
	 * @param token - token of the user
	 * @return return the server response
	 * @throws Exception - mvc throws Exception
	 */
	public static ResultActions getComments(@NonNull final MockMvc mvc,
											@NonNull final String postId,
											@NonNull final String token) throws Exception{
		return mvc.perform(get("/post/"+ postId + "/comments")
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", token));
	}

	/**
	 * Delete a comment on a post
	 * @param mvc - mvc
	 * @param token - token of the user
	 * @param postId - id of the post
	 * @param commentId - id of the comment
	 * @return - return the response
	 * @throws Exception - mvc.perform throws exception
	 */
	public static ResultActions uncomment(@NonNull final MockMvc mvc,
										  @NonNull final String token,
										  @NonNull final String postId,
										  @NonNull final String commentId) throws Exception{
		return mvc.perform(delete("/post/"+ postId +"/comment/" + commentId)
				.header("Authorization", token));
	}

	/**
	 * Follow a tag
	 * @param mvc - mvc
	 * @param token	- token of the user
	 * @param tag - tag to be followed
	 * @return return the response
	 * @throws Exception - mvc.perform throws exception
	 */
	public static ResultActions followTag(@NonNull final MockMvc mvc,
										  @NonNull final String token,
										  @NonNull final String tag) throws Exception{

		return mvc.perform(post("/user/hashtag/" + tag)
				.header("Authorization", token));

	}

    /**
     * Follow a tag
     * @param mvc - mvc
     * @param token	- token of the user
     * @param tag - tag to be followed
     * @return return the tag as a string
     * @throws Exception - mvc.perform throws exception
     */
    public static String followTag2(@NonNull final MockMvc mvc,
                                    @NonNull final String token,
                                    @NonNull final String tag) throws Exception{
        return ((JSONObject) new JSONParser().parse(
                followTag(mvc, token, tag)
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString()
        )).get("tag").toString();
    }


	/**
	 * Unfollow a tag
	 * @param mvc - mvc
	 * @param token	- token of the user
	 * @param tag - tag to be unfollowed
	 * @return return the response
	 * @throws Exception - mvc.perform throws exception
	 */
	public static ResultActions unfollowTag(@NonNull final MockMvc mvc,
											@NonNull final String token,
											@NonNull final String tag) throws Exception{

		return mvc.perform(delete("/user/hashtag/" + tag)
				.header("Authorization", token));
	}

	/**
	 * Unfollow a tag
	 * @param mvc - mvc
	 * @param token	- token of the user
	 * @param tag - tag to be unfollowed
	 * @return return the string which is the hashtag that was unfollowed
	 * @throws Exception - mvc.perform throws exception
	 */
	public static String unfollowTag2(@NonNull final MockMvc mvc,
									  @NonNull final String token,
									  @NonNull final String tag) throws Exception{

		return ((JSONObject) new JSONParser().parse(
				unfollowTag(mvc, token, tag)
						.andExpect(status().isOk())
						.andReturn().getResponse().getContentAsString()
		)).get("tag").toString();
	}

	/**
	 * Get all followed hashtags
	 * @param mvc - mvc
	 * @param token	- token of the user
	 * @return return the response
	 * @throws Exception - mvc.perform throws exception
	 */
	public static ResultActions getTags(@NonNull final MockMvc mvc,
										@NonNull final String token) throws Exception{

		return mvc.perform(get("/user/hashtag")
				.header("Authorization", token));
	}


	/**
	 * Make a repost
	 * @param mvc - mvc
  	 * @param token - token of the user
  	 * @param postId - the id of the post to be reposted
  	 * @param title - the title of the repost
  	 * @param caption - the caption of the repost
  	 * @param locationTitle - title of the location of the post
	 * @throws Exception - mvc.perform throws exception
	 */
	public static ResultActions repost(@NonNull final MockMvc mvc,
									   @NonNull final String token,
									   @NonNull final String postId,
									   @NonNull final String title,
									   @NonNull final String caption,
									   final String locationTitle) throws Exception{


		return mvc.perform(post("/post/" + postId)
				.param("title", title)
				.param("caption", caption)
				.param("apiIdentifier", "0")
				.param("longitude", "0")
				.param("latitude", "0")
				.param("locationType", "dunno")
				.param("locationTitle", locationTitle)
				.header("Authorization", token));
	}

	/**
	 * Make a repost
	 * @param mvc - mvc
	 * @param token - token of the user
	 * @param postId - the id of the post to be reposted
	 * @param title - the title of the repost
	 * @param caption - the caption of the repost
	 * @param locationTitle - title of the location of the post
	 * @return - String : the id of the post
	 * @throws Exception - mvc.perform throws Exception
	 */
	public static String repost2(@NonNull final MockMvc mvc,
								 @NonNull final String token,
								 @NonNull final String postId,
								 @NonNull final String title,
								 @NonNull final String caption,
								 final String locationTitle) throws Exception{



		return ((JSONObject) new JSONParser().parse(
				repost(mvc, token, postId, title, caption, locationTitle)
						.andExpect(status().isCreated())
					.andReturn().getResponse().getContentAsString()
				)).get("id").toString();
	}




	/**
	 * Delete location from post
	 * @param mvc - mvc
	 * @param token	- token of the user
	 * @param postId - id of the post
	 * @return return the response
	 * @throws Exception - mvc.perform throws exception
	 */
	public static ResultActions deleteLocation(@NonNull final MockMvc mvc,
											   @NonNull final String token,
											   @NonNull final String postId) throws Exception{

		return mvc.perform(delete("/post/location/" + postId)
				.header("Authorization", token));
	}

}