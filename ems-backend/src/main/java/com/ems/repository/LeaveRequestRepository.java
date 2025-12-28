package com.ems.repository;

import com.ems.entity.LeaveRequest;
import com.ems.entity.LeaveRequest.LeaveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    
    Page<LeaveRequest> findByStatus(LeaveStatus status, Pageable pageable);
    
    List<LeaveRequest> findByEmployeeIdAndStatus(Long employeeId, LeaveStatus status);
    
    List<LeaveRequest> findByEmployeeId(Long employeeId);
    
    @Query("SELECT l FROM LeaveRequest l WHERE l.employee.id = :employeeId AND " +
           "((l.startDate BETWEEN :startDate AND :endDate) OR " +
           "(l.endDate BETWEEN :startDate AND :endDate) OR " +
           "(l.startDate <= :startDate AND l.endDate >= :endDate))")
    List<LeaveRequest> findOverlappingLeaves(@Param("employeeId") Long employeeId,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT COUNT(l) FROM LeaveRequest l WHERE l.status = 'PENDING'")
    Long countPendingRequests();
    
    @Query("SELECT COUNT(l) FROM LeaveRequest l WHERE l.status = 'APPROVED' AND " +
           "YEAR(l.startDate) = YEAR(CURRENT_DATE) AND MONTH(l.startDate) = MONTH(CURRENT_DATE)")
    Long countApprovedThisMonth();
    
    @Query("SELECT l FROM LeaveRequest l WHERE " +
           "(l.startDate BETWEEN :startDate AND :endDate) OR " +
           "(l.endDate BETWEEN :startDate AND :endDate)")
    List<LeaveRequest> findByDateRange(@Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate);
    
    @Query("SELECT l FROM LeaveRequest l WHERE l.employee.department = :department AND " +
           "l.status = 'APPROVED' AND " +
           "((l.startDate BETWEEN :startDate AND :endDate) OR " +
           "(l.endDate BETWEEN :startDate AND :endDate))")
    List<LeaveRequest> findByDepartmentAndDateRange(@Param("department") String department,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);
}