package com.example.relicv2.model;

import com.example.relicv2.enums.Condition;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Date;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String shortDescription;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private User vendor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Enumerated(value = EnumType.STRING)
    private Condition condition;

    private Long minimumBid;

    private Long currentBid = this.minimumBid;

    private boolean isBiddingClosed = true;

    private boolean isBlocked = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winning_bidder_id")
    private User winningBidder;

    private boolean winningBidderTrophy = false;

    private String biddingTimeLimit;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Bid> bidList;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<Review> reviews;

    @Column(length = 1024)
    private String productImageUrl;

    @ElementCollection
    private List<String> imageUrls;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = false)
    private Date updatedAt = new Date();

    @Column
    private boolean approachingNotificationSent = false;

    @Column
    private boolean endingNotificationSent = false;

    public boolean isApproachingNotificationSent() {
        return approachingNotificationSent;
    }

    public void setApproachingNotificationSent(boolean approachingNotificationSent) {
        this.approachingNotificationSent = approachingNotificationSent;
    }

    public boolean isEndingNotificationSent() {
        return endingNotificationSent;
    }

    public void setEndingNotificationSent(boolean endingNotificationSent) {
        this.endingNotificationSent = endingNotificationSent;
    }

    @Transient
    public int getNumberOfBids() {
        return this.bidList.size();
    }

    @Transient
    public Double getAverageRating() {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for (Review review : reviews) {
            sum += review.getRating();
        }
        return sum / reviews.size();
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
}
