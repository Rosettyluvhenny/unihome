package com.exe.unihome.persistence.entity.review;

import com.exe.unihome.persistence.entity.Furniture;
import com.exe.unihome.persistence.entity.identityAndAuth.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "furniture_reviews",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_review_user_furniture", columnNames = {"furniture_id", "user_id"})
    },
    indexes = {
        @Index(name = "idx_reviews_furniture", columnList = "furniture_id"),
        @Index(name = "idx_reviews_user", columnList = "user_id")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FurnitureReview {

    @Id
    @GeneratedValue
    @Column(name = "review_id", nullable = false)
    private UUID reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "furniture_id", nullable = false)
    private Furniture furniture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Short rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
