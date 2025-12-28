package com.ems.controller;

import com.ems.entity.Employee;
import com.ems.service.EmployeeService;
import com.ems.dto.EmployeeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = {
    "http://localhost:4200",
    "https://employee-management-system-six-silk.vercel.app",
    "https://employee-management-system-jxdj.onrender.com"
})
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    // ========== FIXED: Add missing endpoints for Angular ==========

    @GetMapping("/stats/salary")
    public ResponseEntity<?> getSalaryStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            List<Employee> employees = employeeService.getAllEmployeesForExport();
            
            if (!employees.isEmpty()) {
                double totalSalary = employees.stream()
                    .filter(e -> e.getSalary() != null)
                    .mapToDouble(Employee::getSalary)
                    .sum();
                double avgSalary = totalSalary / employees.size();
                double minSalary = employees.stream()
                    .filter(e -> e.getSalary() != null)
                    .mapToDouble(Employee::getSalary)
                    .min()
                    .orElse(0.0);
                double maxSalary = employees.stream()
                    .filter(e -> e.getSalary() != null)
                    .mapToDouble(Employee::getSalary)
                    .max()
                    .orElse(0.0);
                
                stats.put("averageSalary", String.format("%.2f", avgSalary));
                stats.put("minSalary", String.format("%.2f", minSalary));
                stats.put("maxSalary", String.format("%.2f", maxSalary));
                stats.put("totalSalary", String.format("%.2f", totalSalary));
                
                // Department averages
                Map<String, Double> deptAverages = new HashMap<>();
                Map<String, List<Employee>> employeesByDept = employees.stream()
                    .collect(Collectors.groupingBy(Employee::getDepartment));
                
                employeesByDept.forEach((dept, deptEmployees) -> {
                    double deptAvg = deptEmployees.stream()
                        .filter(e -> e.getSalary() != null)
                        .mapToDouble(Employee::getSalary)
                        .average()
                        .orElse(0.0);
                    deptAverages.put(dept, deptAvg);
                });
                
                stats.put("departmentAverages", deptAverages);
            }
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch salary statistics: " + e.getMessage()));
        }
    }

    @GetMapping("/stats/department")
    public ResponseEntity<?> getDepartmentStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            List<Employee> employees = employeeService.getAllEmployeesForExport();
            
            List<String> departments = employeeService.getDistinctDepartments();
            stats.put("totalDepartments", departments.size());
            
            Map<String, Long> employeeCountByDepartment = employees.stream()
                .filter(e -> e.getDepartment() != null)
                .collect(Collectors.groupingBy(
                    Employee::getDepartment,
                    Collectors.counting()
                ));
            stats.put("employeeCountByDepartment", employeeCountByDepartment);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch department statistics: " + e.getMessage()));
        }
    }

    // ========== FIXED: Bulk operations with correct endpoints ==========

    @PostMapping("/bulk/delete")
    public ResponseEntity<Map<String, Object>> deleteEmployeesBulk(@RequestBody Map<String, Object> request) {
        try {
            List<Long> employeeIds = (List<Long>) request.get("employeeIds");
            if (employeeIds == null || employeeIds.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "No employee IDs provided");
                return ResponseEntity.badRequest().body(error);
            }
            
            int deleted = employeeService.deleteEmployeesBulk(employeeIds);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Successfully deleted " + deleted + " employees");
            response.put("count", deleted);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("success", false);
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/bulk/update-department")
    public ResponseEntity<Map<String, Object>> updateDepartmentBulk(@RequestBody Map<String, Object> request) {
        try {
            List<Long> employeeIds = (List<Long>) request.get("employeeIds");
            String department = (String) request.get("department");
            
            if (employeeIds == null || employeeIds.isEmpty() || department == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Invalid request data");
                error.put("success", false);
                return ResponseEntity.badRequest().body(error);
            }
            
            int updated = employeeService.updateDepartmentBulk(employeeIds, department);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Updated department for " + updated + " employees");
            response.put("count", updated);
            response.put("department", department);
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("success", false);
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ========== FIXED: Import CSV endpoint format ==========

    @PostMapping("/import/csv")
    public ResponseEntity<Map<String, Object>> importEmployeesFromCSV(@RequestParam("file") MultipartFile file) {
        try {
            List<Employee> employees = employeeService.importFromCSV(file);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("imported", employees.size());
            response.put("failed", 0);
            response.put("total", employees.size());
            response.put("message", "Successfully imported " + employees.size() + " employees");
            response.put("employees", employees.stream()
                .map(employeeService::convertToDTO)
                .collect(Collectors.toList()));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", e.getMessage());
            error.put("details", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // ========== EXISTING ENDPOINTS (with minor fixes) ==========

    @GetMapping
    public ResponseEntity<?> getEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) Double minSalary,
            @RequestParam(required = false) Double maxSalary) {
        
        try {
            Page<Employee> employees = employeeService.getAllEmployees(page, size, search, department, position, minSalary, maxSalary);
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", employees.getContent());
            response.put("currentPage", employees.getNumber());
            response.put("totalElements", employees.getTotalElements());
            response.put("totalPages", employees.getTotalPages());
            response.put("pageSize", employees.getSize());
            response.put("hasNext", employees.hasNext());
            response.put("hasPrevious", employees.hasPrevious());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch employees: " + e.getMessage()));
        }
    }

    @PostMapping("/bulk")
    public ResponseEntity<Map<String, Object>> createEmployeesBulk(@RequestBody List<EmployeeDTO> employees) {
        try {
            List<Employee> created = employeeService.createEmployeesBulk(employees);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Successfully created " + created.size() + " employees");
            response.put("count", created.size());
            response.put("employees", created.stream()
                .map(employeeService::convertToDTO)
                .collect(Collectors.toList()));
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("success", false);
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEmployee(@PathVariable Long id) {
        try {
            Employee employee = employeeService.getEmployeeById(id);
            if (employee == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Employee not found with id: " + id));
            }
            return ResponseEntity.ok(employee);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch employee: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createEmployee(@RequestBody Map<String, Object> requestData) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("=== CREATE EMPLOYEE REQUEST ===");
        
        try {
            // Extract values (keeping your existing logic)
            String firstName = extractStringValue(requestData, "firstName");
            String lastName = extractStringValue(requestData, "lastName");
            String email = extractStringValue(requestData, "email");
            String phoneNumber = extractStringValue(requestData, "phoneNumber");
            String countryCode = extractStringValue(requestData, "countryCode", "+1");
            
            String department = extractDepartmentPosition(requestData.get("department"), "department");
            String position = extractDepartmentPosition(requestData.get("position"), "position");
            
            Double salary = extractSalary(requestData.get("salary"));
            
            // Validation
            List<String> validationErrors = new ArrayList<>();
            if (firstName == null || firstName.trim().isEmpty()) validationErrors.add("First name is required");
            if (lastName == null || lastName.trim().isEmpty()) validationErrors.add("Last name is required");
            if (email == null || email.trim().isEmpty()) validationErrors.add("Email is required");
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) validationErrors.add("Phone number is required");
            if (department == null || department.trim().isEmpty()) validationErrors.add("Department is required");
            if (position == null || position.trim().isEmpty()) validationErrors.add("Position is required");
            if (salary == null || salary <= 0) validationErrors.add("Salary must be positive");
            
            if (!validationErrors.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "VALIDATION_ERROR",
                    "message", "Validation failed",
                    "errors", validationErrors
                ));
            }
            
            if (employeeService.emailExists(email)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "message", "Email already exists: " + email
                ));
            }
            
            // Create employee
            Employee employee = new Employee();
            employee.setFirstName(firstName.trim());
            employee.setLastName(lastName.trim());
            employee.setEmail(email.trim().toLowerCase());
            
            // Format phone number
            String finalPhone = phoneNumber.trim();
            if (!finalPhone.startsWith("+")) {
                finalPhone = countryCode + finalPhone;
            }
            employee.setPhoneNumber(finalPhone);
            employee.setCountryCode(countryCode);
            employee.setDepartment(department.trim());
            employee.setPosition(position.trim());
            employee.setSalary(salary);
            
            Employee savedEmployee = employeeService.saveEmployee(employee);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "status", "SUCCESS",
                "message", "Employee created successfully",
                "employeeId", savedEmployee.getId(),
                "employee", Map.of(
                    "id", savedEmployee.getId(),
                    "firstName", savedEmployee.getFirstName(),
                    "lastName", savedEmployee.getLastName(),
                    "email", savedEmployee.getEmail(),
                    "department", savedEmployee.getDepartment(),
                    "position", savedEmployee.getPosition(),
                    "salary", savedEmployee.getSalary()
                )
            ));
            
        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "status", "ERROR",
                    "message", "Failed to create employee: " + e.getMessage()
                ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmployee(@PathVariable Long id, @RequestBody Map<String, Object> requestData) {
        try {
            Employee existingEmployee = employeeService.getEmployeeById(id);
            if (existingEmployee == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Employee not found with id: " + id));
            }
            
            // Extract values
            String firstName = extractStringValue(requestData, "firstName", existingEmployee.getFirstName());
            String lastName = extractStringValue(requestData, "lastName", existingEmployee.getLastName());
            String email = extractStringValue(requestData, "email", existingEmployee.getEmail());
            String phoneNumber = extractStringValue(requestData, "phoneNumber", existingEmployee.getPhoneNumber());
            String countryCode = extractStringValue(requestData, "countryCode", existingEmployee.getCountryCode());
            
            String department = extractDepartmentPosition(requestData.get("department"), "department");
            if (department == null) department = existingEmployee.getDepartment();
            
            String position = extractDepartmentPosition(requestData.get("position"), "position");
            if (position == null) position = existingEmployee.getPosition();
            
            Double salary = extractSalary(requestData.get("salary"));
            if (salary == null) salary = existingEmployee.getSalary();
            
            // Validation
            List<String> validationErrors = new ArrayList<>();
            if (firstName == null || firstName.trim().isEmpty()) validationErrors.add("First name is required");
            if (lastName == null || lastName.trim().isEmpty()) validationErrors.add("Last name is required");
            if (email == null || email.trim().isEmpty()) validationErrors.add("Email is required");
            if (department == null || department.trim().isEmpty()) validationErrors.add("Department is required");
            if (position == null || position.trim().isEmpty()) validationErrors.add("Position is required");
            if (salary == null || salary <= 0) validationErrors.add("Salary must be positive");
            
            if (!validationErrors.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "status", "VALIDATION_ERROR",
                        "message", "Validation failed",
                        "errors", validationErrors
                    ));
            }
            
            // Check email uniqueness
            if (!existingEmployee.getEmail().equals(email) && employeeService.emailExists(email)) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "status", "ERROR",
                        "message", "Email already exists: " + email
                    ));
            }
            
            // Update employee
            existingEmployee.setFirstName(firstName.trim());
            existingEmployee.setLastName(lastName.trim());
            existingEmployee.setEmail(email.trim().toLowerCase());
            
            String finalPhone = phoneNumber.trim();
            if (!finalPhone.startsWith("+")) {
                finalPhone = countryCode + finalPhone;
            }
            existingEmployee.setPhoneNumber(finalPhone);
            existingEmployee.setCountryCode(countryCode);
            existingEmployee.setDepartment(department.trim());
            existingEmployee.setPosition(position.trim());
            existingEmployee.setSalary(salary);
            
            Employee updatedEmployee = employeeService.saveEmployee(existingEmployee);
            
            return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Employee updated successfully",
                "employee", employeeService.convertToDTO(updatedEmployee)
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to update employee: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        try {
            Employee existingEmployee = employeeService.getEmployeeById(id);
            if (existingEmployee == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Employee not found with id: " + id));
            }

            employeeService.deleteEmployee(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Employee deleted successfully");
            response.put("deletedId", id);
            response.put("success", true);
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to delete employee: " + e.getMessage()));
        }
    }

    @GetMapping("/departments")
    public ResponseEntity<?> getDepartments() {
        try {
            List<String> departments = employeeService.getDistinctDepartments();
            return ResponseEntity.ok(departments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch departments: " + e.getMessage()));
        }
    }

    @GetMapping("/positions")
    public ResponseEntity<?> getPositions() {
        try {
            List<String> positions = employeeService.getDistinctPositions();
            return ResponseEntity.ok(positions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch positions: " + e.getMessage()));
        }
    }

    @GetMapping("/department/{department}")
    public ResponseEntity<?> getEmployeesByDepartment(
            @PathVariable String department,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Page<Employee> employees = employeeService.getEmployeesByDepartment(department, page, size);
            
            Map<String, Object> response = new HashMap<>();
            response.put("content", employees.getContent());
            response.put("currentPage", employees.getNumber());
            response.put("totalItems", employees.getTotalElements());
            response.put("totalPages", employees.getTotalPages());
            response.put("department", department);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch employees by department: " + e.getMessage()));
        }
    }

    @GetMapping("/stats/summary")
    public ResponseEntity<?> getEmployeeStats() {
        try {
            Map<String, Object> stats = employeeService.getEmployeeStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch employee statistics: " + e.getMessage()));
        }
    }

    // ========== HELPER METHODS ==========
    
    private String extractStringValue(Map<String, Object> data, String key) {
        return extractStringValue(data, key, null);
    }
    
    private String extractStringValue(Map<String, Object> data, String key, String defaultValue) {
        Object value = data.get(key);
        if (value == null) {
            return defaultValue;
        }
        return value.toString().trim();
    }
    
    private String extractDepartmentPosition(Object obj, String fieldName) {
        if (obj == null) {
            return null;
        }
        
        if (obj instanceof String) {
            return ((String) obj).trim();
        }
        
        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            String[] possibleKeys = {"value", "name", "label", "id", "key"};
            
            for (String key : possibleKeys) {
                Object value = map.get(key);
                if (value != null) {
                    return value.toString().trim();
                }
            }
            
            for (Object value : map.values()) {
                if (value != null) {
                    return value.toString().trim();
                }
            }
        }
        
        return obj.toString().trim();
    }
    
    private Double extractSalary(Object salaryObj) {
        if (salaryObj == null) {
            return null;
        }
        
        try {
            if (salaryObj instanceof Number) {
                return ((Number) salaryObj).doubleValue();
            } else if (salaryObj instanceof String) {
                return Double.parseDouble((String) salaryObj);
            }
        } catch (Exception e) {
            System.out.println("Error parsing salary: " + e.getMessage());
        }
        return null;
    }
    
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        errorResponse.put("timestamp", System.currentTimeMillis());
        errorResponse.put("success", false);
        return errorResponse;
    }
    @GetMapping("/all")
public ResponseEntity<?> getAllEmployeesForExport() {
    try {
        List<Employee> employees = employeeService.getAllEmployeesForExport();
        List<EmployeeDTO> employeeDTOs = employees.stream()
            .map(employeeService::convertToDTO)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(employeeDTOs);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Failed to fetch all employees: " + e.getMessage()));
    }
}
    @PostMapping("/debug-form")
    public ResponseEntity<?> debugFormData(@RequestBody Map<String, Object> formData) {
        return ResponseEntity.ok(Map.of(
            "message", "Form data received",
            "data", formData
        ));
    }
} 