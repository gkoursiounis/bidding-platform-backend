package com.Auctions.backEnd.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Comparator;
import java.util.Date;

@Entity
@Setter
@Getter
@Table(name = "bid")
@NoArgsConstructor
public class Bid extends AuditModel {

    @ManyToOne(cascade = CascadeType.ALL)
    @JsonIgnoreProperties("bids")
    private Item item;

    @ManyToOne(cascade = CascadeType.PERSIST)
    private User bidder;

    @Column(name = "offer_amount")
    private Double offer;

    public static final Comparator<Bid> cmp = Comparator.comparingDouble(Bid::getOffer);

    public Bid(final Date createdAt) {
        super(createdAt);
    }
}
