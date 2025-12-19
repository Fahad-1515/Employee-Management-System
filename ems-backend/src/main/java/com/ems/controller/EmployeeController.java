package com.ems.controller;

import com.ems.entity.Employee;
import com.ems.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = {
     "http://localhost:4200",
    "https://employee-management-system-ruddy-nine.vercel.app",
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
            
            // Create a more detailed response
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
    public ResponseEntity<?> createEmployee(@Valid @RequestBody Employee employee, BindingResult result) {
        try {
            // Check for validation errors
            if (result.hasErrors()) {
                List<String> errors = result.getFieldErrors()
                        .stream()
                        .map(error -> error.getField() + ": " + error.getDefaultMessage())
                        .collect(Collectors.toList());
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Validation failed", errors));
            }

            // Check if email already exists
            if (employeeService.emailExists(employee.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Email already exists: " + employee.getEmail()));
            }

            Employee savedEmployee = employeeService.saveEmployee(employee);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Employee created successfully");
            response.put("employee", savedEmployee);
            response.put("employeeId", savedEmployee.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to create employee: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmployee(@PathVariable Long id, @Valid @RequestBody Employee employee, BindingResult result) {
        try {
            // Check for validation errors
            if (result.hasErrors()) {
                List<String> errors = result.getFieldErrors()
                        .stream()
                        .map(error -> error.getField() + ": " + error.getDefaultMessage())
                        .collect(Collectors.toList());
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Validation failed", errors));
            }

            // Check if employee exists
            Employee existingEmployee = employeeService.getEmployeeById(id);
            if (existingEmployee == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Employee not found with id: " + id));
            }

            // Check if email is being changed and already exists for another employee
            if (!existingEmployee.getEmail().equals(employee.getEmail()) && 
                employeeService.emailExists(employee.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Email already exists: " + employee.getEmail()));
            }

            employee.setId(id);
            Employee updatedEmployee = employeeService.saveEmployee(employee);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Employee updated successfully");
            response.put("employee", updatedEmployee);
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
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

    // Helper method to create error responses
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        errorResponse.put("timestamp", System.currentTimeMillis());
        return errorResponse;
    }

    private Map<String, Object> createErrorResponse(String message, List<String> details) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        errorResponse.put("details", details);
        errorResponse.put("timestamp", System.currentTimeMillis());
        return errorResponse;
    }
}
