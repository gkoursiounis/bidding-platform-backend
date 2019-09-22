package com.Auctions.backEnd.repositories;

import com.Auctions.backEnd.models.Geolocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GeolocationRepository extends JpaRepository<Geolocation, Long> {

    @Query("select g from Geolocation g where (locate(:query, lower(g.locationTitle)) <> 0)")
    List<Geolocation> searchLocations(@Param("query") String query);

    Geolocation findLocationByLatitudeAndLongitude(double Latitude, double Longtitude);
}
