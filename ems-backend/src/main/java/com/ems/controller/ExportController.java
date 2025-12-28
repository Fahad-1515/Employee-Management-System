package com.ems.controller;

import com.ems.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/export")
@CrossOrigin(origins = {
    "http://localhost:4200",
    "https://employee-management-system-six-silk.vercel.app",
    "https://employee-management-system-jxdj.onrender.com"
})
public class ExportController {

    @Autowired
    private ExportService exportService;

    @GetMapping("/employees/csv")
    public ResponseEntity<Resource> exportEmployeesToCSV() {
        try {
            String filename = "employees_" + System.currentTimeMillis() + ".csv";
            InputStreamResource file = new InputStreamResource(exportService.exportEmployeesToCSV());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .contentLength(file.contentLength())
                    .body(file);
        } catch (Exception e) {
            // Return error response
            throw new RuntimeException("Failed to export CSV: " + e.getMessage(), e);
        }
    }

    @GetMapping("/employees/excel")
    public ResponseEntity<Resource> exportEmployeesToExcel() {
        try {
            String filename = "employees_" + System.currentTimeMillis() + ".xlsx";
            InputStreamResource file = new InputStreamResource(exportService.exportEmployeesToExcel());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(file);
        } catch (Exception e) {
            throw new RuntimeException("Failed to export Excel: " + e.getMessage(), e);
        }
    }

    // ========== ADDITIONAL EXPORT OPTIONS ==========
    
    @GetMapping("/employees/json")
    public ResponseEntity<Resource> exportEmployeesToJson() {
        try {
            // You can add JSON export later if needed
            String filename = "employees_" + System.currentTimeMillis() + ".json";
            // Implementation for JSON export would go here
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to export JSON: " + e.getMessage(), e);
        }
    }

    // ========== BULK EXPORT BY DEPARTMENT ==========
    
    @GetMapping("/department/{department}")
    public ResponseEntity<Resource> exportDepartmentEmployees(
            @PathVariable String department,
            @RequestParam(defaultValue = "csv") String format) {
        
        try {
            String filename = "employees_" + department + "_" + System.currentTimeMillis() + 
                             (format.equalsIgnoreCase("excel") ? ".xlsx" : ".csv");
            
            // You would need to implement department-specific export in ExportService
            InputStreamResource file;
            if (format.equalsIgnoreCase("excel")) {
                file = new InputStreamResource(exportService.exportEmployeesToExcel()); // Modify to filter by department
            } else {
                file = new InputStreamResource(exportService.exportEmployeesToCSV()); // Modify to filter by department
            }
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(getMediaType(format))
                    .body(file);
        } catch (Exception e) {
            throw new RuntimeException("Failed to export department employees: " + e.getMessage(), e);
        }
    }

    // ========== HELPER METHODS ==========
    
    private MediaType getMediaType(String format) {
        switch (format.toLowerCase()) {
            case "excel":
                return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case "csv":
                return MediaType.parseMediaType("text/csv");
            case "json":
                return MediaType.APPLICATION_JSON;
            default:
                return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}