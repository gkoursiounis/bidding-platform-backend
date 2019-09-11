package com.Auctions.backEnd.configs;

import com.Auctions.backEnd.services.Security.JWTConfigurer;
import com.Auctions.backEnd.services.Security.TokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final TokenProvider tokenProvider;

    public SecurityConfig(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http
                .csrf()
                .disable()
                .cors()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                //.httpBasic() // optional, if you want to access
                //  .and()     // the services from a browser
                .authorizeRequests()
                .antMatchers("/auth/signup").permitAll()
                .antMatchers("/auth/login").permitAll()
                .antMatchers("/file/upload").permitAll()
                .antMatchers("/account/checkUsername").permitAll()
                .antMatchers("/account/checkEmail").permitAll()
                .antMatchers("/media/downloadFile/{fileId}").permitAll()
                .antMatchers("/item/{itemId}/visitor").permitAll()
                .antMatchers("/item/openAuctions").permitAll()
                .antMatchers("/item/allCategories").permitAll()
                .antMatchers("/search/partialMatch").permitAll()
                .antMatchers("/search/searchBar").permitAll()
                .antMatchers("/search/filters").permitAll()
                .antMatchers("/user/{username}").permitAll()
                .antMatchers("/recommend/visitor").permitAll()
                .antMatchers("/recommend/xmlRead").permitAll()
                .anyRequest().authenticated()
                .and()
                .apply(new JWTConfigurer(this.tokenProvider))
                .and()
                .requiresChannel()
                .anyRequest().requiresSecure()
                .and()
                .headers()
                .httpStrictTransportSecurity()
                .includeSubDomains(true)
                .maxAgeInSeconds(31536000);
        // @formatter:on
    }
}