package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.models.Account;
import com.Auctions.backEnd.models.ConfirmationToken;
import com.Auctions.backEnd.repositories.AccountRepository;
import com.Auctions.backEnd.repositories.ConfirmationTokenRepository;
import com.Auctions.backEnd.requests.AccountEmail;
import com.Auctions.backEnd.requests.AccountRequest;
import com.Auctions.backEnd.requests.ResetPassword;
import com.Auctions.backEnd.responses.Message;
import com.Auctions.backEnd.responses.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account")
public class AccountController extends BaseController {

    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;

    @Autowired
    public AccountController(ConfirmationTokenRepository confirmationTokenRepository,
                             PasswordEncoder passwordEncoder,
                             AccountRepository accountRepository) {
        this.confirmationTokenRepository = confirmationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountRepository = accountRepository;
    }


    /**
     * User can check if a username exists or not
     *
     * @param userName
     * @return false if username exists and true if it does not
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
     * @return false if email exists and true if it does not
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


//    @GetMapping("/confirm")
//    public ResponseEntity confirmUserAccount(@RequestParam("token") String confirmationToken) {
//        ConfirmationToken token = confirmationTokenRepository.findByConfirmationToken(confirmationToken);
//        if (token == null) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
//                    "Error",
//                    "token not valid"
//            ));
//        }
//
//        Account account = accountRepository.findByUsername(token.getAccount().getUsername());
//        if (account == null){
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Message(
//                    "Error",
//                    "Account not found"
//            ));
//        }
//
//        if (account.isVerified()) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Message(
//                    "Error",
//                    "Account already isVerified"
//            ));
//        }
//
//        account.setVerified(true);
//        confirmationTokenRepository.delete(token);
//        accountRepository.save(account);
//        return ResponseEntity.ok(new Message(
//                "Ok",
//                "Account isVerified"
//        ));
//    }
//
    @PutMapping("/change-password")
    public ResponseEntity changePassword(@RequestBody AccountRequest accountRequest) {
        Account account = requestUser().getAccount();

        String oldPasswordFromAccount = account.getPassword();
        String oldPasswordFromRequest = accountRequest.getOldPassword();

        if(!passwordEncoder.matches(oldPasswordFromRequest, oldPasswordFromAccount)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new Message(
                    "Error",
                    "Your actual password is incorrect"
            ));
        }

        account.setPassword(accountRequest.getNewPassword());
        account.encodePassword(passwordEncoder);
        accountRepository.save(account);

        return new ResponseEntity(HttpStatus.OK);
    }
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
}
