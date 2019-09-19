package com.Auctions.backEnd.models;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.*;


@Entity
@Setter
@Getter
@NoArgsConstructor
@Table(name = "user")
public class User extends AuditModel {


    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "fk_account")
    @JsonIgnore
    private Account account;

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL)
    @OrderBy(value = "createdAt DESC")
    @JsonIgnore
    private Set<Item> items = new TreeSet<>();

    @OneToMany
    @JoinColumn(name = "items_user_seen")
    @JsonIgnore
    private List<Item> itemSeen = new ArrayList<>();


    @OneToMany(mappedBy = "bidder", cascade = CascadeType.REMOVE)
    @OrderBy(value = "createdAt DESC")
    @JsonIgnore
    private List<Bid> bids = new ArrayList<>();

    @OneToMany(mappedBy = "recipient", cascade = CascadeType.REMOVE)
    @OrderBy(value = "createdAt DESC")
    @JsonIgnore
    private Set<Notification> notifications = new TreeSet<>();

    @OneToMany(mappedBy = "sender", cascade = CascadeType.REMOVE)
    @JsonIgnore
    private Set<UserMessage> messagesSent = new TreeSet<>();

    @OneToMany(mappedBy = "recipient", cascade = CascadeType.REMOVE)
    @JsonIgnore
    private Set<UserMessage> messagesReceived = new TreeSet<>();

    @NotNull
    @Column(name = "firstName")
    @JsonIgnore
    private String firstName;

    @NotNull
    @Column(name = "lastName")
    @JsonIgnore
    private String lastName;

    @NotNull
    @Column(name = "telNumber")
    @JsonIgnore
    @Size(min=10, max=12)
    private String telNumber;

    @NotNull
    @JsonIgnore
    @Column(name = "taxNumber")
    private String taxNumber;

    @OneToOne
    private Geolocation address;

    private int sellerRating;

    private int bidderRating;


    @JsonIgnore
    public String getUsername() {
        return this.account.getUsername();
    }

    @JsonGetter("username")
    public String username() {
        return this.account.getUsername();
    }

    @JsonGetter("verified")
    public boolean isVerified() {
        return this.account.isVerified() || this.account.isAdmin();
    }

    @JsonGetter("admin")
    public boolean isAdmin() {
        return this.account.isAdmin();
    }
}