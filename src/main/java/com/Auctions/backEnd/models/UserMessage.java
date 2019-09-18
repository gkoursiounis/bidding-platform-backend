package com.Auctions.backEnd.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Setter
@Getter
@NoArgsConstructor
@Table(name = "user_message")
public class UserMessage extends AuditModel {

    @ManyToOne
    private User recipient;

    @ManyToOne
    private User sender;

    @OneToOne
    private Item item;

    String message;

    boolean seen = false;

    public UserMessage(final Date createdAt) {
        super(createdAt);
    }
}