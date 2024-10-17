package com.example.relicv2.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetSuspensionDto {
    private Long id;
    private Date createdAt;
    private boolean suspended;
    private String endDate;
    private String userUsername;
}
