package com.ems.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveStatsDTO {
    private Long pendingRequests;
    private Long approvedThisMonth;
    private Long rejectedThisMonth;
    private Long totalLeavesTaken;
    private Double averageLeaveDuration;
}