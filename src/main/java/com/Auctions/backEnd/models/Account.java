package com.Auctions.backEnd.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Setter
@Getter
@Table(name = "account")
@NoArgsConstructor
public class Account extends AuditModel {

    // Fields

    @NotNull
   // @Size(min=5, max=15)
    @Column(name = "account_username")
    private String username;

    @NotNull
    @Email
    @Column(name = "account_email")
    private String email;

    @NotNull
    @Size(min=8)
    @Column(name = "account_password")
    private String password;

    @Column(name = "account_verified")
    private boolean verified = false;

    @Column(name = "account_admin")
    private boolean admin = false;

    // Constructors

    public Account(String username, String email, String password, Boolean verified) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.verified = verified;
    }


    // Methods

    public void encodePassword(PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(this.password);
    }
}
