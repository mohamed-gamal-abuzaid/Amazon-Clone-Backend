package com.Amazon.clone.entity;

import com.Amazon.clone.entity.enums.SellerOrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "seller_orders",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_order_store",
                columnNames = {"order_id", "store_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class SellerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private CustomerOrder order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SellerOrderStatus status = SellerOrderStatus.PENDING;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "shipping_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @OneToMany(
            mappedBy = "sellerOrder",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<OrderItem> items = new LinkedHashSet<>();

    @OneToMany(mappedBy = "sellerOrder", cascade = CascadeType.ALL,
            orphanRemoval = true)
    private Set<Shipment> shipments = new LinkedHashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}
