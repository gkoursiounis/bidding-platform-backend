package com.Auctions.backEnd.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ConfirmationToken extends AuditModel {

    // ---------
    // Relations
    // ---------
    @OneToOne
    @JoinColumn(name = "fk_account")
    private Account account;

    // ------
    // Fields
    // ------
    @Column(name="confirmation_token")
    private String confirmationToken;

    // ------------
    // Constructors
    // ------------
    public ConfirmationToken(Account account) {
        this.account = account;
        confirmationToken = UUID.randomUUID().toString();
    }
}