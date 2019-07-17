package com.Auctions.backEnd.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ActivityRes {
    private int notifications;
    private int activities;
    private int requests;
}
