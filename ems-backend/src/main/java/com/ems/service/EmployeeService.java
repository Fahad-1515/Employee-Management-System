package com.ems.service;

import com.ems.entity.Employee;
import com.ems.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    public Page<Employee> getAllEmployees(int page, int size, String search, 
                                         String department, String position, 
                                         Double minSalary, Double maxSalary) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("firstName").ascending());
        
        // Use advanced search if any advanced filters are provided
        if (department != null || position != null || minSalary != null || maxSalary != null) {
            return employeeRepository.advancedSearch(search, department, position, minSalary, maxSalary, pageable);
        }
        // Otherwise use basic search
        else if (search != null && !search.trim().isEmpty()) {
            return employeeRepository.searchEmployees(search.trim(), pageable);
        } else {
            return employeeRepository.findAll(pageable);
        }
    }

    // Keep the old method for backward compatibility (optional)
    public Page<Employee> getAllEmployees(int page, int size, String search) {
        return getAllEmployees(page, size, search, null, null, null, null);
    }

    public Employee getEmployeeById(Long id) {
        Optional<Employee> employee = employeeRepository.findById(id);
        return employee.orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
    }

    public Employee saveEmployee(Employee employee) {
        // Check if email already exists (for new employees)
        if (employee.getId() == null && employeeRepository.existsByEmail(employee.getEmail())) {
            throw new RuntimeException("Email already exists: " + employee.getEmail());
        }
        
        // For updates, check if email exists for other employees
        if (employee.getId() != null) {
            Employee existing = getEmployeeById(employee.getId());
            if (!existing.getEmail().equals(employee.getEmail()) && 
                employeeRepository.existsByEmail(employee.getEmail())) {
                throw new RuntimeException("Email already exists: " + employee.getEmail());
            }
        }
        
        return employeeRepository.save(employee);
    }

    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new RuntimeException("Employee not found with id: " + id);
        }
        employeeRepository.deleteById(id);
    }

    // ========== FIXED: Added null check ==========
    public List<String> getDistinctDepartments() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("🔍 EmployeeService.getDistinctDepartments() called");
        
        try {
            List<String> departments = employeeRepository.findDistinctDepartments();
            System.out.println("📊 Database returned: " + departments);
            System.out.println("📊 Is null? " + (departments == null));
            
            // ✅ FIX: Check for null FIRST, then check if empty
            if (departments == null || departments.isEmpty()) {
                System.out.println("⚠️  No departments in database, using defaults");
                List<String> defaultDepartments = Arrays.asList(
                    "IT", "HR", "Finance", "Marketing", 
                    "Sales", "Operations", "Support", "Administration",
                    "Engineering", "Customer Service"
                );
                System.out.println("✅ Returning defaults: " + defaultDepartments);
                return defaultDepartments;
            }
            
            System.out.println("✅ Returning from database: " + departments);
            return departments;
            
        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
            
            // Always return something, never null
            List<String> fallback = Arrays.asList("IT", "HR", "Finance", "Marketing", "Sales");
            System.out.println("🔄 Returning fallback due to error: " + fallback);
            return fallback;
        } finally {
            System.out.println("=".repeat(50) + "\n");
        }
    }

    // ========== FIXED: Added null check ==========
    public List<String> getDistinctPositions() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("🔍 EmployeeService.getDistinctPositions() called");
        
        try {
            List<String> positions = employeeRepository.findDistinctPositions();
            System.out.println("📊 Database returned: " + positions);
            System.out.println("📊 Is null? " + (positions == null));
            
            // ✅ FIX: Check for null FIRST, then check if empty
            if (positions == null || positions.isEmpty()) {
                System.out.println("⚠️  No positions in database, using defaults");
                List<String> defaultPositions = Arrays.asList(
                    "Software Engineer", "HR Manager", "Financial Analyst",
                    "Marketing Specialist", "Sales Representative", "Operations Manager",
                    "System Administrator", "Frontend Developer", "Backend Developer",
                    "Data Analyst", "Project Manager", "UI/UX Designer"
                );
                System.out.println("✅ Returning defaults: " + defaultPositions);
                return defaultPositions;
            }
            
            System.out.println("✅ Returning from database: " + positions);
            return positions;
            
        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
            
            // Always return something, never null
            List<String> fallback = Arrays.asList("Software Engineer", "HR Manager", "Financial Analyst");
            System.out.println("🔄 Returning fallback due to error: " + fallback);
            return fallback;
        } finally {
            System.out.println("=".repeat(50) + "\n");
        }
    }

    public Page<Employee> getEmployeesByDepartment(String department, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("firstName").ascending());
        return employeeRepository.findByDepartment(department, pageable);
    }

    public List<Employee> getAllEmployeesForExport() {
        return employeeRepository.findAll(Sort.by("firstName").ascending());
    }
    
    public boolean emailExists(String email) {
        return employeeRepository.existsByEmail(email);
    }

    public Map<String, Object> getEmployeeStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEmployees", employeeRepository.count());
        stats.put("totalDepartments", getDepartmentCount());
        return stats;
    }

    public long getDepartmentCount() {
        List<String> departments = getDistinctDepartments(); // Use the fixed method
        return departments != null ? departments.size() : 0;
    }
}
