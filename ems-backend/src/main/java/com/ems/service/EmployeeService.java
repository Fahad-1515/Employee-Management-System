package com.ems.service;

import com.ems.entity.Employee;
import com.ems.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.Arrays; 
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    // UPDATED METHOD - Now accepts all search parameters
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

    public List<String> getDistinctDepartments() {
    List<String> departments = employeeRepository.findDistinctDepartments();
    
    // If no departments exist yet (first employee), return common defaults
    if (departments.isEmpty()) {
        return Arrays.asList(
            "IT", "HR", "Finance", "Marketing", 
            "Sales", "Operations", "Support", "Administration"
        );
    }
    
    return departments;
}
    public List<String> getDistinctPositions() {
    List<String> positions = employeeRepository.findDistinctPositions();
    
    // If no positions exist yet (first employee), return common defaults
    if (positions.isEmpty()) {
        return Arrays.asList(
            "Software Engineer", "HR Manager", "Financial Analyst",
            "Marketing Specialist", "Sales Representative", "Operations Manager",
            "System Administrator", "Frontend Developer", "Backend Developer",
            "Data Analyst", "Product Manager", "Quality Assurance"
        );
    }
    
    return positions;
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
    // Add more stats if needed
    return stats;
}

    public long getDepartmentCount() {
    return employeeRepository.findDistinctDepartments().size();
}
}
