package com.Auctions.backEnd.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(
        value = {"createdAt", "updatedAt"},
        allowGetters = true
)

public abstract class AuditModel implements Serializable, Comparable<AuditModel> {

    @Setter
    @Getter
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

    public Date getCreatedAt() {
        return (Date) this.createdAt.clone();
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt =  new Date(createdAt.getTime());
    }

    @Override
    public int compareTo(AuditModel o) {
        return o.getCreatedAt().compareTo(this.getCreatedAt());
    }

    /**
     * Creates the model with the specified date inside
     * @param createdAt the createdAt parameter value
     */
    public AuditModel(final Date createdAt) {
        this.createdAt = (Date)createdAt.clone();
    }

    /**
     * Default constructor
     */
    public AuditModel() {
        this.createdAt = new Date();
    }
}
