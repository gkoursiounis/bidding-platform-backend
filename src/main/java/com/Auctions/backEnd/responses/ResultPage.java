package com.Auctions.backEnd.responses;

import com.Auctions.backEnd.models.Item;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ResultPage {
    private List<Item> content;
    private int totalElements;
    private int totalPages;
}
