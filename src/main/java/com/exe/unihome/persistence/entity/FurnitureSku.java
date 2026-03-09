package com.exe.unihome.persistence.entity;

import com.exe.unihome.persistence.enums.FurnitureStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "furniture_skus",
       indexes = {
           @Index(name = "idx_sku_furniture", columnList = "furniture_id"),
           @Index(name = "idx_sku_status", columnList = "status")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FurnitureSku {

    @Id
    @GeneratedValue
    @Column(name = "sku_id", nullable = false)
    private UUID skuId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "furniture_id", nullable = false)
    private Furniture furniture;

    @Column(name = "sku_code", nullable = false, unique = true, length = 100)
    private String skuCode;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "final_price", precision = 12, scale = 2)
    private BigDecimal finalPrice;

    @Column(nullable = false)
    @Builder.Default
    private Integer stock = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private FurnitureStatus status = FurnitureStatus.AVAILABLE;

    @Column(name = "has_discount", nullable = false)
    @Builder.Default
    private Boolean hasDiscount = false;

    @Column(name = "image_url", length = 1024)
    private String imageUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
