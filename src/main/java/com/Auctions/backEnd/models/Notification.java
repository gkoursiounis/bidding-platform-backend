package com.Auctions.backEnd.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Setter
@Getter
@NoArgsConstructor
@Table(name = "notification")
public class Notification extends AuditModel {

    @ManyToOne
    private User recipient;

    String message;

    public Notification(final Date createdAt) {
        super(createdAt);
    }
}