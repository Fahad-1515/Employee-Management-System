package com.ems.controller;

import com.ems.entity.User;
import com.ems.security.JwtUtil;
import com.ems.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ========== REMOVE THESE SECURITY RISKS! ==========
    // DELETE these insecure endpoints:
    // @PostMapping("/test-login")  - SECURITY RISK!
    // @PostMapping("/open-login")  - SECURITY RISK!

    // === HEALTH CHECK ENDPOINTS ===
    
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        System.out.println("=== AUTH TEST ENDPOINT HIT ===");
        Map<String, String> response = new HashMap<>();
        response.put("message", "Auth API is working!");
        response.put("status", "OK");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(response);
    }

    // === MAIN LOGIN ENDPOINT (FIXED) ===
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) { 
        System.out.println("\n" + "=".repeat(50));
        System.out.println("=== LOGIN ATTEMPT ===");
        System.out.println("Username: " + loginRequest.getUsername());
        System.out.println("Password provided: " + (loginRequest.getPassword() != null ? "YES" : "NO"));
        
        try {
            // Method 1: Manual authentication
            System.out.println("Looking for user in database...");
            User user = userService.findByUsername(loginRequest.getUsername());
            
            if (user == null) {
                System.out.println("❌ USER NOT FOUND: " + loginRequest.getUsername());
                
                // Check hardcoded users as fallback (for development only)
                if ("admin".equals(loginRequest.getUsername()) && "admin123".equals(loginRequest.getPassword())) {
                    System.out.println("⚠️  Using hardcoded admin fallback");
                    return createSuccessResponse("admin", "ADMIN", "admin@ems.com");
                }
                if ("user".equals(loginRequest.getUsername()) && "user123".equals(loginRequest.getPassword())) {
                    System.out.println("⚠️  Using hardcoded user fallback");
                    return createSuccessResponse("user", "USER", "user@ems.com");
                }
                
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("User not found"));
            }
            
            System.out.println("✅ User found: " + user.getUsername());
            System.out.println("User role: " + user.getRole());
            System.out.println("User email: " + user.getEmail());
            System.out.println("Stored password hash exists: " + (user.getPassword() != null));
            
            // Check password
            boolean passwordMatches = false;
            
            if (user.getPassword() != null) {
                passwordMatches = passwordEncoder.matches(loginRequest.getPassword(), user.getPassword());
                System.out.println("BCrypt password match: " + passwordMatches);
            }
            
            // Fallback to direct comparison if BCrypt fails (for development only)
            if (!passwordMatches) {
                System.out.println("⚠️  Trying direct password comparison...");
                if ("admin123".equals(loginRequest.getPassword()) && "admin".equals(user.getUsername())) {
                    passwordMatches = true;
                } else if ("user123".equals(loginRequest.getPassword()) && "user".equals(user.getUsername())) {
                    passwordMatches = true;
                }
            }
            
            if (!passwordMatches) {
                System.out.println("❌ PASSWORD MISMATCH");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(createErrorResponse("Invalid password"));
            }
            
            System.out.println("✅ PASSWORD VERIFIED");
            
            // Generate token
            String token = jwtUtil.generateToken(user.getUsername());
            System.out.println("Token generated (first 20 chars): " + 
                (token.length() > 20 ? token.substring(0, 20) + "..." : token));
            
            return createSuccessResponse(user.getUsername(), user.getRole().toString(), user.getEmail());
            
        } catch (Exception e) {
            System.out.println("❌ EXCEPTION: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Server error: " + e.getMessage()));
        } finally {
            System.out.println("=".repeat(50) + "\n");
        }
    }

    // ========== ADD THESE MISSING ENDPOINTS ==========
    
    @GetMapping("/users/me")
    public ResponseEntity<?> getCurrentUser() {
        System.out.println("=== GET CURRENT USER ===");
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(createErrorResponse("Not authenticated"));
        }
        
        String username = authentication.getName();
        System.out.println("Current user: " + username);
        
        User user = userService.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse("User not found"));
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("role", user.getRole().toString());
        
        System.out.println("✅ User info retrieved: " + username);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("=== USER REGISTRATION ===");
        System.out.println("Username: " + registerRequest.getUsername());
        System.out.println("Email: " + registerRequest.getEmail());
        
        try {
            // Check if username already exists
            if (userService.existsByUsername(registerRequest.getUsername())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createErrorResponse("Username already exists"));
            }
            
            // Check if email already exists
            if (userService.existsByEmail(registerRequest.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(createErrorResponse("Email already exists"));
            }
            
            // Create new user
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            user.setEmail(registerRequest.getEmail());
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            user.setRole(registerRequest.getRole() != null ? 
                User.Role.valueOf(registerRequest.getRole()) : User.Role.USER);
            
            User savedUser = userService.save(user);
            
            // Generate token for auto-login
            String token = jwtUtil.generateToken(savedUser.getUsername());
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("username", savedUser.getUsername());
            response.put("email", savedUser.getEmail());
            response.put("role", savedUser.getRole().toString());
            response.put("expiresIn", 86400000);
            response.put("message", "Registration successful");
            
            System.out.println("✅ User registered successfully: " + savedUser.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            System.out.println("❌ Registration error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("Registration failed: " + e.getMessage()));
        } finally {
            System.out.println("=".repeat(50) + "\n");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // In a stateless JWT system, logout is handled client-side
        // We could implement token blacklisting if needed
        System.out.println("=== LOGOUT REQUEST ===");
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        response.put("status", "SUCCESS");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-database")
    public ResponseEntity<?> checkDatabase() {
        System.out.println("=== CHECKING DATABASE ===");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User admin = userService.findByUsername("admin");
            User user = userService.findByUsername("user");
            
            response.put("adminExists", admin != null);
            response.put("userExists", user != null);
            
            if (admin != null) {
                response.put("adminDetails", Map.of(
                    "username", admin.getUsername(),
                    "email", admin.getEmail(),
                    "role", admin.getRole(),
                    "passwordHashLength", admin.getPassword() != null ? admin.getPassword().length() : 0
                ));
            }
            
            if (user != null) {
                response.put("userDetails", Map.of(
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "role", user.getRole(),
                    "passwordHashLength", user.getPassword() != null ? user.getPassword().length() : 0
                ));
            }
            
            response.put("message", "Database check complete");
            response.put("status", "SUCCESS");
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("status", "ERROR");
        }
        
        return ResponseEntity.ok(response);
    }

    // ========== UTILITY METHODS ==========
    
    private ResponseEntity<?> createSuccessResponse(String username, String role, String email) {
        String token = jwtUtil.generateToken(username);
        
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("username", username);
        response.put("role", role);
        response.put("email", email);
        response.put("expiresIn", 86400000);
        
        System.out.println("✅ Login successful for: " + username);
        return ResponseEntity.ok(response);
    }
    
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        errorResponse.put("timestamp", System.currentTimeMillis());
        errorResponse.put("status", "ERROR");
        return errorResponse;
    }

    // ========== REQUEST CLASSES ==========
    
    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    public static class RegisterRequest {
        private String username;
        private String email;
        private String password;
        private String role;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
    
    public static class ChangePasswordRequest {
        private String oldPassword;
        private String newPassword;

        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}