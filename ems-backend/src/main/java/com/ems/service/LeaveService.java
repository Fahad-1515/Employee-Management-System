package com.ems.service;

import com.ems.dto.LeaveRequestDTO;
import com.ems.dto.LeaveBalanceDTO;
import com.ems.dto.LeavePolicyDTO;
import com.ems.dto.LeaveStatsDTO;
import com.ems.entity.LeaveRequest;
import com.ems.entity.LeavePolicy;
import com.ems.entity.Employee;
import com.ems.entity.LeaveRequest.LeaveStatus;
import com.ems.repository.LeaveRequestRepository;
import com.ems.repository.LeavePolicyRepository;
import com.ems.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaveService {
    
    private final LeaveRequestRepository leaveRequestRepository;
    private final LeavePolicyRepository leavePolicyRepository;
    private final EmployeeRepository employeeRepository;
    
    // Leave Request Methods
    
    @Transactional
    public LeaveRequestDTO requestLeave(LeaveRequestDTO dto, Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));
        
        // Validate leave balance
        validateLeaveBalance(employee, dto);
        
        // Check for overlapping leaves
        checkOverlappingLeaves(employeeId, dto.getStartDate(), dto.getEndDate());
        
        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEmployee(employee);
        leaveRequest.setLeaveType(dto.getLeaveType());
        leaveRequest.setStartDate(dto.getStartDate());
        leaveRequest.setEndDate(dto.getEndDate());
        
        // Calculate total days
        long days = ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;
        leaveRequest.setTotalDays((int) days);
        
        leaveRequest.setReason(dto.getReason());
        leaveRequest.setStatus(LeaveStatus.PENDING);
        
        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        
        return mapToDTO(saved);
    }
    
    @Transactional
    public LeaveRequestDTO approveLeave(Long leaveId, Long approverId, String comments) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveId)
            .orElseThrow(() -> new RuntimeException("Leave request not found"));
        
        // Get approver employee
        Employee approver = employeeRepository.findById(approverId)
            .orElseThrow(() -> new RuntimeException("Approver not found"));
        
        // Update leave balance
        updateLeaveBalance(leaveRequest, true);
        
        leaveRequest.setStatus(LeaveStatus.APPROVED);
        leaveRequest.setApprovedBy(approver);
        leaveRequest.setApprovalComments(comments); // Fixed method name
        leaveRequest.setApprovedDate(LocalDateTime.now()); // Fixed method name
        
        LeaveRequest updated = leaveRequestRepository.save(leaveRequest);
        
        return mapToDTO(updated);
    }
    
    @Transactional
    public LeaveRequestDTO rejectLeave(Long leaveId, Long approverId, String comments) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveId)
            .orElseThrow(() -> new RuntimeException("Leave request not found"));
        
        // Get approver employee
        Employee approver = employeeRepository.findById(approverId)
            .orElseThrow(() -> new RuntimeException("Approver not found"));
        
        leaveRequest.setStatus(LeaveStatus.REJECTED);
        leaveRequest.setApprovedBy(approver);
        leaveRequest.setApprovalComments(comments); // Fixed method name
        leaveRequest.setApprovedDate(LocalDateTime.now()); // Fixed method name
        
        LeaveRequest updated = leaveRequestRepository.save(leaveRequest);
        
        return mapToDTO(updated);
    }
    
    @Transactional
    public LeaveRequestDTO cancelLeave(Long leaveId, Long employeeId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveId)
            .orElseThrow(() -> new RuntimeException("Leave request not found"));
        
        if (!leaveRequest.getEmployee().getId().equals(employeeId)) {
            throw new RuntimeException("Unauthorized to cancel this leave");
        }
        
        if (leaveRequest.getStatus() == LeaveStatus.APPROVED) {
            // Refund leave balance
            updateLeaveBalance(leaveRequest, false);
        }
        
        leaveRequest.setStatus(LeaveStatus.CANCELLED);
        LeaveRequest updated = leaveRequestRepository.save(leaveRequest);
        
        return mapToDTO(updated);
    }
    
    // Leave Balance Methods
    
    public LeaveBalanceDTO getLeaveBalance(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> new RuntimeException("Employee not found"));
        
        return LeaveBalanceDTO.builder()
            .employeeId(employeeId)
            .vacationDays(employee.getVacationDays())
            .sickDays(employee.getSickDays())
            .personalDays(employee.getPersonalDays())
            .maternityDays(90) // From policy
            .paternityDays(14) // From policy
            .usedVacation(employee.getUsedVacation())
            .usedSick(employee.getUsedSick())
            .usedPersonal(employee.getUsedPersonal())
            .remainingVacation(employee.getVacationDays() - employee.getUsedVacation())
            .remainingSick(employee.getSickDays() - employee.getUsedSick())
            .remainingPersonal(employee.getPersonalDays() - employee.getUsedPersonal())
            .build();
    }
    
    // Leave Policy Methods
    
    public LeavePolicyDTO getCurrentPolicy() {
        LeavePolicy policy = leavePolicyRepository.findTopByOrderByIdDesc();
        if (policy == null) {
            policy = createDefaultPolicy();
        }
        return mapToPolicyDTO(policy);
    }
    
    @Transactional
    public LeavePolicyDTO updatePolicy(LeavePolicyDTO dto) {
        LeavePolicy policy = mapToPolicyEntity(dto);
        LeavePolicy saved = leavePolicyRepository.save(policy);
        return mapToPolicyDTO(saved);
    }
    
    // Statistics Methods
    
    public LeaveStatsDTO getLeaveStats() {
        long pending = leaveRequestRepository.countPendingRequests();
        long approvedThisMonth = leaveRequestRepository.countApprovedThisMonth();
        
        // Calculate average leave duration
        List<LeaveRequest> allLeaves = leaveRequestRepository.findAll();
        double averageDuration = allLeaves.stream()
            .filter(l -> l.getStatus() == LeaveStatus.APPROVED)
            .mapToInt(LeaveRequest::getTotalDays)
            .average()
            .orElse(0.0);
        
        // FIXED: Changed allLeaves.size() to (long) allLeaves.size() to avoid int to Long issue
        return LeaveStatsDTO.builder()
            .pendingRequests(pending)
            .approvedThisMonth(approvedThisMonth)
            .rejectedThisMonth(0L) // You can add this query
            .totalLeavesTaken((long) allLeaves.size()) // Fixed: cast int to long
            .averageLeaveDuration(Math.round(averageDuration * 10.0) / 10.0)
            .build();
    }
    
    // Calendar Methods
    
    public List<LeaveRequestDTO> getTeamCalendar(LocalDate startDate, LocalDate endDate, String department) {
        List<LeaveRequest> leaves;
        
        if (department != null && !department.isEmpty()) {
            leaves = leaveRequestRepository.findByDepartmentAndDateRange(department, startDate, endDate);
        } else {
            leaves = leaveRequestRepository.findByDateRange(startDate, endDate);
        }
        
        return leaves.stream()
            .filter(l -> l.getStatus() == LeaveStatus.APPROVED)
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
    
    // Helper Methods
    
    private void validateLeaveBalance(Employee employee, LeaveRequestDTO dto) {
        int requestedDays = (int) ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;
        
        switch (dto.getLeaveType()) {
            case VACATION:
                if (employee.getVacationDays() - employee.getUsedVacation() < requestedDays) {
                    throw new RuntimeException("Insufficient vacation days");
                }
                break;
            case SICK:
                if (employee.getSickDays() - employee.getUsedSick() < requestedDays) {
                    throw new RuntimeException("Insufficient sick days");
                }
                break;
            case PERSONAL:
                if (employee.getPersonalDays() - employee.getUsedPersonal() < requestedDays) {
                    throw new RuntimeException("Insufficient personal days");
                }
                break;
            // Other leave types may not have limits
        }
    }
    
    private void checkOverlappingLeaves(Long employeeId, LocalDate startDate, LocalDate endDate) {
        List<LeaveRequest> overlapping = leaveRequestRepository.findOverlappingLeaves(
            employeeId, startDate, endDate);
        
        overlapping = overlapping.stream()
            .filter(l -> l.getStatus() != LeaveStatus.REJECTED && 
                        l.getStatus() != LeaveStatus.CANCELLED)
            .collect(Collectors.toList());
        
        if (!overlapping.isEmpty()) {
            throw new RuntimeException("Leave request overlaps with existing leave");
        }
    }
    
    private void updateLeaveBalance(LeaveRequest leaveRequest, boolean deduct) {
        Employee employee = leaveRequest.getEmployee();
        int days = leaveRequest.getTotalDays();
        
        switch (leaveRequest.getLeaveType()) {
            case VACATION:
                employee.setUsedVacation(employee.getUsedVacation() + (deduct ? days : -days));
                break;
            case SICK:
                employee.setUsedSick(employee.getUsedSick() + (deduct ? days : -days));
                break;
            case PERSONAL:
                employee.setUsedPersonal(employee.getUsedPersonal() + (deduct ? days : -days));
                break;
            default:
                // Other leave types don't affect balance
                break;
        }
        
        employeeRepository.save(employee);
    }
    
    private LeavePolicy createDefaultPolicy() {
        LeavePolicy policy = new LeavePolicy();
        policy.setVacationDays(20);
        policy.setSickDays(10);
        policy.setPersonalDays(5);
        policy.setMaternityDays(90);
        policy.setPaternityDays(14);
        policy.setMaxConsecutiveDays(30);
        policy.setAdvanceNoticeDays(3);
        policy.setCarryOverEnabled(true);
        policy.setMaxCarryOverDays(10);
        return leavePolicyRepository.save(policy);
    }
    
    // Mapping methods
    private LeaveRequestDTO mapToDTO(LeaveRequest leave) {
        String approvedByName = null;
        if (leave.getApprovedBy() != null) {
            approvedByName = leave.getApprovedBy().getFirstName() + " " + leave.getApprovedBy().getLastName();
        }
        
        return LeaveRequestDTO.builder()
            .id(leave.getId())
            .employeeId(leave.getEmployee().getId())
            .employeeName(leave.getEmployee().getFirstName() + " " + leave.getEmployee().getLastName())
            .leaveType(leave.getLeaveType())
            .startDate(leave.getStartDate())
            .endDate(leave.getEndDate())
            .totalDays(leave.getTotalDays())
            .reason(leave.getReason())
            .status(leave.getStatus())
            .approvedBy(approvedByName)
            .approvalComments(leave.getApprovalComments()) // Fixed method name
            .approvedDate(leave.getApprovedDate()) // Fixed method name
            .createdAt(leave.getCreatedAt())
            .build();
    }
    
    private LeavePolicyDTO mapToPolicyDTO(LeavePolicy policy) {
        return LeavePolicyDTO.builder()
            .id(policy.getId())
            .vacationDays(policy.getVacationDays())
            .sickDays(policy.getSickDays())
            .personalDays(policy.getPersonalDays())
            .maternityDays(policy.getMaternityDays())
            .paternityDays(policy.getPaternityDays())
            .maxConsecutiveDays(policy.getMaxConsecutiveDays())
            .advanceNoticeDays(policy.getAdvanceNoticeDays())
            .carryOverEnabled(policy.getCarryOverEnabled())
            .maxCarryOverDays(policy.getMaxCarryOverDays())
            .build();
    }
    
    private LeavePolicy mapToPolicyEntity(LeavePolicyDTO dto) {
        LeavePolicy policy = new LeavePolicy();
        policy.setId(dto.getId());
        policy.setVacationDays(dto.getVacationDays());
        policy.setSickDays(dto.getSickDays());
        policy.setPersonalDays(dto.getPersonalDays());
        policy.setMaternityDays(dto.getMaternityDays());
        policy.setPaternityDays(dto.getPaternityDays());
        policy.setMaxConsecutiveDays(dto.getMaxConsecutiveDays());
        policy.setAdvanceNoticeDays(dto.getAdvanceNoticeDays());
        policy.setCarryOverEnabled(dto.getCarryOverEnabled());
        policy.setMaxCarryOverDays(dto.getMaxCarryOverDays());
        return policy;
    }
    
    // Get leave requests with pagination
    public Page<LeaveRequestDTO> getLeaveRequests(Pageable pageable, LeaveStatus status) {
        Page<LeaveRequest> leaves;
        if (status != null) {
            leaves = leaveRequestRepository.findByStatus(status, pageable);
        } else {
            leaves = leaveRequestRepository.findAll(pageable);
        }
        return leaves.map(this::mapToDTO);
    }
    
    // Get employee's leave requests
    public List<LeaveRequestDTO> getEmployeeLeaves(Long employeeId) {
        List<LeaveRequest> leaves = leaveRequestRepository.findByEmployeeId(employeeId);
        return leaves.stream()
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }
}