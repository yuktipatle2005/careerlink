package com.example.demo.service;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Service managing asynchronous HTML email notifications via JavaMailSender and Thymeleaf.
 * Operates in non-blocking background threads via @Async.
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final String fromEmail;
    private final String fromName;
    private final boolean mailEnabled;

    public EmailService(JavaMailSender mailSender,
                        TemplateEngine templateEngine,
                        @Value("${app.mail.from:no-reply@careerlink.com}") String fromEmail,
                        @Value("${app.mail.from-name:CareerLink Hiring Team}") String fromName,
                        @Value("${app.mail.enabled:true}") boolean mailEnabled) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
        this.mailEnabled = mailEnabled;
    }

    /**
     * Sends an application submission confirmation email asynchronously.
     */
    @Async("emailTaskExecutor")
    public void sendApplicationSubmittedEmail(String toEmail, String candidateName, String jobTitle, String companyName) {
        Context context = new Context();
        context.setVariable("candidateName", candidateName);
        context.setVariable("jobTitle", jobTitle);
        context.setVariable("companyName", companyName);

        sendHtmlEmail(toEmail, "Application Confirmed: " + jobTitle + " at " + companyName, "application-submitted", context);
    }

    /**
     * Sends an interview invitation email asynchronously.
     */
    @Async("emailTaskExecutor")
    public void sendInterviewScheduledEmail(String toEmail, String candidateName, String jobTitle,
                                             String companyName, String formattedDateTime, String meetingLink) {
        Context context = new Context();
        context.setVariable("candidateName", candidateName);
        context.setVariable("jobTitle", jobTitle);
        context.setVariable("companyName", companyName);
        context.setVariable("formattedDateTime", formattedDateTime);
        context.setVariable("meetingLink", meetingLink);

        sendHtmlEmail(toEmail, "📅 Interview Invitation: " + jobTitle + " at " + companyName, "interview-invitation", context);
    }

    /**
     * Sends an application status transition update email asynchronously.
     */
    @Async("emailTaskExecutor")
    public void sendApplicationStatusUpdateEmail(String toEmail, String candidateName, String jobTitle,
                                                 String companyName, String status) {
        Context context = new Context();
        context.setVariable("candidateName", candidateName);
        context.setVariable("jobTitle", jobTitle);
        context.setVariable("companyName", companyName);
        context.setVariable("status", status);

        sendHtmlEmail(toEmail, "Application Update: " + jobTitle + " (" + status + ")", "status-update", context);
    }

    /**
     * Internal helper to render Thymeleaf HTML template and dispatch MimeMessage.
     */
    private void sendHtmlEmail(String to, String subject, String templateName, Context context) {
        if (!mailEnabled || to == null || to.isBlank()) {
            logger.info("[Mock Mail Mode] Target: {} | Subject: {} | Template: email/{}", to, subject, templateName);
            return;
        }

        try {
            String htmlContent = templateEngine.process("email/" + templateName, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(new InternetAddress(fromEmail, fromName));
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Successfully dispatched HTML email to {} [Subject: '{}']", to, subject);
        } catch (Exception ex) {
            // Log warning without failing the user's HTTP request
            logger.warn("Could not dispatch email to {} [{}]. Check SMTP configuration: {}", to, subject, ex.getMessage());
        }
    }
}
