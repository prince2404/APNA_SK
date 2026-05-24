package com.ask.entity;

import com.ask.enums.EntityStatus;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a product category (e.g. Medicine, Baby Food).
 */
@Entity
@Table(name = "product_categories")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EntityStatus status = EntityStatus.ACTIVE;
}
