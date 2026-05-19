package com.example.food_ordering.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name="foods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Food {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @Column lets you set constraints - nullable, unique, length
    @Column(nullable = false)
    private String name;

    // BigDecimal is always preferred over double for money
    // double has floating-point errors: 0.1 + 0.2 = 0.30000000000000004
    @Column(nullable = false)
    private BigDecimal basePrice;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;

    // nullable = true is the default, but being explicit is good practice
    @Column(columnDefinition = "TEXT")
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
