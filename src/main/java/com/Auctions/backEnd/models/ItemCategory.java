package com.Auctions.backEnd.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Entity
@Setter
@Getter
@Table(name = "item_category")
@NoArgsConstructor
public class ItemCategory extends AuditModel {

    @NotNull
    @Column(name = "category_name")
    private String name;

    @ManyToMany(mappedBy = "categories")
    @JsonIgnore
    private Set<Item> items = new HashSet<>();
}
