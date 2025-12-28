package com.ems.controller;

import com.ems.dto.*;
import com.ems.entity.LeaveRequest.LeaveStatus;
import com.ems.service.LeaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leave")
@RequiredArgsConstructor
@CrossOrigin(origins = {
    "http://localhost:4200",
    "https://employee-management-system-six-silk.vercel.app",
    "https://employee-management-system-jxdj.onrender.com"
})
public class LeaveController {
    
    private final LeaveService leaveService;
    
    // ========== FIXED: Add missing endpoints for Angular ==========
    
    @PutMapping("/requests/{id}/status")
    public ResponseEntity<?> updateLeaveStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        
        System.out.println("\n=== UPDATE LEAVE STATUS ===");
        System.out.println("Leave ID: " + id);
        System.out.println("Request: " + request);
        
        try {
            String status = request.get("status");
            String comments = request.get("comments");
            
            if (status == null) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Status is required"));
            }
            
            Long approverId = 1L; // TODO: Get from authenticated user
            
            if (status.equalsIgnoreCase("APPROVED")) {
                LeaveRequestDTO result = leaveService.approveLeave(id, approverId, comments);
                return ResponseEntity.ok(result);
            } else if (status.equalsIgnoreCase("REJECTED")) {
                LeaveRequestDTO result = leaveService.rejectLeave(id, approverId, comments);
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Invalid status value. Use 'APPROVED' or 'REJECTED'"));
            }
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse("Invalid status value: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to update leave status: " + e.getMessage()));
        }
    }
    
    @GetMapping("/balance")
    public ResponseEntity<?> getLeaveBalance() {
        try {
            // For now, use default employee ID. In real app, get from authentication
            Long employeeId = 1L;
            LeaveBalanceDTO balance = leaveService.getLeaveBalance(employeeId);
            return ResponseEntity.ok(balance);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to get leave balance: " + e.getMessage()));
        }
    }
    
    @GetMapping("/types")
    public ResponseEntity<?> getLeaveTypes() {
        try {
            // Return available leave types
            Map<String, Object> response = new HashMap<>();
            response.put("types", List.of(
                Map.of("value", "VACATION", "label", "Vacation", "icon", "beach_access"),
                Map.of("value", "SICK", "label", "Sick Leave", "icon", "medical_services"),
                Map.of("value", "PERSONAL", "label", "Personal", "icon", "person"),
                Map.of("value", "MATERNITY", "label", "Maternity", "icon", "family_restroom"),
                Map.of("value", "PATERNITY", "label", "Paternity", "icon", "family_restroom"),
                Map.of("value", "UNPAID", "label", "Unpaid Leave", "icon", "money_off")
            ));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to get leave types: " + e.getMessage()));
        }
    }
    
    // ========== EXISTING ENDPOINTS (Fixed) ==========
    
    @PostMapping("/request")
    public ResponseEntity<?> requestLeave(@RequestBody LeaveRequestDTO dto) {
        System.out.println("\n=== LEAVE REQUEST ===");
        System.out.println("Leave Type: " + dto.getLeaveType());
        System.out.println("Start Date: " + dto.getStartDate());
        System.out.println("End Date: " + dto.getEndDate());
        System.out.println("Reason: " + dto.getReason());
        
        try {
            // TODO: Get employeeId from authenticated user
            Long employeeId = 1L;
            LeaveRequestDTO result = leaveService.requestLeave(dto, employeeId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to request leave: " + e.getMessage()));
        }
    }
    
    @GetMapping("/requests")
    public ResponseEntity<?> getLeaveRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<LeaveRequestDTO> requests;
            
            if (status != null && !status.isEmpty()) {
                try {
                    LeaveStatus leaveStatus = LeaveStatus.valueOf(status.toUpperCase());
                    requests = leaveService.getLeaveRequests(pageable, leaveStatus);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest()
                        .body(createErrorResponse("Invalid status value: " + status));
                }
            } else {
                requests = leaveService.getLeaveRequests(pageable, null);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", requests.getContent());
            response.put("currentPage", requests.getNumber());
            response.put("totalElements", requests.getTotalElements());
            response.put("totalPages", requests.getTotalPages());
            response.put("pageSize", requests.getSize());
            response.put("hasNext", requests.hasNext());
            response.put("hasPrevious", requests.hasPrevious());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to fetch leave requests: " + e.getMessage()));
        }
    }
    
    @GetMapping("/my-requests")
    public ResponseEntity<?> getMyLeaveRequests() {
        try {
            // TODO: Get employeeId from authenticated user
            Long employeeId = 1L;
            List<LeaveRequestDTO> requests = leaveService.getEmployeeLeaves(employeeId);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to fetch my leave requests: " + e.getMessage()));
        }
    }
    
    @PutMapping("/requests/{id}/approve")
    public ResponseEntity<?> approveLeave(
            @PathVariable Long id,
            @RequestParam(required = false) String comments) {
        try {
            // TODO: Get approverId from authenticated user
            Long approverId = 1L;
            LeaveRequestDTO result = leaveService.approveLeave(id, approverId, comments);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to approve leave: " + e.getMessage()));
        }
    }
    
    @PutMapping("/requests/{id}/reject")
    public ResponseEntity<?> rejectLeave(
            @PathVariable Long id,
            @RequestParam(required = false) String comments) {
        try {
            // TODO: Get approverId from authenticated user
            Long approverId = 1L;
            LeaveRequestDTO result = leaveService.rejectLeave(id, approverId, comments);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to reject leave: " + e.getMessage()));
        }
    }
    
    @PutMapping("/requests/{id}/cancel")
    public ResponseEntity<?> cancelLeave(@PathVariable Long id) {
        try {
            // TODO: Get employeeId from authenticated user
            Long employeeId = 1L;
            LeaveRequestDTO result = leaveService.cancelLeave(id, employeeId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to cancel leave: " + e.getMessage()));
        }
    }
    
    @GetMapping("/balance/{employeeId}")
    public ResponseEntity<?> getLeaveBalance(@PathVariable Long employeeId) {
        try {
            LeaveBalanceDTO balance = leaveService.getLeaveBalance(employeeId);
            return ResponseEntity.ok(balance);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to get leave balance: " + e.getMessage()));
        }
    }
    
    @GetMapping("/policy")
    public ResponseEntity<?> getLeavePolicy() {
        try {
            LeavePolicyDTO policy = leaveService.getCurrentPolicy();
            return ResponseEntity.ok(policy);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to get leave policy: " + e.getMessage()));
        }
    }
    
    @PutMapping("/policy")
    public ResponseEntity<?> updateLeavePolicy(@RequestBody LeavePolicyDTO dto) {
        try {
            LeavePolicyDTO updated = leaveService.updatePolicy(dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to update leave policy: " + e.getMessage()));
        }
    }
    
    @GetMapping("/calendar")
    public ResponseEntity<?> getTeamCalendar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String department) {
        
        try {
            List<LeaveRequestDTO> calendar = leaveService.getTeamCalendar(startDate, endDate, department);
            return ResponseEntity.ok(calendar);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to get team calendar: " + e.getMessage()));
        }
    }
    
    @GetMapping("/stats")
    public ResponseEntity<?> getLeaveStats() {
        try {
            LeaveStatsDTO stats = leaveService.getLeaveStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to get leave statistics: " + e.getMessage()));
        }
    }
    
    // ========== ADD THESE HELPER ENDPOINTS ==========
    
    @GetMapping("/dashboard-stats")
    public ResponseEntity<?> getDashboardStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Get leave stats
            LeaveStatsDTO leaveStats = leaveService.getLeaveStats();
            stats.put("pendingRequests", leaveStats.getPendingRequests());
            stats.put("approvedThisMonth", leaveStats.getApprovedThisMonth());
            stats.put("rejectedThisMonth", leaveStats.getRejectedThisMonth());
            stats.put("totalLeavesTaken", leaveStats.getTotalLeavesTaken());
            
            // Add mock data for demo
            stats.put("upcomingLeaves", 5);
            stats.put("onLeaveToday", 2);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to get dashboard stats: " + e.getMessage()));
        }
    }
    
    @GetMapping("/test")
    public ResponseEntity<?> testLeaveEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Leave API is working!");
        response.put("status", "OK");
        response.put("timestamp", System.currentTimeMillis());
        response.put("endpoints", List.of(
            "/api/leave/request",
            "/api/leave/requests",
            "/api/leave/my-requests",
            "/api/leave/balance",
            "/api/leave/calendar",
            "/api/leave/stats"
        ));
        return ResponseEntity.ok(response);
    }
    
    // ========== HELPER METHODS ==========
    
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        errorResponse.put("timestamp", System.currentTimeMillis());
        errorResponse.put("success", false);
        return errorResponse;
    }
    
    private Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("message", message);
        successResponse.put("timestamp", System.currentTimeMillis());
        successResponse.put("success", true);
        return successResponse;
    }
}