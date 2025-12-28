package com.ems.service;

import com.ems.entity.Employee;
import com.ems.repository.EmployeeRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Service
public class ExportService {

    private final EmployeeRepository employeeRepository;
    
    // Using constructor injection (recommended)
    @Autowired
    public ExportService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }
    
    // Constants for file names
    private static final String CSV_EXTENSION = ".csv";
    private static final String EXCEL_EXTENSION = ".xlsx";
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional(readOnly = true)
    public ByteArrayInputStream exportEmployeesToCSV() {
        List<Employee> employees = employeeRepository.findAll(Sort.by("firstName").ascending());
        
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), CSVFormat.DEFAULT)) {
            
            // Write header
            csvPrinter.printRecord("ID", "First Name", "Last Name", "Email", "Phone", 
                                 "Department", "Position", "Salary", "Created Date");
            
            // Write data rows
            for (Employee employee : employees) {
                List<String> data = Arrays.asList(
                    String.valueOf(employee.getId()),
                    employee.getFirstName(),
                    employee.getLastName(),
                    employee.getEmail(),
                    employee.getPhoneNumber(),
                    employee.getDepartment(),
                    employee.getPosition(),
                    String.valueOf(employee.getSalary()),
                    employee.getCreatedAt() != null ? 
                        employee.getCreatedAt().format(DATE_FORMATTER) : "N/A"
                );
                csvPrinter.printRecord(data);
            }
            
            csvPrinter.flush();
            return new ByteArrayInputStream(out.toByteArray());
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to export CSV: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public ByteArrayInputStream exportEmployeesToExcel() {
        List<Employee> employees = employeeRepository.findAll(Sort.by("firstName").ascending());
        
        try (Workbook workbook = new XSSFWorkbook(); 
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Employees");
            
            // Create header row with style
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "First Name", "Last Name", "Email", "Phone", 
                              "Department", "Position", "Salary", "Created Date"};
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Create data rows
            int rowNum = 1;
            for (Employee employee : employees) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(employee.getId());
                row.createCell(1).setCellValue(employee.getFirstName());
                row.createCell(2).setCellValue(employee.getLastName());
                row.createCell(3).setCellValue(employee.getEmail());
                row.createCell(4).setCellValue(employee.getPhoneNumber());
                row.createCell(5).setCellValue(employee.getDepartment());
                row.createCell(6).setCellValue(employee.getPosition());
                
                // Format salary cell as number
                Cell salaryCell = row.createCell(7);
                salaryCell.setCellValue(employee.getSalary());
                salaryCell.setCellStyle(createNumberStyle(workbook));
                
                // Date cell
                Cell dateCell = row.createCell(8);
                if (employee.getCreatedAt() != null) {
                    dateCell.setCellValue(employee.getCreatedAt().format(DATE_FORMATTER));
                } else {
                    dateCell.setCellValue("N/A");
                }
            }
            
            // Auto-size columns for better readability
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to export Excel: " + e.getMessage(), e);
        }
    }
    
    // Helper method for header style
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
    
    // Helper method for number formatting
    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        return style;
    }
    
    // Utility method to generate filename
    public String generateCsvFilename() {
        return "employees_" + System.currentTimeMillis() + CSV_EXTENSION;
    }
    
    public String generateExcelFilename() {
        return "employees_" + System.currentTimeMillis() + EXCEL_EXTENSION;
    }
}