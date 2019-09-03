package com.Auctions.backEnd.models;

import com.Auctions.backEnd.services.Xml.DoubleXmlAdapter;
import com.fasterxml.jackson.annotation.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.*;

@Entity
@Setter
@Getter
@Table(name = "item")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Item")
@NoArgsConstructor

public class Item extends AuditModel implements Serializable {

    public static final long serialVersionUID = 69L;

    @ManyToOne
    //@XmlElement(name = "Seller")
   @XmlTransient
    private User seller;

    @NotNull
    @Column(name = "item_name", length = 50)
    @XmlElement(name = "Name")
    private String name;

    @Column(name = "current_price")
    @XmlElement(name = "Currently")
    @XmlJavaTypeAdapter(DoubleXmlAdapter.class)
    private Double currently;

    @Column(name = "buy_price")
    @XmlElement(name = "Buy_Price")
    @XmlJavaTypeAdapter(DoubleXmlAdapter.class)
    private Double buyPrice;

    @Column(name = "first_bid")
    @XmlElement(name = "First_Bid")
    @XmlJavaTypeAdapter(DoubleXmlAdapter.class)
    private Double firstBid;

    @ManyToMany
    @XmlTransient
    private List<ItemCategory> categories = new ArrayList<>();

    @OneToMany(mappedBy = "item")
    @OrderBy(value = "offer DESC")
    @JsonIgnoreProperties("item")
//    @XmlElement(name = "Bids")
    private Set<Bid> bids = new TreeSet<>();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "ends_at")
    private Date endsAt;

    @Column(name = "auction_completed")
    private Boolean auctionCompleted = false;

    @Column(name = "description")
    @XmlElement(name = "Description")
    private String description;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "fk_picture")
    @JsonIgnore
    private List<DBFile> media = new ArrayList<>();         //TODO how to delete picture?

    @OneToOne
    @XmlElement(name = "Location")
    private Geolocation location;

    private int sellerRating;

    private int bidderRating;


    public Item(final Date createdAt) { super(createdAt); }

    public boolean isAuctionCompleted() { return this.auctionCompleted; }
}


