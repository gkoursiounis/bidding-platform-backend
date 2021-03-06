package com.Auctions.backEnd;

import com.Auctions.backEnd.models.*;
import com.Auctions.backEnd.repositories.*;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@SpringBootApplication
@EnableJpaAuditing
public class BackEndApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(BackEndApplication.class, args);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(12);
	}

	@Bean
	public ServletWebServerFactory servletContainer() {
		TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
			@Override
			protected void postProcessContext(Context context) {
				SecurityConstraint securityConstraint = new SecurityConstraint();
				securityConstraint.setUserConstraint("CONFIDENTIAL");
				SecurityCollection collection = new SecurityCollection();
				collection.addPattern("/*");
				securityConstraint.addCollection(collection);
				context.addConstraint(securityConstraint);
			}
		};
		tomcat.addAdditionalTomcatConnectors(redirectConnector());
		return tomcat;
	}


	private Connector redirectConnector() {
		Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
		connector.setScheme("http");
		connector.setPort(8080);
		connector.setSecure(false);
		connector.setRedirectPort(8443);
		return connector;
	}


	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private ItemCategoryRepository itemCategoryRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;


	@Override
	public void run(String... args) throws Exception {
		System.out.println("App is running...");

		ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
		ScheduledExecutorService exec1 = Executors.newSingleThreadScheduledExecutor();

		exec.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {

				if(userRepository.findByAccount_Username("tediadiktyoy") == null) {
					Account admin = new Account();
					admin.setUsername("tediadiktyoy");
					admin.setPassword(passwordEncoder.encode("adminadmin"));
					admin.setEmail("sdi1600077@di.uoa.gr");
					admin.setAdmin(true);
					admin.setVerified(true);

					User user = new User();
					user.setFirstName("TEDiadiktyoy");
					user.setLastName("spring2019");
					user.setTelNumber("1234567890");
					user.setTaxNumber("1234");
					user.setAccount(admin);

					userRepository.save(user);
					accountRepository.save(admin);

					if(itemCategoryRepository.findItemCategoryByName("All categories") == null) {
						ItemCategory root = new ItemCategory();
						root.setName("All categories");
						itemCategoryRepository.save(root);
					}
				}
				System.err.println("Setting up admin account...");
			}
		}, 0, 365, TimeUnit.DAYS);


		/**
		 * Auction auto-closure utility
		 *
		 * Thread that retrieves every 5 seconds the non-completed auctions set
		 * and for every item it checks if the current time is after the
		 * 'endsAt' time meaning that the auction must be completed
		 *
		 */
		exec1.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				System.out.println("Updating...");
				try {
					List<Item> auctions = itemRepository.getAllOpenAuctions();
					auctions.forEach(item -> {

						if (item.getEndsAt().getTime() < System.currentTimeMillis()) {

							System.err.println("Closing auction with id " + item.getId());

							item.setAuctionCompleted(true);
							itemRepository.save(item);

							Notification toSeller = new Notification();
							toSeller.setRecipient(item.getSeller());
							toSeller.setItemId(item.getId());
							toSeller.setMessage("Your auction has been completed! Click here for details");
							notificationRepository.save(toSeller);

							item.getSeller().getNotifications().add(toSeller);
							userRepository.save(item.getSeller());


							if(!item.getBids().isEmpty()) {
								Notification toBuyer = new Notification();
								User highestBidder = Collections.max(item.getBids(), Bid.cmp).getBidder();
								toBuyer.setRecipient(highestBidder);
								toBuyer.setItemId(item.getId());
								toBuyer.setMessage("Congratulations, you won an auction! Click here for details");
								notificationRepository.save(toBuyer);

								highestBidder.getNotifications().add(toBuyer);
								userRepository.save(highestBidder);
							}
						}
					});
				}
				catch (Exception e){
					e.printStackTrace();
				}
			}
		}, 0, 5, TimeUnit.SECONDS);
	}
}
