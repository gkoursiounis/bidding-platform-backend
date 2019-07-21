package com.Auctions.backEnd.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Set;
import java.util.TreeSet;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "geolocation")
public class Geolocation extends AuditModel {

    private double longitude;

    private double latitude;

    private String locationTitle;

    @OneToMany(mappedBy = "location")
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
