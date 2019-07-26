package com.Auctions.backEnd.controllers;

import com.Auctions.backEnd.repositories.GeolocationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "https://localhost:3000")
@RequestMapping("/location")
public class LocationController {

    private final GeolocationRepository geolocationRepository;

    public LocationController(GeolocationRepository geolocationRepository) {
        this.geolocationRepository = geolocationRepository;
    }


    /**
     * The system can provide the user with partial matching search in order
     * to help him find a location during his filter search
     * (see com.Auctions.backEnd.controllers.ItemController : filterSearch())
     *
     * @param query - location keyword
     * @return a set of possible locations
     */
    @GetMapping("/search")
    public ResponseEntity getPartialMatchedLocations(@RequestParam String query) {
        return ResponseEntity.ok(geolocationRepository.searchLocations(query.toLowerCase()));
    }
}
