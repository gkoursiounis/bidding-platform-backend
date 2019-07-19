package com.Auctions.backEnd.requests;

import com.Auctions.backEnd.models.User;
import com.Auctions.backEnd.repositories.UserRepository;
import com.Auctions.backEnd.services.security.TokenProvider;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

import static com.Auctions.backEnd.services.security.JWTFilter.resolveToken;

@NoArgsConstructor
public class RequestUser {

    @Autowired
    private TokenProvider tokenProvider;
    private UserRepository userRepository;

    @Autowired
    public RequestUser(TokenProvider tokenProvider, UserRepository userRepository){
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    public User requestUser() {
//        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        org.springframework.security.core.userdetails.User account;
//        account = (org.springframework.security.core.userdetails.User) principal;
//
//        User user = userRepository.findByAccount_Username(account.getUsername());
//        // user.setFollowed(true);
//        return user;

        final HttpServletRequest currentRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String ProjectContext = currentRequest.getContextPath();

//        Enumeration headerNames = currentRequest.getHeaderNames();
//        while (headerNames.hasMoreElements()) {
//            String key = (String) headerNames.nextElement();
//            String value = currentRequest.getHeader(key);
//            if(value.contains("Bearer")) {
//                System.out.println(value);
//
//            }
//        }

        String r = resolveToken(currentRequest);
        System.out.println(r);
        Authentication a = tokenProvider.getAuthentication(r);
        System.out.println(a);

        User user = userRepository.findByAccount_Username(a.getName());
        return user;
    }
}
