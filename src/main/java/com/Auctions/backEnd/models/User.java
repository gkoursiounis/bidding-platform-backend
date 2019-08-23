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


    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinColumn(name = "fk_account")
    @JsonIgnore
    private Account account;

//    @ManyToMany
//    @JoinTable(
//            name = "follows",
//            inverseJoinColumns = @JoinColumn(name = "follower_id"),
//            joinColumns = @JoinColumn(name = "following_id")
//    )
//    @JsonIgnore
//    private Set<User> followers = new TreeSet<>();
//
//    @ManyToMany
//    @JoinTable(
//            name = "follows",
//            inverseJoinColumns = @JoinColumn(name = "following_id"),
//            joinColumns = @JoinColumn(name = "follower_id")
//    )
//    @JsonIgnore
//    public Set<User> following = new TreeSet<>();
//
//    @ManyToMany
//    @JoinTable(
//            name = "blocked_relation",
//            joinColumns = @JoinColumn(name = "blocked_id"),
//            inverseJoinColumns = @JoinColumn(name = "blockers_id"))
//    @JsonIgnore
//    private Set<User> blockedBy = new TreeSet<>();

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL)
    @OrderBy(value = "createdAt DESC")
    @JsonIgnore
    private Set<Item> items = new TreeSet<>();


    @OneToMany(mappedBy = "bidder", cascade = CascadeType.REMOVE)
    @OrderBy(value = "createdAt DESC")
    @JsonIgnore
    private Set<Bid> bids = new TreeSet<>();

    @OneToMany(mappedBy = "recipient", cascade = CascadeType.REMOVE)
    @OrderBy(value = "createdAt DESC")
    @JsonIgnore
    private Set<Notification> notifications = new TreeSet<>();

//    @ManyToMany(cascade = CascadeType.DETACH)
//    private Set<HashTag> hashTags = new HashSet<>();


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
    
    @JsonIgnore
    public boolean isAdmin() {
        return this.account.isAdmin();
    }
}