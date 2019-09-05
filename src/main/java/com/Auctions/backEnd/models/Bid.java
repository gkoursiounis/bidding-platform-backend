package com.Auctions.backEnd.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Comparator;
import java.util.Date;

@Entity
@Setter
@Getter
@Table(name = "bid")
@NoArgsConstructor
//@XmlAccessorType(XmlAccessType.FIELD)
//@XmlRootElement(name = "Bid")
public class Bid extends AuditModel {

    @ManyToOne(cascade=CascadeType.PERSIST)
    @JsonIgnoreProperties("bids")
    private Item item;

    @ManyToOne
   // @XmlElement(name = "Bidder")
    private User bidder;

    @Column(name = "offer_amount")
//    @XmlAttribute(name = "Amount")
//    @XmlJavaTypeAdapter(DoubleXmlAdapter.class)
    private Double offer;

    public static final Comparator<Bid> cmp = Comparator.comparingDouble(Bid::getOffer);

    public Bid(final Date createdAt) {
        super(createdAt);
    }
}
