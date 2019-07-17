package com.Auctions.backEnd.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Location {
    private String apiIdentifier;
    private Double longitude;
    private Double latitude;
    private String locationType;
    private String locationTitle;
}
