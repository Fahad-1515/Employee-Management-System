package com.ems.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        System.out.println("=== BUILDING SECURITY FILTER CHAIN ===");
        
        // Configure HTTP Security
        http
            // 1. Configure CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 2. Disable CSRF for APIs
            .csrf(csrf -> csrf.disable())
            
            // 3. Use stateless sessions
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 4. Configure request authorization
            .authorizeHttpRequests(auth -> auth
                
                // Preflight requests
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // ========== PUBLIC ENDPOINTS ==========
                
                // Auth endpoints
                .requestMatchers("/api/auth/**").permitAll()
                
                // Test endpoints
                .requestMatchers("/api/test/**").permitAll()
                
                // H2 Console (for development)
                .requestMatchers("/h2-console/**").permitAll()
                
                // Error endpoint
                .requestMatchers("/error").permitAll()
                
                // Employee dropdown endpoints (public for frontend)
                .requestMatchers(HttpMethod.GET, "/api/employees/departments").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/employees/positions").permitAll()
                
                // Employee stats endpoint (public for dashboard)
                .requestMatchers(HttpMethod.GET, "/api/employees/stats/summary").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/employees/stats/salary").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/employees/stats/department").permitAll()
                
                // Export endpoints (public for now)
                .requestMatchers(HttpMethod.GET, "/api/export/**").permitAll()
                
                // ========== PROTECTED ENDPOINTS (REQUIRE AUTH) ==========
                
                // Admin only endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Employee management endpoints (require auth)
                .requestMatchers(HttpMethod.POST, "/api/employees").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/employees/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/employees/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/employees/bulk/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/employees/bulk/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/employees/bulk/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/employees/import/**").hasRole("ADMIN")
                
                // Leave management endpoints
                .requestMatchers("/api/leave/**").authenticated()
                
                // Allow GET /employees for authenticated users
                .requestMatchers(HttpMethod.GET, "/api/employees/**").authenticated()
                
                // ========== FALLBACK ==========
                .anyRequest().authenticated()  // Protect everything else by default
            )
            
            // 5. Add headers for H2 console (development only)
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            
            // 6. Add JWT authentication filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        System.out.println("=== SECURITY CONFIGURATION COMPLETE ===");
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        System.out.println("=== CREATING CORS CONFIGURATION ===");
        
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow specific origins
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:4200",
            "https://employee-management-system-wheat-chi.vercel.app",
            "https://employee-management-system-jxdj.onrender.com"
        ));
        
        // Allow all HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"
        ));
        
        // Allow all headers
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers",
            "Cache-Control"
        ));
        
        // Expose headers to client
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials"
        ));
        
        // Allow credentials
        configuration.setAllowCredentials(true);
        
        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);
        
        System.out.println("✅ CORS Configuration Created");
        System.out.println("- Allowed Origins: " + configuration.getAllowedOrigins());
        System.out.println("- Allow Credentials: " + configuration.getAllowCredentials());
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
