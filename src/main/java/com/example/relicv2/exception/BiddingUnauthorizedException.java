package com.example.relicv2.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BiddingUnauthorizedException extends RuntimeException {
    public BiddingUnauthorizedException(String message) {
        super(message);
    }
}
