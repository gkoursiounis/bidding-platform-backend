package com.Auctions.backEnd.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Setter
@Getter
@NoArgsConstructor
@Table(name = "user_message")
public class UserMessage extends AuditModel {

    @ManyToOne
    @JsonIgnore
    private User recipient;

    @ManyToOne
    @JsonIgnore
    private User sender;

    String message;

    boolean seen = false;

    public UserMessage(final Date createdAt) {
        super(createdAt);
    }
}