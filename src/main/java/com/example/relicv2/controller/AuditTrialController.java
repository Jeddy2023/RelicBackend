package com.example.relicv2.controller;

import com.example.relicv2.dto.AuditDto;
import com.example.relicv2.service.AuditTrialService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/audit")
public class AuditTrialController {
    private final AuditTrialService auditTrialService;

    public AuditTrialController(AuditTrialService auditTrialService) {
        this.auditTrialService = auditTrialService;
    }

    @GetMapping("/all")
    public ResponseEntity<Page<AuditDto>> getAllAuditTrials(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String audit,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "Newest") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<AuditDto> auditTrials = auditTrialService.getAllAuditTrials(userId, action, audit, startDate, endDate, sortBy);

        int start = Math.min(page * size, auditTrials.size());
        int end = Math.min((page + 1) * size, auditTrials.size());
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditDto> auditTrialPage = new PageImpl<>(auditTrials.subList(start, end), pageable, auditTrials.size());

        return new ResponseEntity<>(auditTrialPage, HttpStatus.OK);
    }
}
