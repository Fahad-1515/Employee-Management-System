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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Service
public class ExportService {

    @Autowired
    private EmployeeRepository employeeRepository;

    public ByteArrayInputStream exportEmployeesToCSV() {
        List<Employee> employees = employeeRepository.findAll(Sort.by("firstName").ascending());
        
        // Alternative approach without withHeader
        final CSVFormat format = CSVFormat.DEFAULT;

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), format)) {
            
            // Manually write header
            csvPrinter.printRecord("ID", "First Name", "Last Name", "Email", "Phone", 
                                 "Department", "Position", "Salary", "Created Date");
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
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
                        employee.getCreatedAt().format(formatter) : "N/A"
                );
                csvPrinter.printRecord(data);
            }
            
            csvPrinter.flush();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to export CSV: " + e.getMessage());
        }
    }

    public ByteArrayInputStream exportEmployeesToExcel() {
        List<Employee> employees = employeeRepository.findAll(Sort.by("firstName").ascending());
        
        try (Workbook workbook = new XSSFWorkbook(); 
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Employees");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "First Name", "Last Name", "Email", "Phone", 
                        "Department", "Position", "Salary", "Created Date"};
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Create data rows
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
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
                row.createCell(7).setCellValue(employee.getSalary());
                if (employee.getCreatedAt() != null) {
                    row.createCell(8).setCellValue(employee.getCreatedAt().format(formatter));
                } else {
                    row.createCell(8).setCellValue("N/A");
                }
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to export Excel: " + e.getMessage());
        }
    }
}