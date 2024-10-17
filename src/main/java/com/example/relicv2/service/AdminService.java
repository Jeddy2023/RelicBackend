package com.example.relicv2.service;

import com.example.relicv2.dto.AdminUserDetailsDto;
import com.example.relicv2.dto.SuspensionDto;
import com.example.relicv2.dto.TotalAdminCountsDto;
import com.example.relicv2.enums.Role;

import java.util.List;

public interface AdminService {
    TotalAdminCountsDto getTotalCounts();
    List<AdminUserDetailsDto> getAllUsers(
            Boolean isVerified,
            Boolean isBlocked,
            Role role,
            String keyword,
            String startDateStr,
            String endDateStr,
            int page,
            int size
    );
    void blockUser(Long userId);
    void unBlockUser(Long userId);
    void makeUserAdmin(Long userId);
    void suspendUser(SuspensionDto suspensionDto);
    void deleteUser(Long userId);
}
