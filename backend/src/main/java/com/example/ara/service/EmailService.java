package com.example.ara.service;

import com.example.ara.model.Assessment;
import com.example.ara.model.ScopeResult;
import com.example.ara.model.ScopeResult.ScopeItem;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${ara.mail.results-recipient}")
    private String resultsRecipient;

    @Value("${ara.mail.from}")
    private String fromAddress;

    @Value("${ara.frontend.base-url}")
    private String frontendBaseUrl;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendScopeResultEmail(Assessment assessment, ScopeResult result) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(resultsRecipient);
            helper.setSubject(String.format("[ARA] New Assessment Scope — %s (%s Risk)",
                    assessment.getAssetName(), result.getRiskRating()));
            helper.setText(buildScopeHtml(assessment, result), true);
            mailSender.send(message);
        } catch (MessagingException e) {
            // Log but don't fail the user's request — email is best-effort
        }
    }

    @Async
    public void sendPasswordResetEmail(String toEmail, String resetToken, String fullName) {
        try {
            String resetLink = frontendBaseUrl + "/pages/reset-password.html?token=" + resetToken;
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("[ARA] Password Reset Request");
            helper.setText(buildResetHtml(fullName, resetLink), true);
            mailSender.send(message);
        } catch (MessagingException e) {
            // Silent — user sees generic success message regardless
        }
    }

    // ── Email Templates ───────────────────────────────────────────

    private String buildScopeHtml(Assessment a, ScopeResult r) {
        List<ScopeItem> mandatory = r.getScopeItems().stream().filter(ScopeItem::isMandatory).toList();
        List<ScopeItem> optional  = r.getScopeItems().stream().filter(i -> !i.isMandatory()).toList();

        return "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'>" +
               "<meta name='viewport' content='width=device-width,initial-scale=1'></head>" +
               "<body style='margin:0;padding:0;background:#0f172a;font-family:Segoe UI,Arial,sans-serif;color:#f1f5f9'>" +
               "<table width='100%' cellpadding='0' cellspacing='0' style='background:#0f172a;padding:32px 0'>" +
               "<tr><td align='center'>" +
               "<table width='620' cellpadding='0' cellspacing='0' style='background:#1e293b;border-radius:12px;overflow:hidden;border:1px solid #334155'>" +

               // Header
               "<tr><td style='background:#1e40af;padding:24px 32px'>" +
               "<table width='100%' cellpadding='0' cellspacing='0'><tr>" +
               "<td><span style='background:#3b82f6;color:#fff;font-size:18px;font-weight:800;padding:5px 12px;border-radius:6px'>ARA</span>" +
               "<span style='color:#bfdbfe;font-size:13px;margin-left:12px;vertical-align:middle'>Asset Risk Assurance</span></td>" +
               "<td align='right'><span style='color:#bfdbfe;font-size:11px'>Assessment Scope Report</span></td>" +
               "</tr></table></td></tr>" +

               // Risk banner
               "<tr><td style='background:" + riskColour(r.getRiskRating()) + ";padding:14px 32px;text-align:center'>" +
               "<span style='color:#fff;font-size:20px;font-weight:700;letter-spacing:1px'>OVERALL RISK: " + r.getRiskRating() + "</span>" +
               "</td></tr>" +

               // Asset details
               "<tr><td style='padding:28px 32px 0'>" +
               "<h2 style='margin:0 0 14px;font-size:17px;color:#f1f5f9'>Asset Details</h2>" +
               "<table width='100%' cellpadding='0' cellspacing='0' style='font-size:14px'>" +
               detailRow("Asset Name",      a.getAssetName()) +
               detailRow("Asset Owner",     a.getAssetOwner()) +
               detailRow("Department",      a.getDepartment()) +
               detailRow("Asset Type",      a.getAssetType().name().replace("_", " ")) +
               detailRow("Criticality",     a.getCriticality().name()) +
               detailRow("Network Exposure",a.getNetworkExposure().name().replace("_", " ")) +
               detailRow("Submitted By",    a.getSubmittedByEmail() != null ? a.getSubmittedByEmail() : "—") +
               detailRow("Submitted At",    a.getCreatedAt().format(FORMATTER)) +
               "</table></td></tr>" +

               // Summary
               "<tr><td style='padding:20px 32px 0'>" +
               "<div style='background:#0f172a;border-left:4px solid #3b82f6;padding:14px 16px;border-radius:0 8px 8px 0;font-size:14px;color:#cbd5e1;line-height:1.6'>" +
               r.getSummary() + "</div></td></tr>" +

               // Mandatory items
               buildSectionHtml("Mandatory Test Areas (" + mandatory.size() + ")", mandatory) +

               // Optional items
               buildSectionHtml("Recommended Test Areas (" + optional.size() + ")", optional) +

               // Footer
               "<tr><td style='padding:24px 32px;border-top:1px solid #334155'>" +
               "<p style='margin:0;font-size:12px;color:#475569;text-align:center'>" +
               "Automatically generated by the ARA Scope Determination Tool.<br>" +
               "Assessment ID: #" + a.getId() + " &nbsp;|&nbsp; Generated: " + r.getGeneratedAt().format(FORMATTER) +
               "</p></td></tr>" +

               "</table></td></tr></table></body></html>";
    }

    private String buildSectionHtml(String title, List<ScopeItem> items) {
        if (items.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("<tr><td style='padding:22px 32px 0'>")
          .append("<h2 style='margin:0 0 10px;font-size:15px;color:#f1f5f9'>").append(title).append("</h2>");
        for (ScopeItem item : items) {
            sb.append("<div style='background:#0f172a;border-radius:8px;padding:13px 16px;margin-bottom:8px;border-left:4px solid ")
              .append(riskColour(item.getPriority())).append("'>")
              .append("<table width='100%' cellpadding='0' cellspacing='0'><tr>")
              .append("<td style='font-weight:600;font-size:13px;color:#f1f5f9'>").append(item.getTestArea())
              .append(item.isMandatory() ? " <span style='background:#4f46e5;color:#fff;font-size:10px;padding:1px 8px;border-radius:99px;margin-left:4px'>MANDATORY</span>" : "")
              .append("</td>")
              .append("<td align='right'><span style='background:").append(riskColour(item.getPriority()))
              .append(";color:#fff;font-size:10px;font-weight:700;padding:2px 10px;border-radius:99px;text-transform:uppercase'>")
              .append(item.getPriority()).append("</span></td></tr>")
              .append("<tr><td colspan='2' style='padding-top:5px;font-size:12px;color:#94a3b8'>")
              .append(item.getRationale()).append("</td></tr>")
              .append("</table></div>");
        }
        sb.append("</td></tr>");
        return sb.toString();
    }

    private String detailRow(String label, String value) {
        return "<tr><td style='padding:7px 0;color:#94a3b8;width:180px'>" + label + "</td>" +
               "<td style='padding:7px 0;font-weight:500'>" + value + "</td></tr>";
    }

    private String riskColour(String level) {
        return switch (level) {
            case "CRITICAL" -> "#dc2626";
            case "HIGH"     -> "#ea580c";
            case "MEDIUM"   -> "#d97706";
            default         -> "#16a34a";
        };
    }

    private String buildResetHtml(String fullName, String resetLink) {
        return "<!DOCTYPE html><html lang='en'><head><meta charset='UTF-8'></head>" +
               "<body style='margin:0;padding:0;background:#0f172a;font-family:Segoe UI,Arial,sans-serif;color:#f1f5f9'>" +
               "<table width='100%' cellpadding='0' cellspacing='0' style='background:#0f172a;padding:40px 0'><tr><td align='center'>" +
               "<table width='520' cellpadding='0' cellspacing='0' style='background:#1e293b;border-radius:12px;overflow:hidden;border:1px solid #334155'>" +
               "<tr><td style='background:#1e40af;padding:24px 32px'>" +
               "<span style='background:#3b82f6;color:#fff;font-size:18px;font-weight:800;padding:5px 12px;border-radius:6px'>ARA</span>" +
               "<span style='color:#bfdbfe;font-size:13px;margin-left:12px;vertical-align:middle'>Asset Risk Assurance</span>" +
               "</td></tr>" +
               "<tr><td style='padding:32px'>" +
               "<h2 style='margin:0 0 16px;font-size:18px'>Password Reset Request</h2>" +
               "<p style='color:#94a3b8;font-size:14px;line-height:1.7'>Hi " + fullName + ",</p>" +
               "<p style='color:#94a3b8;font-size:14px;line-height:1.7'>We received a request to reset your ARA account password. Click the button below to set a new password. This link expires in <strong style='color:#f1f5f9'>1 hour</strong>.</p>" +
               "<div style='text-align:center;margin:28px 0'>" +
               "<a href='" + resetLink + "' style='background:#3b82f6;color:#fff;text-decoration:none;font-weight:600;padding:12px 32px;border-radius:8px;font-size:15px;display:inline-block'>Reset My Password</a>" +
               "</div>" +
               "<p style='color:#475569;font-size:12px;line-height:1.6'>If you didn't request a password reset, please ignore this email. Your password won't be changed.</p>" +
               "<p style='color:#475569;font-size:12px;margin-top:4px'>If the button doesn't work, copy this link: <span style='color:#93c5fd'>" + resetLink + "</span></p>" +
               "</td></tr>" +
               "<tr><td style='padding:16px 32px;border-top:1px solid #334155'>" +
               "<p style='margin:0;font-size:11px;color:#475569;text-align:center'>Asset Risk Assurance Platform</p>" +
               "</td></tr>" +
               "</table></td></tr></table></body></html>";
    }
}
