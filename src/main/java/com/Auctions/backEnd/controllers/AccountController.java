package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.models.Account;
import com.Auctions.backEnd.repositories.AccountRepository;
import com.Auctions.backEnd.requests.AccountRequest;
import com.Auctions.backEnd.responses.Message;
import com.Auctions.backEnd.responses.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account")
public class AccountController extends BaseController{

    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;

    @Autowired
    public AccountController(PasswordEncoder passwordEncoder, AccountRepository accountRepository) {
        this.passwordEncoder = passwordEncoder;
        this.accountRepository = accountRepository;
    }


    /**
     * Route for checking if a username exists or not
     * Afterwards, we decide to suggest the user to try another username or not
     *
     * If a username exists we get back an: <HTTP>BAD REQUEST</HTTP> with Valid.valid = false
     * If a username does not exist we get back an: <HTTP>OK</HTTP> with Valid.valid = true
     *
     * @param username - the username we wish to check
     * @return a validity field {false,true}
     */
    @GetMapping("/checkUsername")
    public ResponseEntity checkUsername(@RequestParam(value="username") String username) {
        if (accountRepository.findByUsername(username) != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Valid(false));
        } else {
            return ResponseEntity.ok(new Valid(true));
        }
    }


    /**
     * Route for checking if an email exists or not
     * Afterwards, we decide to suggest the user to try another email or not
     *
     * If an email exists we get back an: <HTTP>BAD REQUEST</HTTP> with Valid.valid = false
     * If an email does not exist we get back an: <HTTP>OK</HTTP> with Valid.valid = true
     *
     * @param email - the email we wish to check
     * @return a validity field {false,true}
     */
    @GetMapping("/checkEmail")
    public ResponseEntity checkEmail(@RequestParam(value="email") String email) {
        if (accountRepository.findByEmail(email) != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Valid(false));
        } else {
            return ResponseEntity.ok(new Valid(true));
        }
    }


    /**
     * A user can get the details of his account
     * without displaying the encoded password
     *
     * @return the account details
     */
    @GetMapping
    public ResponseEntity getAccount() {
        Account account = requestUser().getAccount();
        account.setPassword(null);
        return ResponseEntity.ok(account);
    }


    /**
     * A user can change his password
     *
     * @param accountRequest - HTTP request body containing the old and the new password
     * @return an <HTTP>OK</HTTP>
     */
    @PutMapping("/changePassword")
    public ResponseEntity changePassword(@RequestBody AccountRequest accountRequest){

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

        return ResponseEntity.ok(new Message(
                "Ok",
                "Your password has been changed"
        ));
    }
}
