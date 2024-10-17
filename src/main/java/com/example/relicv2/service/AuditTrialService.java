package com.example.relicv2.service;

import com.example.relicv2.dto.AuditDto;
import com.example.relicv2.model.User;

import java.util.List;

public interface AuditTrialService {
    List<AuditDto> getAllAuditTrials(
            Long userId,
            String action,
            String audit,
            String startDate,
            String endDate,
            String sortBy
    );
    void addToAuditTrial(User user, String action, String audit);
}
