package com.ems.controller;

import com.ems.entity.Employee;
import com.ems.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = {
    "http://localhost:4200",
    "https://employee-management-system-xi-henna.vercel.app",
    "https://employee-management-system-jxdj.onrender.com"
})
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

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
            response.put("totalItems", employees.getTotalElements());
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
        System.out.println("Full Request Data: " + requestData);
        
        try {
            System.out.println("\n=== FIELD ANALYSIS ===");
            requestData.forEach((key, value) -> {
                System.out.printf("%-15s = %-40s (Type: %s)%n", 
                    key, 
                    value != null ? value.toString() : "null",
                    value != null ? value.getClass().getSimpleName() : "null"
                );
            });
            
            String firstName = extractStringValue(requestData, "firstName");
            String lastName = extractStringValue(requestData, "lastName");
            String email = extractStringValue(requestData, "email");
            String phoneNumber = extractStringValue(requestData, "phoneNumber");
            String countryCode = extractStringValue(requestData, "countryCode", "+1");
            
            // CRITICAL: Handle department and position
            String department = extractDepartmentPosition(requestData.get("department"), "department");
            String position = extractDepartmentPosition(requestData.get("position"), "position");
            
            Double salary = extractSalary(requestData.get("salary"));
            
            System.out.println("\n=== EXTRACTED VALUES ===");
            System.out.println("First Name: '" + firstName + "'");
            System.out.println("Last Name: '" + lastName + "'");
            System.out.println("Email: '" + email + "'");
            System.out.println("Phone: '" + phoneNumber + "'");
            System.out.println("Country Code: '" + countryCode + "'");
            System.out.println("Department: '" + department + "'");
            System.out.println("Position: '" + position + "'");
            System.out.println("Salary: " + salary);
            
            List<String> validationErrors = new ArrayList<>();
            
            if (firstName == null || firstName.trim().isEmpty()) {
                validationErrors.add("First name is required");
            }
            if (lastName == null || lastName.trim().isEmpty()) {
                validationErrors.add("Last name is required");
            }
            if (email == null || email.trim().isEmpty()) {
                validationErrors.add("Email is required");
            } else if (!isValidEmail(email)) {
                validationErrors.add("Email should be valid");
            }
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                validationErrors.add("Phone number is required");
            }
            if (department == null || department.trim().isEmpty()) {
                validationErrors.add("Department is required");
            }
            if (position == null || position.trim().isEmpty()) {
                validationErrors.add("Position is required");
            }
            if (salary == null || salary <= 0) {
                validationErrors.add("Salary must be positive");
            }
            
            if (!validationErrors.isEmpty()) {
                System.out.println("❌ Validation errors: " + validationErrors);
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "VALIDATION_ERROR",
                    "message", "Validation failed",
                    "errors", validationErrors,
                    "receivedData", requestData
                ));
            }
            
            if (employeeService.emailExists(email)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "message", "Email already exists: " + email
                ));
            }
            
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
            
            // Set department and position
            employee.setDepartment(department.trim());
            employee.setPosition(position.trim());
            employee.setSalary(salary);
            
            System.out.println("✅ Employee object created successfully");
            System.out.println("Department set to: " + employee.getDepartment());
            System.out.println("Position set to: " + employee.getPosition());
            
            Employee savedEmployee = employeeService.saveEmployee(employee);
            
            System.out.println("\n✅ EMPLOYEE SAVED SUCCESSFULLY");
            System.out.println("ID: " + savedEmployee.getId());
            System.out.println("Name: " + savedEmployee.getFirstName() + " " + savedEmployee.getLastName());
            System.out.println("Email: " + savedEmployee.getEmail());
            System.out.println("Department: " + savedEmployee.getDepartment());
            System.out.println("Position: " + savedEmployee.getPosition());
            System.out.println("=".repeat(60) + "\n");
            
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
                    "message", "Failed to create employee: " + e.getMessage(),
                    "errorDetails", e.toString()
                ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmployee(@PathVariable Long id, @RequestBody Map<String, Object> requestData) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("=== UPDATE EMPLOYEE REQUEST ===");
        System.out.println("Employee ID: " + id);
        System.out.println("Request Data: " + requestData);
        
        try {
            // Check if employee exists
            Employee existingEmployee = employeeService.getEmployeeById(id);
            if (existingEmployee == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Employee not found with id: " + id));
            }
            
            String firstName = extractStringValue(requestData, "firstName", existingEmployee.getFirstName());
            String lastName = extractStringValue(requestData, "lastName", existingEmployee.getLastName());
            String email = extractStringValue(requestData, "email", existingEmployee.getEmail());
            String phoneNumber = extractStringValue(requestData, "phoneNumber", existingEmployee.getPhoneNumber());
            String countryCode = extractStringValue(requestData, "countryCode", existingEmployee.getCountryCode());
            
            // Handle department and position
            String department = extractDepartmentPosition(requestData.get("department"), "department");
            if (department == null) {
                department = existingEmployee.getDepartment();
            }
            
            String position = extractDepartmentPosition(requestData.get("position"), "position");
            if (position == null) {
                position = existingEmployee.getPosition();
            }
            
            Double salary = extractSalary(requestData.get("salary"));
            if (salary == null) {
                salary = existingEmployee.getSalary();
            }
            
            System.out.println("\n=== EXTRACTED VALUES ===");
            System.out.println("Department: '" + department + "'");
            System.out.println("Position: '" + position + "'");
            
            List<String> validationErrors = new ArrayList<>();
            
            if (firstName == null || firstName.trim().isEmpty()) {
                validationErrors.add("First name is required");
            }
            if (lastName == null || lastName.trim().isEmpty()) {
                validationErrors.add("Last name is required");
            }
            if (email == null || email.trim().isEmpty()) {
                validationErrors.add("Email is required");
            } else if (!isValidEmail(email)) {
                validationErrors.add("Email should be valid");
            }
            if (department == null || department.trim().isEmpty()) {
                validationErrors.add("Department is required");
            }
            if (position == null || position.trim().isEmpty()) {
                validationErrors.add("Position is required");
            }
            if (salary == null || salary <= 0) {
                validationErrors.add("Salary must be positive");
            }
            
            if (!validationErrors.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "status", "VALIDATION_ERROR",
                        "message", "Validation failed",
                        "errors", validationErrors
                    ));
            }
            
            // Check if email is being changed and already exists
            if (!existingEmployee.getEmail().equals(email) && employeeService.emailExists(email)) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "status", "ERROR",
                        "message", "Email already exists: " + email
                    ));
            }
            
            existingEmployee.setFirstName(firstName.trim());
            existingEmployee.setLastName(lastName.trim());
            existingEmployee.setEmail(email.trim().toLowerCase());
            
            // Format phone number
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
            
            System.out.println("✅ Employee updated successfully");
            
            return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Employee updated successfully",
                "employee", Map.of(
                    "id", updatedEmployee.getId(),
                    "firstName", updatedEmployee.getFirstName(),
                    "lastName", updatedEmployee.getLastName(),
                    "email", updatedEmployee.getEmail(),
                    "department", updatedEmployee.getDepartment(),
                    "position", updatedEmployee.getPosition()
                )
            ));
            
        } catch (Exception e) {
            System.out.println("❌ Error updating employee: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to update employee: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        try {
            // Check if employee exists
            Employee existingEmployee = employeeService.getEmployeeById(id);
            if (existingEmployee == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Employee not found with id: " + id));
            }

            employeeService.deleteEmployee(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Employee deleted successfully");
            response.put("deletedId", id.toString());
            
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
    
    // CRITICAL: Extract department/position from object or string
    private String extractDepartmentPosition(Object obj, String fieldName) {
        if (obj == null) {
            System.out.println(fieldName + " is null");
            return null;
        }
        
        System.out.println("Processing " + fieldName + ": " + obj + " (Type: " + obj.getClass().getSimpleName() + ")");
        
        if (obj instanceof String) {
            String str = (String) obj;
            System.out.println(fieldName + " is String: '" + str + "'");
            return str;
        }
        
        // If it's a Map (like {value: "IT", label: "IT Department"})
        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            System.out.println(fieldName + " is Map: " + map);
            
            String[] possibleKeys = {"value", "name", "label", "id", "key"};
            
            for (String key : possibleKeys) {
                Object value = map.get(key);
                if (value != null) {
                    String result = value.toString().trim();
                    System.out.println("Found " + fieldName + " with key '" + key + "': '" + result + "'");
                    return result;
                }
            }
            
            // If no common keys found, try first non-null value
            for (Object value : map.values()) {
                if (value != null) {
                    String result = value.toString().trim();
                    System.out.println("Using first non-null value for " + fieldName + ": '" + result + "'");
                    return result;
                }
            }
        }
        
        String result = obj.toString().trim();
        System.out.println("Fallback for " + fieldName + ": '" + result + "'");
        return result;
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

    // Helper method to create error responses
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        errorResponse.put("timestamp", System.currentTimeMillis());
        return errorResponse;
    }
    
    @PostMapping("/debug-form")
    public ResponseEntity<?> debugFormData(@RequestBody Map<String, Object> formData) {
        System.out.println("\n=== DEBUG FORM DATA ===");
        
        // Log everything
        formData.forEach((key, value) -> {
            System.out.printf("%-15s = %-40s (Type: %s)%n", 
                key, 
                value != null ? value.toString() : "null",
                value != null ? value.getClass().getSimpleName() : "null"
            );
        });
        
        // Specifically check department and position
        Object dept = formData.get("department");
        Object pos = formData.get("position");
        
        System.out.println("\n=== DEPARTMENT ANALYSIS ===");
        System.out.println("Value: " + dept);
        System.out.println("Type: " + (dept != null ? dept.getClass().getName() : "null"));
        
        System.out.println("\n=== POSITION ANALYSIS ===");
        System.out.println("Value: " + pos);
        System.out.println("Type: " + (pos != null ? pos.getClass().getName() : "null"));
        
        return ResponseEntity.ok(Map.of(
            "message", "Form data received",
            "department", Map.of(
                "value", dept,
                "type", dept != null ? dept.getClass().getName() : "null"
            ),
            "position", Map.of(
                "value", pos,
                "type", pos != null ? pos.getClass().getName() : "null"
            ),
            "allData", formData
        ));
    }
}
