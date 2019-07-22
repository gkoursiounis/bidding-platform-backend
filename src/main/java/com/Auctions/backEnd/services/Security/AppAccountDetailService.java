package com.Auctions.backEnd.services.Security;

import com.Auctions.backEnd.models.Account;
import com.Auctions.backEnd.repositories.AccountRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class AppAccountDetailService implements UserDetailsService {

    private final AccountRepository accountRepository;

    public AppAccountDetailService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public final UserDetails loadUserByUsername(String accountName)
            throws UsernameNotFoundException {
        final Account user = accountRepository.findByUsername(accountName);
        if (user == null) {
            throw new UsernameNotFoundException("User '" + accountName + "' not found");
        }

        return org.springframework.security.core.userdetails.User.withUsername(accountName)
                .password(user.getPassword()).authorities(Collections.emptyList())
                .accountExpired(false).accountLocked(false).credentialsExpired(false)
                .disabled(false).build();
    }

}