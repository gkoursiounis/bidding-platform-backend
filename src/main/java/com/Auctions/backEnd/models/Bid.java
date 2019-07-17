package com.Auctions.backEnd.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ManyToAny;

import javax.persistence.*;
import java.util.Set;
import java.util.TreeSet;

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

}
