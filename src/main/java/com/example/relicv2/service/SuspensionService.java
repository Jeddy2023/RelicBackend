package com.example.relicv2.service;

import com.example.relicv2.dto.GetSuspensionDto;

import java.util.List;

public interface SuspensionService {
    List<GetSuspensionDto> getSuspensionsByUser(Long userId);
    List<GetSuspensionDto> getAllSuspensions(
            String startDate,
            String endDate,
            Boolean suspended,
            String sortBy
    );
}
