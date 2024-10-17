package com.example.relicv2.repository;

import com.example.relicv2.model.Bid;
import com.example.relicv2.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BiddingRepository extends JpaRepository<Bid, Long> {
    List<Bid> findBidsByProductIdOrderByBidAmountDesc(Long productId);
    Bid findByProductIdAndIsWinningBidTrue(Long productId);
    Optional<Bid> findByProductIdAndBidderId(Long productId, Long bidderId);
    List<Bid> findByProductIdAndIsWinningBidFalseAndBidderNot(Long productId, User winner);
    @Query("SELECT DISTINCT b.bidder FROM Bid b WHERE b.product.id = :productId")
    List<User> findDistinctBiddersByProductId(Long productId);
    List<Bid> findByBidderId(Long userId);
}
