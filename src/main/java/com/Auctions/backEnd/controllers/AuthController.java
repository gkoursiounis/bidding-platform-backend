package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.models.Account;
import com.Auctions.backEnd.models.User;
import com.Auctions.backEnd.repositories.AccountRepository;
import com.Auctions.backEnd.repositories.UserRepository;
import com.Auctions.backEnd.requests.SignUp;
import com.Auctions.backEnd.responses.FormattedUser;
import com.Auctions.backEnd.responses.LoginRes;
import com.Auctions.backEnd.responses.Message;
import com.Auctions.backEnd.services.security.TokenProvider;
import io.jsonwebtoken.*;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.crypto.spec.SecretKeySpec;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/auth")
public class AuthController extends BaseController {

    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Value("${app.chatKit.instanceId}")
    private String instanceId;

    @Value("${app.chatKit.keyId}")
    private String keyId;

    @Value("${app.chatKit.secret}")
    private String secret;

    @Autowired
    public AuthController(PasswordEncoder passwordEncoder,
                          TokenProvider tokenProvider,
                          AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          AccountRepository accountRepository,
                          RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }

    @GetMapping("/authenticate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void authenticate() {
        // we don't have to do anything here
        // this is just a secure endpoint and the JWTFilter
        // validates the token
        // this service is called at startup of the app to check
        // if the jwt token is still valid
    }

    @PostMapping("/login")
    public ResponseEntity authorize(@RequestBody Account account) {
        Account requestAccount;
        try {
            if (account.getUsername() != null && account.getEmail()!= null){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                        "Error",
                        "Bad request"
                ));
            }
            if (account.getEmail() != null) {
                requestAccount = accountRepository.findByEmail(account.getEmail());
                if (requestAccount != null){
                    account.setUsername(requestAccount.getUsername());
                }

            } else {
                requestAccount = accountRepository.findByUsername(account.getUsername());
            }

            if (requestAccount == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
                        "Error",
                        "Account not found"
                ));
            }

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            account.getUsername(),
                            account.getPassword()
                    );

            this.authenticationManager.authenticate(authenticationToken);
            String token = this.tokenProvider.createToken(account.getUsername());
            User user = userRepository.findByAccount_Username(account.getUsername());
            return ResponseEntity.ok(new LoginRes(token, new FormattedUser(user)));

        } catch (NullPointerException | AuthenticationException e) {
            System.err.println("Bad credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Message(
                    "Error",
                    "Bad credentials"
            ));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity signup(@RequestBody SignUp signupAccount) throws NoSuchProviderException, NoSuchAlgorithmException {

        if (signupAccount.getUsername() == null || !checkUsername(signupAccount.getUsername()) ||
                signupAccount.getUsername().length() < 5 || signupAccount.getUsername().length() > 15) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "Invalid username"
            ));
        }

        if (signupAccount.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "Invalid password"
            ));
        }

        if (signupAccount.getFirstName() == null || signupAccount.getLastName() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "Invalid full name"
            ));
        }

        if (signupAccount.getTelNumber() == null || signupAccount.getTaxNumber() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "Telephone number or tax number missing"
            ));
        }

        if ((accountRepository.findByUsername(signupAccount.getUsername()) != null) ||
                accountRepository.findByEmail(signupAccount.getEmail()) != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
                    "Error",
                    "The account already exists"
            ));
        }


        String pwd = this.passwordEncoder.encode(signupAccount.getPassword());

        Account account = new Account();
        account.setUsername(signupAccount.getUsername());
        account.setPassword(pwd);
        account.setEmail(signupAccount.getEmail());
        account.setVisitor(signupAccount.isVisitor());

        //TODO temporary
        account.setVerified(true);

        account = accountRepository.save(account);

        User user = new User();
        user.setFirstName(signupAccount.getFirstName());
        user.setLastName(signupAccount.getLastName());
        user.setTelNumber(signupAccount.getTelNumber());
        user.setTaxNumber(signupAccount.getTaxNumber());
        user.setAccount(account);

        userRepository.save(user);

        String token = this.tokenProvider.createToken(account.getUsername());
        return ResponseEntity.ok(new LoginRes(
                token,
                new FormattedUser(user)
                )
        );
    }

    @GetMapping(value = "/chatkitToken", produces = "application/json")
    public ResponseEntity generateChatkitToken() {
        User requester = requestUser();

        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.MINUTE, 1);
        Map<String, Object> header = new HashMap<>();
        header.put("typ", "JWT");
        header.put("alg", "HS256");
        // Creation of a JWT token for Chatkit auth
        String JWTtoken = Jwts.builder()
                .setHeader(header)
                .setIssuer("api_keys/"+keyId)
                .setSubject(requester.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(c.getTime())
                .claim("instance", instanceId)
                .claim("su", Boolean.TRUE)
                .signWith(SignatureAlgorithm.HS256, new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"))
                .compact();
        return ResponseEntity.ok("{ \"token\": \""+JWTtoken+"\"}");
    }

    private boolean checkUsername(String userName) {
        Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        for (RequestMappingInfo mapping : requestMappingHandlerMapping.getHandlerMethods().keySet()) {
            for(String prc : mapping.getPatternsCondition().getPatterns()) {
                if(prc.contains(userName)) {
                    return false;
                }
            }
        }
        return !p.matcher(userName).find();
    }
}