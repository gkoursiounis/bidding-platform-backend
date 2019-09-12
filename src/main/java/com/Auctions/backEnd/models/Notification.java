package com.Auctions.backEnd.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.util.Date;

@Entity
@Setter
@Getter
@NoArgsConstructor
@Table(name = "notification")
public class Notification extends AuditModel {

    @ManyToOne
    @JsonIgnore
    private User recipient;

    String message;

    @Nullable
    Long itemId;

    boolean seen = false;

    public Notification(final Date createdAt) {
        super(createdAt);
    }
}