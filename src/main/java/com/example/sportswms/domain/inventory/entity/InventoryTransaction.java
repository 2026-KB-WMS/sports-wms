package com.example.sportswms.domain.inventory.entity;

import com.example.sportswms.domain.product.entity.ProductSKU;
import com.example.sportswms.domain.user.entity.User;
import com.example.sportswms.domain.warehouse.entity.Section;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "InventoryTransaction")
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id", nullable = false)
    private ProductSKU productSKU;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryStatus status;

    @Column(name = "before_quantity", nullable = false)
    private int beforeQuantity;

    @Column(name = "after_quantity", nullable = false)
    private int afterQuantity;

    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private InventoryTransaction(Section section, ProductSKU productSKU, TransactionType transactionType,
                                 int quantity, InventoryStatus status, int beforeQuantity, int afterQuantity,
                                 String reason, User user) {
        this.section = section;
        this.productSKU = productSKU;
        this.transactionType = transactionType;
        this.quantity = quantity;
        this.status = status;
        this.beforeQuantity = beforeQuantity;
        this.afterQuantity = afterQuantity;
        this.reason = reason;
        this.user = user;
        this.createdAt = LocalDateTime.now();
    }

    public static InventoryTransaction of(Section section, ProductSKU productSKU, TransactionType transactionType,
                                          int quantity, InventoryStatus status, int beforeQuantity,
                                          int afterQuantity, String reason, User user) {
        return new InventoryTransaction(section, productSKU, transactionType, quantity, status,
                beforeQuantity, afterQuantity, reason, user);
    }
}
