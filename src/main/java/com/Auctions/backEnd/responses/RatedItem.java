package com.Auctions.backEnd.responses;

import com.Auctions.backEnd.models.Item;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RatedItem{
    Item item;
    double rating;
}
