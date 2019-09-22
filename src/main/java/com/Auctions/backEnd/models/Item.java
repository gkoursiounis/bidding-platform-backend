package com.Auctions.backEnd.models;

import com.fasterxml.jackson.annotation.*;
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
    @JoinTable(name = "itemCategory_item",
            joinColumns = @JoinColumn(name = "item_id"),
            inverseJoinColumns = @JoinColumn(name = "item_category_id"))
    private List<ItemCategory> categories = new ArrayList<>();

    @OneToMany( mappedBy = "item", fetch = FetchType.EAGER)
    @OrderBy(value = "offer DESC")
    @JsonIgnoreProperties("item")
    private List<Bid> bids = new ArrayList<>();

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
    private List<DBFile> media = new ArrayList<>();

    @OneToOne
    private Geolocation location;

    private Integer sellerRating;

    private Integer bidderRating;


    @JsonGetter("getMediaPath")
    public List<String> getMediaPath() {
        if(media != null) {
            List<String> paths = new ArrayList<>();
            this.media.forEach(picture -> {
                paths.add(picture.getDownloadLink());
            });
            return paths;
        }
        return null;
    }

    public Item(final Date createdAt) { super(createdAt); }

    public boolean isAuctionCompleted() { return this.auctionCompleted; }
}


