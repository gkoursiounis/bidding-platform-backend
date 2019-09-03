package com.Auctions.backEnd.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.*;
import java.util.Set;
import java.util.TreeSet;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "geolocation")
//@XmlAccessorType(XmlAccessType.FIELD)
//@XmlRootElement(name = "Location")
public class Geolocation extends AuditModel {

//    @XmlAttribute(name = "Longitude")
    private double longitude;

//    @XmlAttribute(name = "Latitude")
    private double latitude;

//    @XmlElement(name = "Location")
    private String locationTitle;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "location")
    @JsonIgnore
//    @XmlTransient
    private final Set<Item> items = new TreeSet<>();

    @OneToMany(mappedBy = "address")
    @JsonIgnore
//    @XmlTransient
    private final Set<User> users = new TreeSet<>();

    public Geolocation(Double longitude, Double latitude, String locationTitle) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.locationTitle = locationTitle;
    }
}
