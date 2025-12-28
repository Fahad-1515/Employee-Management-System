package com.ems.service;

import com.ems.entity.LeaveRequest;
import com.ems.entity.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class NotificationService {
    
    @Autowired(required = false)
    private JavaMailSender mailSender;
    
    @Autowired(required = false)
    private TemplateEngine templateEngine;
    
    @Value("${spring.mail.username:no-reply@ems.com}")
    private String fromEmail;
    
    @Value("${app.notifications.enabled:true}")
    private boolean notificationsEnabled;
    
    // Simple version for console logging
    public void sendLeaveRequestNotification(LeaveRequest leaveRequest) {
        if (!notificationsEnabled) return;
        
        String message = String.format(
            "New leave request from %s %s: %s leave from %s to %s",
            leaveRequest.getEmployee().getFirstName(),
            leaveRequest.getEmployee().getLastName(),
            leaveRequest.getLeaveType(),
            leaveRequest.getStartDate(),
            leaveRequest.getEndDate()
        );
        
        System.out.println("📧 Leave Request Notification: " + message);
        
        // Try to send email if mailSender is available
        sendSimpleEmail(
            "manager@company.com", // In real app, get manager's email
            "New Leave Request Requires Approval",
            message
        );
    }
    
    public void sendLeaveApprovalNotification(LeaveRequest leaveRequest) {
        if (!notificationsEnabled) return;
        
        String message = String.format(
            "Your leave request has been APPROVED: %s leave from %s to %s",
            leaveRequest.getLeaveType(),
            leaveRequest.getStartDate(),
            leaveRequest.getEndDate()
        );
        
        System.out.println("✅ Leave Approval Notification: " + message);
        
        // Send email to employee
        if (leaveRequest.getEmployee().getEmail() != null) {
            sendSimpleEmail(
                leaveRequest.getEmployee().getEmail(),
                "Leave Request Approved",
                message
            );
        }
    }
    
    public void sendLeaveRejectionNotification(LeaveRequest leaveRequest) {
        if (!notificationsEnabled) return;
        
        String reason = leaveRequest.getApprovalComments() != null ? 
            leaveRequest.getApprovalComments() : "No reason provided";
        
        String message = String.format(
            "Your leave request has been REJECTED: %s leave from %s to %s. Reason: %s",
            leaveRequest.getLeaveType(),
            leaveRequest.getStartDate(),
            leaveRequest.getEndDate(),
            reason
        );
        
        System.out.println("❌ Leave Rejection Notification: " + message);
        
        // Send email to employee
        if (leaveRequest.getEmployee().getEmail() != null) {
            sendSimpleEmail(
                leaveRequest.getEmployee().getEmail(),
                "Leave Request Rejected",
                message
            );
        }
    }
    
    // Simple email sending method
    private void sendSimpleEmail(String to, String subject, String body) {
        if (mailSender == null) {
            System.out.println("⚠️  MailSender not configured. Email not sent to: " + to);
            return;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);
            
            mailSender.send(message);
            System.out.println("✅ Email sent successfully to: " + to);
        } catch (MessagingException e) {
            System.err.println("❌ Failed to send email to " + to + ": " + e.getMessage());
        }
    }
    
    // Additional notification methods
    public void sendPasswordResetEmail(String email, String resetToken) {
        String subject = "Password Reset Request";
        String body = String.format(
            "You requested a password reset. Use this token: %s\n" +
            "If you didn't request this, please ignore this email.",
            resetToken
        );
        
        sendSimpleEmail(email, subject, body);
    }
    
    public void sendWelcomeEmail(Employee employee, String temporaryPassword) {
        String subject = "Welcome to Employee Management System";
        String body = String.format(
            "Welcome %s %s!\n\n" +
            "Your account has been created.\n" +
            "Username: %s\n" +
            "Temporary Password: %s\n\n" +
            "Please change your password after first login.",
            employee.getFirstName(),
            employee.getLastName(),
            employee.getEmail(),
            temporaryPassword
        );
        
        sendSimpleEmail(employee.getEmail(), subject, body);
    }
    
    // Template-based email (if template engine is available)
    public void sendLeaveTemplateEmail(LeaveRequest leaveRequest, String templateName) {
        if (templateEngine == null || mailSender == null) {
            sendSimpleEmail(
                leaveRequest.getEmployee().getEmail(),
                "Leave Status Update",
                "Your leave request status has been updated."
            );
            return;
        }
        
        try {
            Context context = new Context();
            context.setVariable("employee", leaveRequest.getEmployee());
            context.setVariable("leave", leaveRequest);
            context.setVariable("company", "Employee Management System");
            
            String htmlContent = templateEngine.process(templateName, context);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(leaveRequest.getEmployee().getEmail());
            helper.setSubject("Leave Request Update");
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            System.out.println("✅ Template email sent successfully");
        } catch (Exception e) {
            System.err.println("❌ Failed to send template email: " + e.getMessage());
            // Fallback to simple email
            sendSimpleEmail(
                leaveRequest.getEmployee().getEmail(),
                "Leave Status Update",
                "Your leave request status has been updated."
            );
        }
    }
}