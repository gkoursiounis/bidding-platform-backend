package com.Auctions.backEnd;

import com.Auctions.backEnd.models.Account;
import com.Auctions.backEnd.models.User;
import com.Auctions.backEnd.repositories.AccountRepository;
import com.Auctions.backEnd.repositories.UserRepository;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@SpringBootApplication
@EnableJpaAuditing
public class BackEndApplication implements CommandLineRunner {

	/* Used to print list of routes
	@Autowired
	private RequestMappingHandlerMapping requestMappingHandlerMapping;
	*/

	public static void main(String[] args) {
		SpringApplication.run(BackEndApplication.class, args);

//		SpringApplication sa = new SpringApplication(BackEndApplication.class);
//		sa.setBannerMode(Banner.Mode.OFF);
//		sa.setLogStartupInfo(false);
//
//		ApplicationContext c = sa.run(args);
//		MyObject bean = c.getBean(MyObject.class);
//		bean.doSomething();
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
	private PasswordEncoder passwordEncoder;


	@Override
	public void run(String... args) throws Exception {
		System.out.println("App is running...");

		ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
		exec.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				//System.err.println("Creating admin account...");

				userRepository.deleteAll();
				accountRepository.deleteAll();
				Account admin = new Account();
				admin.setUsername("tediadiktyoy");
				admin.setPassword(passwordEncoder.encode("adminadmin"));
				admin.setEmail("sdi1600077@di.uoa.gr");
				admin.setAdmin(true);
				admin.setVerified(true);
//
//			accountRepository.save(admin);

				User user = new User();
				user.setFirstName("TEDiadiktyoy");
				user.setLastName("spring2019");
				user.setTelNumber("1234567890");
				user.setTaxNumber("1234");
				user.setAccount(admin);

				userRepository.save(user);
				accountRepository.save(admin);

				System.err.println("Creating admin account...");

			}
		}, 0, 1, TimeUnit.DAYS);


//		exec.scheduleAtFixedRate(new Runnable() {
//
//			@Override
//			public void run() {
//				//System.err.println("Creating admin account...");
//				System.err.println("hello...");
//
//			}
//		}, 0, 2, TimeUnit.SECONDS);

	}
}
