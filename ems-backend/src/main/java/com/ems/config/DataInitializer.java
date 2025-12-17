package com.ems.config;

import com.ems.entity.Employee;
import com.ems.entity.User;
import com.ems.repository.EmployeeRepository;
import com.ems.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeUsers();
        initializeEmployees();
    }

    private void initializeUsers() {
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@ems.com");
            admin.setRole(User.Role.ADMIN);
            userRepository.save(admin);

            User user = new User();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setEmail("user@ems.com");
            user.setRole(User.Role.USER);
            userRepository.save(user);

            System.out.println("Default users created:");
            System.out.println("Admin - username: admin, password: admin123");
            System.out.println("User - username: user, password: user123");
        }
    }

    private void initializeEmployees() {
        if (employeeRepository.count() == 0) {
            List<Employee> employees = Arrays.asList(
                // USA/Canada numbers
                new Employee("John", "Doe", "john.doe@company.com", "1234567890", "+1", "IT", "Software Engineer", 75000.0),
                new Employee("Jane", "Smith", "jane.smith@company.com", "2345678901", "+1", "HR", "HR Manager", 65000.0),
                new Employee("Mike", "Johnson", "mike.johnson@company.com", "3456789012", "+1", "Finance", "Financial Analyst", 70000.0),
                
                // UK numbers
                new Employee("Sarah", "Wilson", "sarah.wilson@company.com", "7123456789", "+44", "Marketing", "Marketing Specialist", 60000.0),
                new Employee("David", "Brown", "david.brown@company.com", "7456789012", "+44", "IT", "System Administrator", 68000.0),
                
                // India numbers
                new Employee("Emily", "Davis", "emily.davis@company.com", "9876543210", "+91", "Sales", "Sales Manager", 72000.0),
                
                // Australia numbers
                new Employee("Robert", "Miller", "robert.miller@company.com", "412345678", "+61", "Operations", "Operations Manager", 80000.0),
                
                // Germany numbers
                new Employee("Lisa", "Taylor", "lisa.taylor@company.com", "15123456789", "+49", "IT", "Frontend Developer", 70000.0),
                
                // Additional international employees
                new Employee("Carlos", "Rodriguez", "carlos.rodriguez@company.com", "5511999999999", "+55", "IT", "Backend Developer", 68000.0),
                new Employee("Yuki", "Tanaka", "yuki.tanaka@company.com", "9012345678", "+81", "Design", "UI/UX Designer", 62000.0),
                new Employee("Wei", "Zhang", "wei.zhang@company.com", "13123456789", "+86", "Finance", "Accountant", 58000.0),
                new Employee("Marco", "Rossi", "marco.rossi@company.com", "3123456789", "+39", "Sales", "Sales Executive", 67000.0),
                new Employee("Sophie", "Martin", "sophie.martin@company.com", "612345678", "+33", "HR", "Recruiter", 59000.0)
            );

            employeeRepository.saveAll(employees);
            System.out.println("Sample employees created with international phone numbers");
            System.out.println("Total employees: " + employees.size());
            
            // Display sample phone numbers for testing
            System.out.println("\nSample Phone Numbers by Country:");
            System.out.println("USA/Canada: +11234567890");
            System.out.println("UK: +447123456789");
            System.out.println("India: +919876543210");
            System.out.println("Australia: +61412345678");
            System.out.println("Germany: +4915123456789");
            System.out.println("Brazil: +5511999999999");
            System.out.println("Japan: +819012345678");
            System.out.println("China: +8613123456789");
            System.out.println("Italy: +393123456789");
            System.out.println("France: +33612345678");
        }
    }
}