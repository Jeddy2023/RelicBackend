package com.example.relicv2.controller;

import com.example.relicv2.dto.AdminBidListDto;
import com.example.relicv2.dto.BidDto;
import com.example.relicv2.dto.BidListDto;
import com.example.relicv2.dto.UserBiddingListDto;
import com.example.relicv2.service.BiddingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/bid")
@CrossOrigin(origins = "*")
public class BiddingController {
    @Autowired
    private BiddingService biddingService;

    @PostMapping("/place/{productId}")
    public ResponseEntity<String> placeBid(@PathVariable("productId") Long productId, @RequestBody BidDto bidDto) {
        String newbid = biddingService.placeBid(productId, bidDto);
        return new ResponseEntity<>(newbid, HttpStatus.CREATED);
    }

    @GetMapping("/list/{productId}")
    public ResponseEntity<List<BidListDto>> getBiddingList(@PathVariable("productId") Long productId) {
        return ResponseEntity.ok(biddingService.getBiddingList(productId));
    }

    @GetMapping("winning-bid/{productId}")
    public ResponseEntity<BidListDto> getWinningBidding(@PathVariable("productId") Long productId) {
        return new ResponseEntity<>(biddingService.getWinningBid(productId), HttpStatus.OK);
    }

    @PatchMapping("/close/{productId}")
    public ResponseEntity<String> closeBid(@PathVariable("productId") Long productId) {
        String closed = biddingService.closeBidding(productId);
        return new ResponseEntity<>(closed, HttpStatus.OK);
    }

    @PatchMapping("/reopen/{productId}")
    public ResponseEntity<String> reopenBidding(@PathVariable("productId") Long productId) throws ParseException {
        String reopen = biddingService.reopenBidding(productId);
        return new ResponseEntity<>(reopen, HttpStatus.OK);
    }

    @PatchMapping("/declarewinner/{productId}")
    public ResponseEntity<String> declareWinner(@PathVariable("productId") Long productId) {
        String declare = biddingService.declareWinner(productId);
        return new ResponseEntity<>(declare, HttpStatus.OK);
    }

    @GetMapping("/user")
    public ResponseEntity<Page<UserBiddingListDto>> getUserBids(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String createdAtStart,
            @RequestParam(required = false) String createdAtEnd,
            @RequestParam(defaultValue = "Date Descending") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<UserBiddingListDto> userBids = biddingService.getUserBids(userId, keyword, createdAtStart, createdAtEnd, sortBy);

        int start = Math.min(page * size, userBids.size());
        int end = Math.min((page + 1) * size, userBids.size());
        Pageable pageable = PageRequest.of(page, size);
        Page<UserBiddingListDto> bidPage = new PageImpl<>(userBids.subList(start, end), pageable, userBids.size());

        return new ResponseEntity<>(bidPage, HttpStatus.OK);
    }


    @GetMapping("/admin/all-bids")
    public ResponseEntity<Page<AdminBidListDto>> getAllBidsForAdmin(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String createdAtStart,
            @RequestParam(required = false) String createdAtEnd,
            @RequestParam(defaultValue = "Date Descending") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<AdminBidListDto> allBids = biddingService.getAllBidsForAdmin(keyword, createdAtStart, createdAtEnd, sortBy);

        int start = Math.min(page * size, allBids.size());
        int end = Math.min((page + 1) * size, allBids.size());
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminBidListDto> bidPage = new PageImpl<>(allBids.subList(start, end), pageable, allBids.size());

        return new ResponseEntity<>(bidPage, HttpStatus.OK);
    }
}
