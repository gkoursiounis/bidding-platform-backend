package com.Auctions.backEnd.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
@Setter
@Getter
@Table(name = "item_category")
@NoArgsConstructor
public class ItemCategory extends AuditModel {

    @NotNull
    @Column(name = "category_name")
    private String name;

    @ManyToMany
    private List<Item> items;
}
