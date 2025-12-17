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
@CrossOrigin(origins = "http://localhost:4200")
public class ExportController {

    @Autowired
    private ExportService exportService;

    @GetMapping("/employees/csv")
    public ResponseEntity<Resource> exportEmployeesToCSV() {
        String filename = "employees_" + System.currentTimeMillis() + ".csv";
        InputStreamResource file = new InputStreamResource(exportService.exportEmployeesToCSV());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/csv"))
                .body(file);
    }

    @GetMapping("/employees/excel")
    public ResponseEntity<Resource> exportEmployeesToExcel() {
        String filename = "employees_" + System.currentTimeMillis() + ".xlsx";
        InputStreamResource file = new InputStreamResource(exportService.exportEmployeesToExcel());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }
}