package com.Auctions.backEnd.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

@Entity
@Setter
@Getter
@Table(name = "item")
@NoArgsConstructor
public class Item extends AuditModel {

    @ManyToOne
    private User seller;

    @NotNull
    @Column(name = "item_name")
    private String name;

    @Column(name = "current_price")
    private Double currently;

    @Column(name = "buy_price")
    private Double buyPrice;

    @Column(name = "first_bid")
    private Double firstBid;

    @ManyToMany
    private Set<ItemCategory> categories;

    @OneToMany(mappedBy = "item")
    @OrderBy(value = "createdAt DESC")
    private Set<Bid> bids = new TreeSet<>();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "started_at")
    private Date startedAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ends_at")
    private Date endsAt;

    @Column(name = "auction_completed")
    private boolean auctionCompleted = false;

    @Column(name = "description", length = 250)
    private String description;

    @OneToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "fk_picture")
    @JsonIgnore
    private DBFile media;


//    @ManyToMany
//    @JoinTable(
//            name = "blocked_relation",
//            joinColumns = @JoinColumn(name = "blocked_id"),
//            inverseJoinColumns = @JoinColumn(name = "blockers_id"))
//    @JsonIgnore
//    private Set<User> blockedBy = new TreeSet<>();

    public boolean isAuctionCompleted() { return this.auctionCompleted; }
}


