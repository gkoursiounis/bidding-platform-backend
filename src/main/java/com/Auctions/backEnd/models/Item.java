package com.Auctions.backEnd.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;

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
    @Column(name = "item_name", length = 50)
    private String name;

    @Column(name = "current_price")
    private Double currently;

    @Column(name = "buy_price")
    private Double buyPrice;

    @Column(name = "first_bid")
    private Double firstBid;

    @ManyToMany
//    @JoinTable(
//            name = "item_category",
//            joinColumns = @JoinColumn(name = "item_id"),
//            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<ItemCategory> categories = new HashSet<>();

    @OneToMany(mappedBy = "item")
    @OrderBy(value = "offer DESC")
    @JsonBackReference
    private Set<Bid> bids = new TreeSet<>();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ends_at")
    private Date endsAt;

    @Column(name = "auction_completed")
    private Boolean auctionCompleted = false;

    @Column(name = "description")
    private String description;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "fk_picture")
    @JsonIgnore
    private List<DBFile> media = new ArrayList<>();         //TODO how to delete picture?

    @OneToOne
    private Geolocation location;

    private int sellerRating;

    private int bidderRating;


//    @ManyToMany
//    @JoinTable(
//            name = "blocked_relation",
//            joinColumns = @JoinColumn(name = "blocked_id"),
//            inverseJoinColumns = @JoinColumn(name = "blockers_id"))
//    @JsonIgnore
//    private Set<User> blockedBy = new TreeSet<>();

    public boolean isAuctionCompleted() { return this.auctionCompleted; }

   // @JsonGetter("categories")
  //  public Set<ItemCategory> getCategories() { return this.categories; }

    public Item(final Date createdAt) { super(createdAt); }
}


