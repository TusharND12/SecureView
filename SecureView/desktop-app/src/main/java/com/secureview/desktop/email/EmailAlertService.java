package com.secureview.desktop.email;

import com.secureview.desktop.config.ConfigManager;
import com.secureview.desktop.config.ApplicationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

/**
 * Sends intrusion alerts via email with intruder image attached.
 */
public class EmailAlertService {
    private static final Logger logger = LoggerFactory.getLogger(EmailAlertService.class);

    private static EmailAlertService instance;

    private final ConfigManager configManager;

    private EmailAlertService() {
        this.configManager = ConfigManager.getInstance();
    }

    public static synchronized EmailAlertService getInstance() {
        if (instance == null) {
            instance = new EmailAlertService();
        }
        return instance;
    }

    private boolean isEmailConfigured() {
        ApplicationConfig config = configManager.getConfig();
        return config.getSmtpHost() != null && !config.getSmtpHost().isEmpty()
            && config.getSmtpUsername() != null && !config.getSmtpUsername().isEmpty()
            && config.getSmtpPassword() != null && !config.getSmtpPassword().isEmpty()
            && config.getAlertEmailFrom() != null && !config.getAlertEmailFrom().isEmpty();
    }

    /**
     * Reads the latest alert email from the CSV file written during registration.
     * Falls back to config.getAlertEmailTo() if CSV is missing or empty.
     */
    private String getRecipientEmail() {
        // Try multiple possible CSV locations
        String[] possiblePaths = {
            System.getProperty("user.home") + File.separator + ".secureview" + File.separator + "Email Alert Data.csv",
            System.getProperty("user.dir") + File.separator + "Email Alert Data.csv",
            "Email Alert Data.csv"
        };
        
        for (String csvPath : possiblePaths) {
            try {
                Path path = Paths.get(csvPath);
                if (Files.exists(path)) {
                    List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
                    // Find last non-header, non-empty line
                    for (int i = lines.size() - 1; i >= 0; i--) {
                        String line = lines.get(i).trim();
                        if (line.isEmpty() || line.startsWith("timestamp") || line.startsWith("Timestamp")) {
                            continue;
                        }
                        String[] parts = line.split(",", 2);
                        if (parts.length == 2) {
                            String email = parts[1].trim();
                            if (!email.isEmpty() && email.contains("@")) {
                                logger.info("Found alert email in CSV: {}", email);
                                return email;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.debug("Failed to read alert email from CSV at: {}", csvPath, e);
            }
        }

        // Fallback to config value
        String cfgEmail = configManager.getConfig().getAlertEmailTo();
        if (cfgEmail != null && !cfgEmail.isEmpty() && cfgEmail.contains("@")) {
            logger.info("Using alert email from config: {}", cfgEmail);
            return cfgEmail;
        }
        
        logger.warn("No valid alert recipient email found in CSV or config");
        return null;
    }

    /**
     * Sends an intrusion alert email with the intruder image attached.
     */
    public void sendIntrusionAlert(byte[] intruderImage, String timestamp, String details) {
        if (!isEmailConfigured()) {
            logger.warn("Email not configured. Skipping intrusion email alert.");
            return;
        }

        try {
            ApplicationConfig cfg = configManager.getConfig();
            String recipient = getRecipientEmail();
            if (recipient == null || recipient.isEmpty()) {
                logger.warn("No alert recipient email found in CSV or config. Skipping email alert.");
                return;
            }

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", String.valueOf(cfg.isSmtpUseTls()));
            props.put("mail.smtp.host", cfg.getSmtpHost());
            props.put("mail.smtp.port", String.valueOf(cfg.getSmtpPort()));

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(cfg.getSmtpUsername(), cfg.getSmtpPassword());
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(cfg.getAlertEmailFrom()));
            message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(recipient));
            message.setSubject("SecureView Intrusion Alert - " + timestamp);

            // Text part
            MimeBodyPart textPart = new MimeBodyPart();
            StringBuilder body = new StringBuilder();
            body.append("An unauthorized access attempt was detected on your device.\n\n");
            body.append("Time: ").append(timestamp).append("\n");
            body.append("Details: ").append(details != null ? details : "N/A").append("\n\n");
            body.append("This message was sent automatically by SecureView.");
            textPart.setText(body.toString());

            // Image attachment
            MimeBodyPart imagePart = new MimeBodyPart();
            imagePart.setFileName("intruder.jpg");
            imagePart.setContent(intruderImage, "image/jpeg");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);
            multipart.addBodyPart(imagePart);

            message.setContent(multipart);

            Transport.send(message);

            logger.info("Intrusion alert email sent successfully to {}", recipient);

        } catch (Exception e) {
            logger.error("Failed to send intrusion alert email", e);
        }
    }
}


