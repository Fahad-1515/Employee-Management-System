package com.ems.service;

import com.ems.dto.EmployeeDTO;
import com.ems.entity.Employee;
import com.ems.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmployeeService {
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private CSVImportService csvImportService;

    // ========== UPDATED CONVERSION METHODS ==========
    
    public EmployeeDTO convertToDTO(Employee employee) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(employee.getId());
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setEmail(employee.getEmail());
        dto.setDepartment(employee.getDepartment());
        dto.setPosition(employee.getPosition());
        
        // Now these can be null - no need for null checks
        dto.setVacationDays(employee.getVacationDays());
        dto.setSickDays(employee.getSickDays());
        dto.setPersonalDays(employee.getPersonalDays());
        dto.setUsedVacation(employee.getUsedVacation());
        dto.setUsedSick(employee.getUsedSick());
        dto.setUsedPersonal(employee.getUsedPersonal());
        
        return dto;
    }
    
    private Employee convertToEntity(EmployeeDTO dto) {
        Employee employee = new Employee();
        employee.setId(dto.getId());
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setDepartment(dto.getDepartment());
        employee.setPosition(dto.getPosition());
        
        // Set default phone values if not provided
        employee.setPhoneNumber("+1234567890");
        employee.setCountryCode("+1");
        employee.setSalary(50000.0);
        
        // Set leave balances from DTO - can be null
        employee.setVacationDays(dto.getVacationDays());
        employee.setSickDays(dto.getSickDays());
        employee.setPersonalDays(dto.getPersonalDays());
        employee.setUsedVacation(dto.getUsedVacation());
        employee.setUsedSick(dto.getUsedSick());
        employee.setUsedPersonal(dto.getUsedPersonal());
        
        return employee;
    }
    
    // ========== EXISTING METHODS (WITH MINOR UPDATES) ==========
    
    @Transactional
    public List<Employee> createEmployeesBulk(List<EmployeeDTO> employeeDTOs) {
        List<Employee> employees = employeeDTOs.stream()
            .map(dto -> {
                Employee employee = convertToEntity(dto);
                // Ensure defaults for any null values
                if (employee.getVacationDays() == null) employee.setVacationDays(20);
                if (employee.getSickDays() == null) employee.setSickDays(10);
                if (employee.getPersonalDays() == null) employee.setPersonalDays(5);
                if (employee.getUsedVacation() == null) employee.setUsedVacation(0);
                if (employee.getUsedSick() == null) employee.setUsedSick(0);
                if (employee.getUsedPersonal() == null) employee.setUsedPersonal(0);
                return employee;
            })
            .collect(Collectors.toList());
        
        return employeeRepository.saveAll(employees);
    }

    @Transactional
    public int deleteEmployeesBulk(List<Long> employeeIds) {
        int deleted = 0;
        for (Long id : employeeIds) {
            try {
                employeeRepository.deleteById(id);
                deleted++;
            } catch (Exception e) {
                System.err.println("Error deleting employee " + id + ": " + e.getMessage());
                // Continue with other deletions
            }
        }
        return deleted;
    }

    @Transactional
    public int updateDepartmentBulk(List<Long> employeeIds, String department) {
        int updated = 0;
        for (Long id : employeeIds) {
            Employee employee = employeeRepository.findById(id).orElse(null);
            if (employee != null) {
                employee.setDepartment(department);
                employeeRepository.save(employee);
                updated++;
            }
        }
        return updated;
    }

    public List<Employee> importFromCSV(MultipartFile file) throws Exception {
        return csvImportService.importEmployeesFromCSV(file);
    }

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
        
        // Ensure leave balances have defaults if null
        if (employee.getVacationDays() == null) employee.setVacationDays(20);
        if (employee.getSickDays() == null) employee.setSickDays(10);
        if (employee.getPersonalDays() == null) employee.setPersonalDays(5);
        if (employee.getUsedVacation() == null) employee.setUsedVacation(0);
        if (employee.getUsedSick() == null) employee.setUsedSick(0);
        if (employee.getUsedPersonal() == null) employee.setUsedPersonal(0);
        
        return employeeRepository.save(employee);
    }

    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new RuntimeException("Employee not found with id: " + id);
        }
        employeeRepository.deleteById(id);
    }

    public List<String> getDistinctDepartments() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("🔍 EmployeeService.getDistinctDepartments() called");
        
        try {
            List<String> departments = employeeRepository.findDistinctDepartments();
            System.out.println("📊 Database returned: " + departments);
            System.out.println("📊 Is null? " + (departments == null));
            
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
            
            List<String> fallback = Arrays.asList("IT", "HR", "Finance", "Marketing", "Sales");
            System.out.println("🔄 Returning fallback due to error: " + fallback);
            return fallback;
        } finally {
            System.out.println("=".repeat(50) + "\n");
        }
    }

    public List<String> getDistinctPositions() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("🔍 EmployeeService.getDistinctPositions() called");
        
        try {
            List<String> positions = employeeRepository.findDistinctPositions();
            System.out.println("📊 Database returned: " + positions);
            System.out.println("📊 Is null? " + (positions == null));
            
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
        
        try {
            long totalEmployees = employeeRepository.count();
            List<String> departments = getDistinctDepartments();
            long totalDepartments = departments != null ? departments.size() : 0;
            
            stats.put("totalEmployees", totalEmployees);
            stats.put("totalDepartments", totalDepartments);
            
            List<Employee> allEmployees = employeeRepository.findAll();
            if (!allEmployees.isEmpty()) {
                double totalSalary = allEmployees.stream()
                    .filter(e -> e.getSalary() != null)
                    .mapToDouble(Employee::getSalary)
                    .sum();
                double avgSalary = totalSalary / allEmployees.size();
                double minSalary = allEmployees.stream()
                    .filter(e -> e.getSalary() != null)
                    .mapToDouble(Employee::getSalary)
                    .min()
                    .orElse(0.0);
                double maxSalary = allEmployees.stream()
                    .filter(e -> e.getSalary() != null)
                    .mapToDouble(Employee::getSalary)
                    .max()
                    .orElse(0.0);
                
                stats.put("averageSalary", String.format("%.2f", avgSalary));
                stats.put("minSalary", String.format("%.2f", minSalary));
                stats.put("maxSalary", String.format("%.2f", maxSalary));
                stats.put("totalSalaryExpense", String.format("%.2f", totalSalary));
            }
            
        } catch (Exception e) {
            System.err.println("Error calculating statistics: " + e.getMessage());
            stats.put("error", "Failed to calculate statistics");
        }
        
        return stats;
    }

    public long getDepartmentCount() {
        List<String> departments = getDistinctDepartments();
        return departments != null ? departments.size() : 0;
    }
    
    // ========== ADDITIONAL HELPER METHODS ==========
    
    public List<EmployeeDTO> getAllEmployeesAsDTO() {
        List<Employee> employees = employeeRepository.findAll();
        return employees.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    
    public Employee updateEmployeeLeaveBalances(Long id, Integer vacationDays, Integer sickDays, 
                                              Integer personalDays) {
        Employee employee = getEmployeeById(id);
        if (vacationDays != null) employee.setVacationDays(vacationDays);
        if (sickDays != null) employee.setSickDays(sickDays);
        if (personalDays != null) employee.setPersonalDays(personalDays);
        return employeeRepository.save(employee);
    }
    
    public EmployeeDTO getEmployeeDTOById(Long id) {
        Employee employee = getEmployeeById(id);
        return convertToDTO(employee);
    }
    
    // ========== NEW METHOD: For creating Employee from form data ==========
    
    public Employee createEmployeeFromFormData(String firstName, String lastName, String email,
                                              String phoneNumber, String countryCode,
                                              String department, String position, Double salary) {
        Employee employee = new Employee();
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmail(email);
        employee.setPhoneNumber(phoneNumber);
        employee.setCountryCode(countryCode);
        employee.setDepartment(department);
        employee.setPosition(position);
        employee.setSalary(salary);
        
        // Set default leave balances
        employee.setVacationDays(20);
        employee.setSickDays(10);
        employee.setPersonalDays(5);
        employee.setUsedVacation(0);
        employee.setUsedSick(0);
        employee.setUsedPersonal(0);
        
        return saveEmployee(employee);
    }
}