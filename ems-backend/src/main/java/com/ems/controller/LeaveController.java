package com.ems.controller;

import com.ems.dto.*;
import com.ems.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/leave")
@RequiredArgsConstructor
public class LeaveController {
    
    private final LeaveService leaveService;
    
    // Leave Requests
    
    @PostMapping("/request")
    public ResponseEntity<LeaveRequestDTO> requestLeave(
            @RequestBody LeaveRequestDTO dto,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
        // In real app, get employeeId from authenticated user
        Long employeeId = 1L; // Replace with actual user ID
        LeaveRequestDTO result = leaveService.requestLeave(dto, employeeId);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/requests")
    public ResponseEntity<Page<LeaveRequestDTO>> getLeaveRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<LeaveRequestDTO> requests = leaveService.getLeaveRequests(pageable, 
            status != null ? com.ems.entity.LeaveRequest.LeaveStatus.valueOf(status) : null);
        return ResponseEntity.ok(requests);
    }
    
    @GetMapping("/my-requests")
    public ResponseEntity<List<LeaveRequestDTO>> getMyLeaveRequests(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
        Long employeeId = 1L; // Replace with actual user ID
        List<LeaveRequestDTO> requests = leaveService.getEmployeeLeaves(employeeId);
        return ResponseEntity.ok(requests);
    }
    
    @PutMapping("/requests/{id}/approve")
    public ResponseEntity<LeaveRequestDTO> approveLeave(
            @PathVariable Long id,
            @RequestParam(required = false) String comments,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
        Long approverId = 1L; // Replace with actual approver ID
        LeaveRequestDTO result = leaveService.approveLeave(id, approverId, comments);
        return ResponseEntity.ok(result);
    }
    
    @PutMapping("/requests/{id}/reject")
    public ResponseEntity<LeaveRequestDTO> rejectLeave(
            @PathVariable Long id,
            @RequestParam(required = false) String comments,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
        Long approverId = 1L; // Replace with actual approver ID
        LeaveRequestDTO result = leaveService.rejectLeave(id, approverId, comments);
        return ResponseEntity.ok(result);
    }
    
    @PutMapping("/requests/{id}/cancel")
    public ResponseEntity<LeaveRequestDTO> cancelLeave(
            @PathVariable Long id,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
        Long employeeId = 1L; // Replace with actual user ID
        LeaveRequestDTO result = leaveService.cancelLeave(id, employeeId);
        return ResponseEntity.ok(result);
    }
    
    // Leave Balance
    
    @GetMapping("/balance/{employeeId}")
    public ResponseEntity<LeaveBalanceDTO> getLeaveBalance(@PathVariable Long employeeId) {
        LeaveBalanceDTO balance = leaveService.getLeaveBalance(employeeId);
        return ResponseEntity.ok(balance);
    }
    
    @GetMapping("/balance/my")
    public ResponseEntity<LeaveBalanceDTO> getMyLeaveBalance(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
        Long employeeId = 1L; // Replace with actual user ID
        LeaveBalanceDTO balance = leaveService.getLeaveBalance(employeeId);
        return ResponseEntity.ok(balance);
    }
    
    // Leave Policy
    
    @GetMapping("/policy")
    public ResponseEntity<LeavePolicyDTO> getLeavePolicy() {
        LeavePolicyDTO policy = leaveService.getCurrentPolicy();
        return ResponseEntity.ok(policy);
    }
    
    @PutMapping("/policy")
    public ResponseEntity<LeavePolicyDTO> updateLeavePolicy(@RequestBody LeavePolicyDTO dto) {
        LeavePolicyDTO updated = leaveService.updatePolicy(dto);
        return ResponseEntity.ok(updated);
    }
    
    // Calendar
    
    @GetMapping("/calendar")
    public ResponseEntity<List<LeaveRequestDTO>> getTeamCalendar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String department) {
        List<LeaveRequestDTO> calendar = leaveService.getTeamCalendar(startDate, endDate, department);
        return ResponseEntity.ok(calendar);
    }
    
    // Statistics
    
    @GetMapping("/stats")
    public ResponseEntity<LeaveStatsDTO> getLeaveStats() {
        LeaveStatsDTO stats = leaveService.getLeaveStats();
        return ResponseEntity.ok(stats);
    }
}
