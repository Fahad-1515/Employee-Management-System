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
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        System.out.println("=== BUILDING SECURITY FILTER CHAIN ===");
        System.out.println("✅ Making /departments and /positions endpoints PUBLIC");
        
        // Configure HTTP Security
        http
            // 1. Configure CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 2. Disable CSRF for APIs
            .csrf(csrf -> csrf.disable())
            
            // 3. Use stateless sessions
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 4. Configure request authorization - CRITICAL FIX
            .authorizeHttpRequests(auth -> auth
                // ========== PUBLIC ENDPOINTS (NO AUTH REQUIRED) ==========
                
                // Allow all OPTIONS requests (CORS preflight)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // Auth endpoints
                .requestMatchers("/api/auth/**").permitAll()
                
                // Test endpoints
                .requestMatchers("/api/test/**").permitAll()
                
                // H2 Console (for development)
                .requestMatchers("/h2-console/**").permitAll()
                
                // Error endpoint
                .requestMatchers("/error").permitAll()
                
                // ✅ CRITICAL FIX: Make dropdown endpoints PUBLIC
                .requestMatchers(HttpMethod.GET, "/api/employees/departments").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/employees/positions").permitAll()
                
                // ✅ Make stats endpoint public for dashboard
                .requestMatchers(HttpMethod.GET, "/api/employees/stats/summary").permitAll()
                
                // ✅ Allow GET /employees for initial loading (optional, can remove if you want it protected)
                .requestMatchers(HttpMethod.GET, "/api/employees").permitAll()
                
                // ========== PROTECTED ENDPOINTS (REQUIRE AUTH) ==========
                
                // Admin only endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Employee management endpoints (require auth)
                .requestMatchers(HttpMethod.POST, "/api/employees/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/employees/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/employees/**").authenticated()
                
                // Allow authenticated access to other employee endpoints
                .requestMatchers("/api/employees/**").authenticated()
                
                // ========== FALLBACK ==========
                .anyRequest().authenticated()  // Protect everything else by default
            )
            
            // 5. Add headers for H2 console (development only)
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            
            // 6. Add JWT authentication filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        System.out.println("=== SECURITY CONFIGURATION COMPLETE ===");
        System.out.println("Public endpoints:");
        System.out.println("  • /api/auth/**");
        System.out.println("  • /api/test/**");
        System.out.println("  • GET /api/employees/departments ✅");
        System.out.println("  • GET /api/employees/positions ✅");
        System.out.println("  • GET /api/employees/stats/summary ✅");
        System.out.println("  • GET /api/employees ✅");
        
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        System.out.println("=== CREATING CORS CONFIGURATION ===");
        
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow specific origins
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:4200",
            "http://127.0.0.1:4200",
            "https://employee-management-system-xi-henna.vercel.app",
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
        
        System.out.println("CORS Configuration:");
        System.out.println("- Allowed Origins: " + configuration.getAllowedOrigins());
        System.out.println("- Allow Credentials: " + configuration.getAllowCredentials());
        System.out.println("- Allowed Methods: " + configuration.getAllowedMethods());
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    // Alternative: Use this for development with wildcard origins
    @Bean
    public CorsFilter corsFilter() {
        System.out.println("=== CREATING CORS FILTER (DEVELOPMENT) ===");
        
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");  // Wildcard for development
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
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
