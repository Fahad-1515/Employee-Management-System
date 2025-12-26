package com.ems.service;

import com.ems.entity.Employee;
import com.ems.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CSVImportService {
    
    private final EmployeeRepository employeeRepository;
    
    public List<Employee> importEmployeesFromCSV(MultipartFile file) throws Exception {
        List<Employee> employees = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, 
                 CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {
            
            for (CSVRecord record : csvParser) {
                Employee employee = new Employee();
                
                // Map CSV columns to Employee fields
                employee.setFirstName(record.get("firstname"));
                employee.setLastName(record.get("lastname"));
                employee.setEmail(record.get("email").toLowerCase());
                employee.setDepartment(record.get("department"));
                employee.setPosition(record.get("position"));
                
                // Parse salary
                try {
                    employee.setSalary(Double.parseDouble(record.get("salary")));
                } catch (NumberFormatException e) {
                    employee.setSalary(0.0);
                }
                
                // Set default values
                employee.setPhoneNumber(record.isMapped("phone") ? record.get("phone") : "");
                employee.setCountryCode("+1");
                employee.setVacationDays(20);
                employee.setSickDays(10);
                employee.setPersonalDays(5);
                employee.setUsedVacation(0);
                employee.setUsedSick(0);
                employee.setUsedPersonal(0);
                
                // Validate required fields
                if (employee.getFirstName() == null || employee.getFirstName().isEmpty() ||
                    employee.getLastName() == null || employee.getLastName().isEmpty() ||
                    employee.getEmail() == null || employee.getEmail().isEmpty()) {
                    continue; // Skip invalid rows
                }
                
                employees.add(employee);
            }
        }
        
        // Save all employees
        return employeeRepository.saveAll(employees);
    }
}
