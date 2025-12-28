package com.ems.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalanceDTO {
    private Long employeeId;
    private Integer vacationDays;
    private Integer sickDays;
    private Integer personalDays;
    private Integer maternityDays;
    private Integer paternityDays;
    private Integer usedVacation;
    private Integer usedSick;
    private Integer usedPersonal;
    private Integer remainingVacation;
    private Integer remainingSick;
    private Integer remainingPersonal;
}