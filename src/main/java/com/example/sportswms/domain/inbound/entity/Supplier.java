package com.example.sportswms.domain.inbound.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="Supplier")
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "supplier_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name="manager_name", nullable = false)
    private String managerName;

    @Column(name="call_num", nullable = false)
    private String callNum;

    @Column(nullable = false)
    private String email;
}
