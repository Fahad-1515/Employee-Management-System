package com.ems.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeavePolicyDTO {
    private Long id;
    private Integer vacationDays;
    private Integer sickDays;
    private Integer personalDays;
    private Integer maternityDays;
    private Integer paternityDays;
    private Integer maxConsecutiveDays;
    private Integer advanceNoticeDays;
    private Boolean carryOverEnabled;
    private Integer maxCarryOverDays;
}