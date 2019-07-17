package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.repositories.GeolocationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/location")
public class LocationController {

    private final GeolocationRepository geolocationRepository;

    public LocationController(GeolocationRepository geolocationRepository) {
        this.geolocationRepository = geolocationRepository;
    }

    @GetMapping("/search")
    public ResponseEntity getPartialMatchedLocations(@RequestParam String query) {
        return ResponseEntity.ok(geolocationRepository.searchLocations(query.toLowerCase()));
    }
}
