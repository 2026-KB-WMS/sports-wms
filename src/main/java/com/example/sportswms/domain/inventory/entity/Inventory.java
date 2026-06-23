package com.example.sportswms.domain.inventory.entity;

import com.example.sportswms.domain.product.entity.ProductSKU;
import com.example.sportswms.domain.warehouse.entity.Section;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.example.sportswms.global.util.MessageUtils.getMessage;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Inventory")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id", nullable = false)
    private ProductSKU productSKU;

    @Column(name = "actual_quantity")
    private int actualQuantity;

    @Column(name = "allocated_quantity")
    private int allocatedQuantity;

    @Enumerated(EnumType.STRING)
    private InventoryStatus status;

    private Inventory(Section section, ProductSKU productSKU, int quantity) {
        this.section = section;
        this.productSKU = productSKU;
        this.actualQuantity = quantity;
        this.allocatedQuantity = 0;
        this.status = InventoryStatus.NORMAL;
    }

    public static Inventory create(Section section, ProductSKU productSKU, int quantity) {
        return new Inventory(section, productSKU, quantity);
    }

    public void addQuantity(int quantity) {
        this.actualQuantity += quantity;
    }

    // 가용 재고 = 실재고 - 이미 다른 출고에 할당된 수량
    public int getAvailableQuantity() {
        return this.actualQuantity - this.allocatedQuantity;
    }

    // 창고관리자가 출고 구역(피킹 위치)을 배정할 때 호출. 가용 재고가 부족하면 예외 발생
    public void allocate(int quantity) {
        if (getAvailableQuantity() < quantity) {
            throw new IllegalArgumentException(
                    getMessage("outbound.inventory.insufficient", getAvailableQuantity(), quantity));
        }
        this.allocatedQuantity += quantity;
    }

    // 출고 구역 배정 취소/변경 시 할당량을 되돌린다.
    // allocatedQuantity < quantity이면 데이터 불일치 버그이므로 예외 처리
    public void deallocate(int quantity) {
        if (this.allocatedQuantity < quantity) {
            throw new IllegalStateException(
                    getMessage("inventory.deallocate.underflow", this.allocatedQuantity, quantity));
        }
        this.allocatedQuantity -= quantity;
    }

    // 피킹 완료 시 실제로 구역에서 물건이 빠져나감. actualQuantity와 allocatedQuantity를 함께 차감
    public void pick(int quantity) {
        if (this.actualQuantity < quantity || this.allocatedQuantity < quantity) {
            throw new IllegalStateException(
                    getMessage("inventory.pick.underflow", quantity, this.actualQuantity, this.allocatedQuantity));
        }
        this.actualQuantity -= quantity;
        this.allocatedQuantity -= quantity;
    }
}
