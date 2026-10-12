package com.example.demo.service;

import com.example.demo.model.Application;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Writer;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for streaming and exporting applicant datasets into RFC 4180 compliant CSV files.
 * Includes security defenses against CSV Formula Injection (Excel Macro Injection).
 */
@Service
public class CsvExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Streams candidate applications into a CSV file via the provided Writer.
     */
    public void writeApplicationsToCsv(List<Application> applications, Writer writer) throws IOException {
        // Write CSV Header
        writer.write("Application ID,Candidate Name,Email,Mobile,Education,Skills,Job Title,Company,Status,Applied Date,Resume File,Cover Note,Interview Date,Interview Link\n");

        if (applications == null || applications.isEmpty()) {
            writer.flush();
            return;
        }

        for (Application app : applications) {
            String appId = String.valueOf(app.getId());
            String candidateName = (app.getCandidate() != null) ? app.getCandidate().getName() : "";
            String email = (app.getCandidate() != null && app.getCandidate().getUser() != null) ? app.getCandidate().getUser().getEmail() : "";
            String mobile = (app.getCandidate() != null) ? app.getCandidate().getMobile() : "";
            String education = (app.getCandidate() != null) ? app.getCandidate().getEducation() : "";
            String skills = (app.getCandidate() != null) ? app.getCandidate().getSkills() : "";
            String jobTitle = (app.getJobPost() != null) ? app.getJobPost().getTitle() : "";
            String company = (app.getJobPost() != null && app.getJobPost().getHr() != null) ? app.getJobPost().getHr().getCompanyName() : "";
            String status = (app.getStatus() != null) ? app.getStatus().name() : "";
            String appliedDate = (app.getApplyDate() != null) ? app.getApplyDate().format(DATE_FORMATTER) : "";
            String resumePath = (app.getResumePath() != null) ? app.getResumePath() : "";
            String coverNote = (app.getCoverNote() != null) ? app.getCoverNote() : "";

            String interviewDate = "";
            String interviewLink = "";
            if (app.getInterview() != null) {
                interviewDate = (app.getInterview().getInterviewDate() != null) ? app.getInterview().getInterviewDate().format(DATE_FORMATTER) : "";
                interviewLink = (app.getInterview().getModeOrLink() != null) ? app.getInterview().getModeOrLink() : "";
            }

            // Write row with RFC 4180 escaping
            writer.write(String.join(",",
                    sanitizeCsvField(appId),
                    sanitizeCsvField(candidateName),
                    sanitizeCsvField(email),
                    sanitizeCsvField(mobile),
                    sanitizeCsvField(education),
                    sanitizeCsvField(skills),
                    sanitizeCsvField(jobTitle),
                    sanitizeCsvField(company),
                    sanitizeCsvField(status),
                    sanitizeCsvField(appliedDate),
                    sanitizeCsvField(resumePath),
                    sanitizeCsvField(coverNote),
                    sanitizeCsvField(interviewDate),
                    sanitizeCsvField(interviewLink)
            ));
            writer.write("\n");
        }

        writer.flush();
    }

    /**
     * Sanitizes CSV field values following RFC 4180 and protects against CSV Formula Injection.
     */
    private String sanitizeCsvField(String field) {
        if (field == null || field.isBlank()) {
            return "\"\"";
        }

        String sanitized = field.trim();

        // Security Defense: Neutralize CSV Formula Injection (=, +, -, @)
        char firstChar = sanitized.charAt(0);
        if (firstChar == '=' || firstChar == '+' || firstChar == '-' || firstChar == '@') {
            sanitized = "'" + sanitized;
        }

        // Escape internal double quotes by doubling them (" -> "")
        sanitized = sanitized.replace("\"", "\"\"");

        // Wrap within quotes
        return "\"" + sanitized + "\"";
    }
}
