package com.Auctions.backEnd.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import javax.xml.bind.annotation.*;
import java.util.Set;
import java.util.TreeSet;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "geolocation")
//@XmlAccessorType(XmlAccessType.FIELD)
//@XmlRootElement(name = "Location")
public class Geolocation extends AuditModel {

//    @XmlAttribute(name = "Longitude")
    @Column(name = "longitude")
    private Double longitude;

//    @XmlAttribute(name = "Latitude")
    @Column(name = "latitude")
    private Double latitude;

//    @XmlElement(name = "Location")
    @Column(name = "location_title")
    private String locationTitle;

    //TODO WARNING THIS WAS THE REASON OF ITEM DUPLICATION
   // @OneToMany(cascade = CascadeType.PERSIST, mappedBy = "location")
    @OneToMany( mappedBy = "location")
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
