package com.ems.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Column(nullable = false)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Column(nullable = false)
    private String lastName;

    @Email(message = "Email should be valid")
    @Column(unique = true, nullable = false)
    private String email;

    // Updated phone validation for country codes
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+[1-9]\\d{0,3}[0-9]{8,15}$", message = "Phone number should be valid with country code (e.g., +1234567890)")
    @Column(name = "phone_number")
    private String phoneNumber;

    // Add country code field
    @NotBlank(message = "Country code is required")
    @Column(name = "country_code")
    private String countryCode;

    @NotBlank(message = "Department is required")
    private String department;

    @NotBlank(message = "Position is required")
    private String position;

    @DecimalMin(value = "0.0", message = "Salary must be positive")
    private Double salary;

    private LocalDateTime hireDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (hireDate == null) {
            hireDate = LocalDateTime.now();
        }
        // Auto-generate full phone number if not provided with country code
        if (this.phoneNumber != null && !this.phoneNumber.startsWith("+")) {
            this.phoneNumber = this.countryCode + this.phoneNumber;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public Employee() {}

    // Updated constructor with country code
    public Employee(String firstName, String lastName, String email, String phoneNumber, 
                   String countryCode, String department, String position, Double salary) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.countryCode = countryCode;
        this.department = department;
        this.position = position;
        this.salary = salary;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public Double getSalary() { return salary; }
    public void setSalary(Double salary) { this.salary = salary; }

    public LocalDateTime getHireDate() { return hireDate; }
    public void setHireDate(LocalDateTime hireDate) { this.hireDate = hireDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Helper method to get formatted phone number
    public String getFormattedPhoneNumber() {
        if (phoneNumber == null) return "";
        // Format as +1 (234) 567-8900 if possible
        if (phoneNumber.matches("^\\+1\\d{10}$")) {
            return String.format("+1 (%s) %s-%s", 
                phoneNumber.substring(2, 5),
                phoneNumber.substring(5, 8),
                phoneNumber.substring(8));
        }
        return phoneNumber;
    }
}