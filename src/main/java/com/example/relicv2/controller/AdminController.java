package com.example.relicv2.controller;

import com.example.relicv2.dto.AdminUserDetailsDto;
import com.example.relicv2.dto.SuspensionDto;
import com.example.relicv2.dto.TotalAdminCountsDto;
import com.example.relicv2.enums.Role;
import com.example.relicv2.service.AdminService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class AdminController {
    @Autowired
    private final AdminService adminService;

    @GetMapping("/totals")
    public TotalAdminCountsDto getTotalCounts() {
        return adminService.getTotalCounts();
    }

    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserDetailsDto>> getAllUsers(
            @RequestParam(required = false) Boolean isVerified,
            @RequestParam(required = false) Boolean isBlocked,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startDateStr,
            @RequestParam(required = false) String endDateStr,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<AdminUserDetailsDto> users = adminService.getAllUsers(isVerified, isBlocked, role, keyword, startDateStr, endDateStr, page, size);

        int start = Math.min(page * size, users.size());
        int end = Math.min((page + 1) * size, users.size());
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminUserDetailsDto> userPage = new PageImpl<>(users.subList(start, end), pageable, users.size());

        return new ResponseEntity<>(userPage, HttpStatus.OK);
    }

    @PutMapping("/users/{userId}/block")
    public ResponseEntity<String> blockUser(@PathVariable Long userId) {
        adminService.blockUser(userId);
        return ResponseEntity.ok("User blocked successfully");
    }

    @PutMapping("/users/{userId}/unblock")
    public ResponseEntity<String> unBlockUser(@PathVariable Long userId) {
        adminService.unBlockUser(userId);
        return ResponseEntity.ok("User unblocked successfully");
    }

    @PutMapping("/users/{userId}/make-admin")
    public ResponseEntity<String> makeUserAdmin(@PathVariable Long userId) {
        adminService.makeUserAdmin(userId);
        return ResponseEntity.ok("User is now an admin");
    }

    @PostMapping("/users/suspend")
    public ResponseEntity<String> suspendUser(@RequestBody SuspensionDto suspensionDto) {
        adminService.suspendUser(suspensionDto);
        return ResponseEntity.ok("User suspended successfully");
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.ok("User deleted successfully");
    }

}
