package com.example.relicv2.controller;

import com.example.relicv2.dto.GetSuspensionDto;
import com.example.relicv2.service.SuspensionService;
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
@RequestMapping("/api/v1/suspensions")
@CrossOrigin(origins = "*")
public class SuspensionController {

    @Autowired
    private SuspensionService suspensionService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<GetSuspensionDto>> getSuspensionsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<GetSuspensionDto> suspensions = suspensionService.getSuspensionsByUser(userId);

        int start = Math.min(page * size, suspensions.size());
        int end = Math.min((page + 1) * size, suspensions.size());
        Pageable pageable = PageRequest.of(page, size);
        Page<GetSuspensionDto> suspensionPage = new PageImpl<>(suspensions.subList(start, end), pageable, suspensions.size());

        return new ResponseEntity<>(suspensionPage, HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<Page<GetSuspensionDto>> getAllSuspensions(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Boolean suspended,
            @RequestParam(required = false, defaultValue = "dateDesc") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<GetSuspensionDto> suspensions = suspensionService.getAllSuspensions(startDate, endDate, suspended, sortBy);

        int start = Math.min(page * size, suspensions.size());
        int end = Math.min((page + 1) * size, suspensions.size());
        Pageable pageable = PageRequest.of(page, size);
        Page<GetSuspensionDto> suspensionPage = new PageImpl<>(suspensions.subList(start, end), pageable, suspensions.size());

        return new ResponseEntity<>(suspensionPage, HttpStatus.OK);
    }
}
