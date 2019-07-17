package com.Auctions.backEnd.requests;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccountRequest {
    private String newPassword;
    private String oldPassword;
}
