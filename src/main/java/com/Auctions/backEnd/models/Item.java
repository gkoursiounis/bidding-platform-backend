package com.Auctions.backEnd.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Entity
@Setter
@Getter
@Table(name = "item")
@NoArgsConstructor
public class Item extends AuditModel implements Serializable {

    public static final long serialVersionUID = 69L;

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
    @Column(name = "ends_at")
    private Date endsAt;

    @Column(name = "auction_completed")
    private Boolean auctionCompleted = false;

    @Column(name = "description", length = 250)
    private String description;

    @OneToMany(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "fk_picture")
    @JsonIgnore
    private List<DBFile> media;         //TODO how to delete picture?

    @OneToOne
    private Geolocation location;


//    @ManyToMany
//    @JoinTable(
//            name = "blocked_relation",
//            joinColumns = @JoinColumn(name = "blocked_id"),
//            inverseJoinColumns = @JoinColumn(name = "blockers_id"))
//    @JsonIgnore
//    private Set<User> blockedBy = new TreeSet<>();

    public boolean isAuctionCompleted() { return this.auctionCompleted; }

    public Item(final Date createdAt) {
        super(createdAt);
    }
}


