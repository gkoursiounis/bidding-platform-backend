package com.Auctions.backEnd.responses;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Message {
    private String type;
    private String text;
}
