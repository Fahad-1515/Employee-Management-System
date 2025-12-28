package com.ems.dto;

import com.ems.entity.LeaveRequest.LeaveType;
import com.ems.entity.LeaveRequest.LeaveStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestDTO {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalDays;
    private String reason;
    private LeaveStatus status;
    private String approvedBy;
    private String approvalComments;
    private LocalDateTime approvedDate;
    private LocalDateTime createdAt;
}