package com.example.food_ordering.entity;
import com.example.food_ordering.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;


@Entity
@Table(name="users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private  String password;

    @Column(nullable = false)
    private  String fullName;

    @Column(nullable = false)
    private  String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
