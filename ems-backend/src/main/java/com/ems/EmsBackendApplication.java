package com.ems;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class EmsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmsBackendApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("✅ EMPLOYEE MANAGEMENT SYSTEM STARTED SUCCESSFULLY!");
        System.out.println("=".repeat(60));
        System.out.println("\n📊 Application Information:");
        System.out.println("- Local URL: http://localhost:8080");
        System.out.println("- API Base: http://localhost:8080/api");
        System.out.println("- H2 Console: http://localhost:8080/h2-console");
        System.out.println("- Database: H2 (in-memory)");
        System.out.println("- JDBC URL: jdbc:h2:mem:emsdb");
        System.out.println("- Username: emsuser");
        System.out.println("- Password: emspass");
        System.out.println("\n🔑 Default Login Credentials:");
        System.out.println("- Admin: admin / admin123");
        System.out.println("- User: user / user123");
        System.out.println("\n" + "=".repeat(60) + "\n");
    }
}