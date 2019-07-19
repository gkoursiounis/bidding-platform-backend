package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.models.Account;
import com.Auctions.backEnd.models.User;
import com.Auctions.backEnd.repositories.AccountRepository;
import com.Auctions.backEnd.repositories.UserRepository;
import com.Auctions.backEnd.responses.Valid;
import com.Auctions.backEnd.services.security.TokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

import static com.Auctions.backEnd.services.security.JWTFilter.resolveToken;

@RestController
@RequestMapping("/account")
public class AccountController extends BaseController {

    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;

    @Autowired
    public AccountController(PasswordEncoder passwordEncoder, AccountRepository accountRepository,
                             UserRepository userRepository, TokenProvider tokenProvider) {
        this.passwordEncoder = passwordEncoder;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
    }


    /**
     * User can check if a username exists or not
     *
     * @param userName
     * @return false (BAD_REQUEST) if username exists and true (OK) if it does not
     */
    @GetMapping("/checkUsername")
    public ResponseEntity checkUserName(@RequestParam(value="username") String userName) {
        if (accountRepository.findByUsername(userName) != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Valid(false));
        } else {
            return ResponseEntity.ok(new Valid(true));
        }
    }


    /**
     * User can check if an email exists or not
     *
     * @param email
     * @return false (BAD_REQUEST) if email exists and true (OK) if it does not
     */
    @GetMapping("/checkEmail")
    public ResponseEntity checkEmail(@RequestParam(value="email") String email) {
        if (accountRepository.findByEmail(email) != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Valid(false));
        } else {
            return ResponseEntity.ok(new Valid(true));
        }
    }

    @GetMapping
    public ResponseEntity getAccount() {
        Account account = requestUser().getAccount();
        account.setPassword(null);
        return ResponseEntity.ok(account);
    }


//    @PutMapping("/change-password")
//    public ResponseEntity changePassword(@RequestBody AccountRequest accountRequest) {
//        Account account = requestUser().getAccount();
//
//        String oldPasswordFromAccount = account.getPassword();
//        String oldPasswordFromRequest = accountRequest.getOldPassword();
//
//        if(!passwordEncoder.matches(oldPasswordFromRequest, oldPasswordFromAccount)){
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Message(
//                    "Error",
//                    "Your actual password is incorrect"
//            ));
//        }
//
//        account.setPassword(accountRequest.getNewPassword());
//        account.encodePassword(passwordEncoder);
//        accountRepository.save(account);
//
//        return new ResponseEntity(HttpStatus.OK);
//    }
//
//    @PostMapping("/get-reset-password")
//    public ResponseEntity getResetPassword(@RequestBody AccountEmail email) {
//        Account account = accountRepository.findByEmail(email.getEmail());
//        if (account == null) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
//                    "Error",
//                    "account not found"
//            ));
//        }
//
//        ConfirmationToken oldToken = confirmationTokenRepository.findByAccount_Id(account.getId());
//        if (oldToken != null) {
//            confirmationTokenRepository.delete(oldToken);
//        }
//
//        ConfirmationToken confirmationToken = new ConfirmationToken(account);
//        confirmationToken.setAccount(account);
//        confirmationTokenRepository.save(confirmationToken);
//
//        sendResetPasswordEmail(account.getEmail(), confirmationToken.getConfirmationToken());
//
//        return ResponseEntity.ok(new Message(
//                "Ok",
//                "Verify your email"
//        ));
//    }
//
//    @PostMapping("/retry-reset-password")
//    public ResponseEntity retryResetPassword(@RequestBody AccountEmail accountEmail) {
//        Account account = accountRepository.findByEmail(accountEmail.getEmail());
//        if (account == null) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
//                    "Error",
//                    "mail not valid"
//            ));
//        }
//
//        ConfirmationToken token = confirmationTokenRepository.findByAccount_Id(account.getId());
//        if (token == null) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
//                    "Error",
//                    "reset request not present"
//            ));
//        }
//
//        confirmationTokenRepository.delete(token);
//        token = new ConfirmationToken(account);
//        token.setAccount(account);
//        confirmationTokenRepository.save(token);
//
//        sendResetPasswordEmail(account.getEmail(), token.getConfirmationToken());
//
//        return ResponseEntity.ok(new Message(
//                "Ok",
//                "Verify your email"
//        ));
//    }
//
//    @PostMapping("/reset-password")
//    public ResponseEntity resetPassword(@RequestBody ResetPassword resetPassword) {
//        ConfirmationToken token = confirmationTokenRepository.findByConfirmationToken(resetPassword.getToken());
//        if (token == null) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
//                    "Error",
//                    "invalid token"
//            ));
//        }
//
//        Account account = accountRepository.findByUsername(token.getAccount().getUsername());
//        if (account == null){
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
//                    "Error",
//                    "account not found"
//            ));
//        }
//
//        ConfirmationToken confirmationToken = confirmationTokenRepository.findByAccount_Id(account.getId());
//
//        if (confirmationToken == null || !confirmationToken.equals(token)) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
//                    "Error",
//                    "invalid token"
//            ));
//        }
//
//        account.setVerified(true);
//        account.setPassword(resetPassword.getNewPassword());
//        account.encodePassword(this.passwordEncoder);
//        confirmationTokenRepository.delete(token);
//        accountRepository.save(account);
//
//        return ResponseEntity.ok(new Message(
//                "Ok",
//                "Password changed"
//        ));
//    }


    public User requestUser() {

        final HttpServletRequest currentRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String r = resolveToken(currentRequest);
        System.out.println(r);
        Authentication a = tokenProvider.getAuthentication(r);
        System.out.println(a);

        User user = userRepository.findByAccount_Username(a.getName());
        return user;
    }
}
