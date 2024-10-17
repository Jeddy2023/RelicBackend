package com.example.relicv2.service;

import com.example.relicv2.dto.AdminBidListDto;
import com.example.relicv2.dto.BidDto;
import com.example.relicv2.dto.BidListDto;
import com.example.relicv2.dto.UserBiddingListDto;

import java.text.ParseException;
import java.util.List;

public interface BiddingService {
    String placeBid(Long productId, BidDto bidDto);
    List<BidListDto> getBiddingList(Long productId);
    BidListDto getWinningBid(Long productId);
    String closeBidding(Long productId);
    String reopenBidding(Long productId) throws ParseException;
    String declareWinner(Long productId);
    List<UserBiddingListDto> getUserBids(Long userId, String keyword, String createdAtStart, String createdAtEnd, String sortBy);
    List<AdminBidListDto> getAllBidsForAdmin(String keyword, String createdAtStart, String createdAtEnd, String sortBy);
}
