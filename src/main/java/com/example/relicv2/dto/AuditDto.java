package com.example.relicv2.dto;

import lombok.Data;

import java.util.Date;

@Data
public class AuditDto {
    private Long id;
    private String userUserName;
    private String action;
    private String audit;
    private Date timestamp;
}
