package com.example.relicv2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BidListDto {
    private Long id;
    private String bidderUsername;
    private Long bidAmount;
    private Boolean isWinningBid;
    private Date updatedAt;
}
