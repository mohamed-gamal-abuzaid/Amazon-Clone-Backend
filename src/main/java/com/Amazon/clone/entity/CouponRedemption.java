package com.Amazon.clone.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "coupon_redemptions",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_coupon_order",
                columnNames = {"coupon_id", "order_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class CouponRedemption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private CustomerOrder order;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "redeemed_at", nullable = false, updatable = false)
    private OffsetDateTime redeemedAt;

    @PrePersist
    protected void onCreate() {
        redeemedAt = OffsetDateTime.now();
    }
}
