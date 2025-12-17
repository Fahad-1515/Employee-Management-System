package com.ems.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain chain)
            throws ServletException, IOException {
        
        System.out.println("\n=== JWT FILTER START ===");
        System.out.println("Request Method: " + request.getMethod());
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Request URL: " + request.getRequestURL());
        System.out.println("Origin Header: " + request.getHeader("Origin"));
        System.out.println("Content-Type: " + request.getHeader("Content-Type"));
        
        // SKIP JWT check for OPTIONS requests (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            System.out.println("⏩ Skipping JWT filter for OPTIONS preflight request");
            chain.doFilter(request, response);
            return;
        }
        
        final String authorizationHeader = request.getHeader("Authorization");
        System.out.println("Authorization Header: " + 
            (authorizationHeader != null ? 
                (authorizationHeader.length() > 30 ? authorizationHeader.substring(0, 30) + "..." : authorizationHeader) 
                : "null"));

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            username = jwtUtil.extractUsername(jwt);
            System.out.println("Extracted username from JWT: " + username);
            System.out.println("JWT length: " + jwt.length());
        } else {
            System.out.println("No Bearer token found in Authorization header");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            System.out.println("Attempting to load user details for: " + username);
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                System.out.println("UserDetails loaded successfully");
                
                if (jwtUtil.validateToken(jwt)) {
                    System.out.println("✅ JWT token is valid");
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("✅ Authentication set in SecurityContext for: " + username);
                } else {
                    System.out.println("❌ JWT token validation failed");
                }
            } catch (Exception e) {
                System.out.println("❌ Error loading user details: " + e.getMessage());
                e.printStackTrace();
            }
        } else if (username == null) {
            System.out.println("⚠️  No username extracted from JWT, proceeding without authentication");
        } else {
            System.out.println("⚠️  Authentication already exists in SecurityContext");
        }
        
        System.out.println("=== JWT FILTER END ===\n");
        chain.doFilter(request, response);
    }
}