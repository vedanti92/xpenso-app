package com.example.xpenso.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class EmailService {
    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.from.email}")
    private String fromEmail;

    @Value("${brevo.from.name:Xpenso}")
    private String fromName;

    private RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000); // 10 seconds
        factory.setReadTimeout(10000); // 10 seconds
        return new RestTemplate(factory);
    }

    @Async
    public CompletableFuture<Void> sendEmailAsync(String to, String subject, String body) {
        try {
            sendEmail(to, subject, body);
            log.info("Email sent successfully to: {}", to);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Failed to send email to: {}. Error: {}", to, e.getMessage(), e);
            return CompletableFuture.completedFuture(null); // Don't throw, just log
        }
    }

    public void sendEmail(String to, String subject, String body) {
        try {
            RestTemplate restTemplate = createRestTemplate();
            String url = "https://api.brevo.com/v3/smtp/email";

            Map<String, Object> payload = new HashMap<>();
            payload.put("sender", Map.of("name", fromName, "email", fromEmail));
            payload.put("to", new Map[]{ Map.of("email", to) });
            payload.put("subject", subject);
            payload.put("htmlContent", body);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            log.info("Email API response for {}: {}", to, response.getBody());
        } catch (RestClientException e) {
            log.error("RestClientException while sending email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Email sending failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error while sending email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Email sending failed: " + e.getMessage(), e);
        }
    }

    public void sendEmailWithAttachment(
            String to,
            String subject,
            String body,
            byte[] attachment,
            String filename
    ) {
        try {
            RestTemplate restTemplate = createRestTemplate();
            String url = "https://api.brevo.com/v3/smtp/email";

            // Convert attachment to Base64
            String base64Attachment = java.util.Base64.getEncoder().encodeToString(attachment);

            Map<String, Object> payload = new HashMap<>();
            payload.put("sender", Map.of("name", fromName, "email", fromEmail));
            payload.put("to", new Map[]{ Map.of("email", to) });
            payload.put("subject", subject);
            payload.put("htmlContent", body);

            // Add attachment here
            payload.put("attachment", new Object[]{
                    Map.of(
                            "name", filename,
                            "content", base64Attachment
                    )
            });

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            log.info("Email with attachment sent to {}. Response: {}", to, response.getBody());

        } catch (Exception e) {
            log.error("Failed to send email with attachment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send email with attachment", e);
        }
    }

}
