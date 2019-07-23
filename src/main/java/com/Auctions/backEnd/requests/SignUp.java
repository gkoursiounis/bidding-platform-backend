package com.Auctions.backEnd.requests;

import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Setter
@Getter
public class SignUp {

    @NotNull
    @Size(min=5, max=15)
    private String username;

    @NotNull
    @Email
    private String email;

    @NotNull
    @Size(min=8)
    private String password;

    @NotNull
    private String firstName;

    @NotNull
    private String lastName;

    @NotNull
    @Size(min=10, max=12)
    private String telNumber;

    @NotNull
    private String taxNumber;
}
