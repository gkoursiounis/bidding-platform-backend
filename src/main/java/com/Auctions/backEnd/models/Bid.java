package com.Auctions.backEnd.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Setter
@Getter
@Table(name = "bid")
@NoArgsConstructor
public class Bid extends AuditModel {

    @ManyToOne
    private Item item;

    @ManyToOne
    private User bidder;

    @Column(name = "offer_amount")
    private Double offer;

    public Bid(final Date createdAt) {
        super(createdAt);
    }
}
