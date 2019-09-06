package com.Auctions.backEnd.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;
import java.util.TreeSet;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "geolocation")
public class Geolocation extends AuditModel {

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "location_title")
    private String locationTitle;

    //TODO WARNING THIS WAS THE REASON OF ITEM DUPLICATION
   // @OneToMany(cascade = CascadeType.PERSIST, mappedBy = "location")
    @OneToMany( mappedBy = "location")
    @JsonIgnore
    private final Set<Item> items = new TreeSet<>();

    @OneToMany(mappedBy = "address")
    @JsonIgnore
    private final Set<User> users = new TreeSet<>();

    public Geolocation(Double longitude, Double latitude, String locationTitle) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.locationTitle = locationTitle;
    }
}
