// src/main/java/com/ems/controller/TestController.java
package com.ems.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = {
     "http://localhost:4200",
    "https://employee-management-system-ruddy-nine.vercel.app",
    "https://employee-management-system-jxdj.onrender.com"
})
public class TestController {
    
    @GetMapping("/hello")
    public String hello() {
        return "Backend is working! API endpoints are available.";
    }
    
    @GetMapping("/auth-test")
    public String authTest() {
        return "Auth endpoint would be here";
    }
}
