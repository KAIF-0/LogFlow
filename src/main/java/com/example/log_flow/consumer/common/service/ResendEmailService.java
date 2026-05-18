package com.example.log_flow.consumer.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;

@Service
public class ResendEmailService implements EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResendEmailService.class);
    private static final String FROM_EMAIL = "LogFlow Alerts <alerts@prods.dev>";

    private final Resend resend;

    public ResendEmailService(@Value("${resend.api-key}") String apiKey) {
        this.resend = new Resend(apiKey);
    }

    @Override
    public void send(String to, String subject, String body) {
        try {
            String htmlBody = buildHtmlTemplate(subject, body);
                CreateEmailOptions request = CreateEmailOptions.builder()
                    .from(FROM_EMAIL)
                    .to(to)
                    .subject(subject)
                    .html(htmlBody)
                    .build();

            CreateEmailResponse response = resend.emails().send(request); 
            if (response.getId() != null) {
                LOGGER.info("Email sent successfully: {}", response.getId());
            } else {
                LOGGER.error("Failed to send email to {}: {}", to, response.toString());
            }
        } catch (ResendException e) {
            LOGGER.error("Resend error sending email to {}", to, e);
        }
    }

    private String buildHtmlTemplate(String subject, String message) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>LogFlow Alert</title>
                    <style>
                        body {
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                            background-color: #f5f5f5;
                            margin: 0;
                            padding: 0;
                        }
                        .container {
                            max-width: 600px;
                            margin: 20px auto;
                            background-color: #ffffff;
                            border-radius: 8px;
                            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
                            overflow: hidden;
                        }
                        .header {
                            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                            color: #ffffff;
                            padding: 30px 20px;
                            text-align: center;
                        }
                        .header h1 {
                            margin: 0;
                            font-size: 24px;
                            font-weight: 600;
                        }
                        .content {
                            padding: 30px;
                        }
                        .alert-box {
                            background-color: #fff3cd;
                            border-left: 4px solid #ffc107;
                            padding: 15px;
                            margin-bottom: 20px;
                            border-radius: 4px;
                        }
                        .alert-box strong {
                            color: #856404;
                        }
                        .message {
                            color: #333333;
                            line-height: 1.6;
                            font-size: 14px;
                            margin-bottom: 20px;
                        }
                        .footer {
                            background-color: #f8f9fa;
                            padding: 20px;
                            text-align: center;
                            border-top: 1px solid #e9ecef;
                            font-size: 12px;
                            color: #6c757d;
                        }
                        .footer a {
                            color: #667eea;
                            text-decoration: none;
                        }
                        .timestamp {
                            color: #6c757d;
                            font-size: 12px;
                            margin-top: 15px;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🚨 LogFlow Alert</h1>
                        </div>
                        <div class="content">
                            <div class="alert-box">
                                <strong>Alert Triggered</strong>
                            </div>
                            <div class="message">
                                """ + message + """
                            </div>
                            <div class="timestamp">
                                Sent at: """ + java.time.Instant.now() + """
                            </div>
                        </div>
                        <div class="footer">
                            <p>This is an automated alert from <strong>LogFlow</strong>.</p>
                            <p><a href="https://logflow.dev">Visit Dashboard</a> | <a href="https://logflow.dev/settings">Manage Alerts</a></p>
                            <p>&copy; 2026 LogFlow. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """;
    }
}
