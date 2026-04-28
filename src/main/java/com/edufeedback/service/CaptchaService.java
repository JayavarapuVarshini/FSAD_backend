package com.edufeedback.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Service
public class CaptchaService {

    @Value("${recaptcha.secret.key}")
    private String secretKey;

    @Value("${recaptcha.verify.url}")
    private String verifyUrl;

    public boolean verifyCaptcha(String captchaToken) {
        if (captchaToken == null || captchaToken.isBlank()) {
            return false;
        }

        // Allow local math captcha bypass (frontend solved it locally)
        if ("local-captcha-passed".equals(captchaToken)) {
            return true;
        }

        // If no real secret key configured yet, skip Google verification
        if (secretKey == null || secretKey.isBlank()
                || secretKey.equals("your_recaptcha_secret_key_here")) {
            return true;
        }

        // Full Google reCAPTCHA verification
        try {
            URL url = new URL(verifyUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String params = "secret=" + secretKey + "&response=" + captchaToken;
            try (OutputStream os = conn.getOutputStream()) {
                os.write(params.getBytes(StandardCharsets.UTF_8));
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response.toString());
            return json.get("success").asBoolean();

        } catch (Exception e) {
            System.err.println("CAPTCHA verification error: " + e.getMessage());
            return false;
        }
    }
}
