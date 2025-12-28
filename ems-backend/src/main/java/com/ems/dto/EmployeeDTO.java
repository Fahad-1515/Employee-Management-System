package com.ems.dto;

public class EmployeeDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private String position;
    private Integer vacationDays;    // Changed from int to Integer
    private Integer sickDays;        // Changed from int to Integer
    private Integer personalDays;    // Changed from int to Integer
    private Integer usedVacation;    // Changed from int to Integer
    private Integer usedSick;        // Changed from int to Integer
    private Integer usedPersonal;    // Changed from int to Integer
    
    // Constructors
    public EmployeeDTO() {}
    
    public EmployeeDTO(Long id, String firstName, String lastName, String email, 
                      String department, String position) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.department = department;
        this.position = position;
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
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    
    public Integer getVacationDays() { return vacationDays; }
    public void setVacationDays(Integer vacationDays) { this.vacationDays = vacationDays; }
    
    public Integer getSickDays() { return sickDays; }
    public void setSickDays(Integer sickDays) { this.sickDays = sickDays; }
    
    public Integer getPersonalDays() { return personalDays; }
    public void setPersonalDays(Integer personalDays) { this.personalDays = personalDays; }
    
    public Integer getUsedVacation() { return usedVacation; }
    public void setUsedVacation(Integer usedVacation) { this.usedVacation = usedVacation; }
    
    public Integer getUsedSick() { return usedSick; }
    public void setUsedSick(Integer usedSick) { this.usedSick = usedSick; }
    
    public Integer getUsedPersonal() { return usedPersonal; }
    public void setUsedPersonal(Integer usedPersonal) { this.usedPersonal = usedPersonal; }
}