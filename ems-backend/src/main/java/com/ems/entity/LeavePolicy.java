package com.ems.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "leave_policies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeavePolicy {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Integer vacationDays = 20;
    
    @Column(nullable = false)
    private Integer sickDays = 10;
    
    @Column(nullable = false)
    private Integer personalDays = 5;
    
    @Column(nullable = false)
    private Integer maternityDays = 90;
    
    @Column(nullable = false)
    private Integer paternityDays = 14;
    
    @Column(name = "max_consecutive_days", nullable = false)
    private Integer maxConsecutiveDays = 30;
    
    @Column(name = "advance_notice_days", nullable = false)
    private Integer advanceNoticeDays = 3;
    
    @Column(name = "carry_over_enabled", nullable = false)
    private Boolean carryOverEnabled = true;
    
    @Column(name = "max_carry_over_days", nullable = false)
    private Integer maxCarryOverDays = 10;
}