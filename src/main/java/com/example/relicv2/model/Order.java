package com.example.relicv2.model;

import com.example.relicv2.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private User vendor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "order_date", nullable = false)
    private Date createdAt = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt = new Date();

    private Long totalPrice;
    private Double companyPercentage;
    private Double companyCommission;
    private Boolean paymentReceived = false;
    private Boolean vendorPaymentSent = false;
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    private String cancelReason;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }

    public Boolean isPaymentReceived() {
        return paymentReceived;
    }

    public Boolean isVendorPaymentSent() {
        return vendorPaymentSent;
    }
}
