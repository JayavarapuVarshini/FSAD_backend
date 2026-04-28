package com.edufeedback.service;

import com.edufeedback.model.OtpToken;
import com.edufeedback.repository.OtpTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OtpService {

    @Autowired
    private OtpTokenRepository otpTokenRepository;

    @Autowired
    private EmailService emailService;

    @Value("${app.otp.expiry-minutes:10}")
    private int otpExpiryMinutes;

    private final SecureRandom random = new SecureRandom();

    @Transactional
    public void generateAndSendOtp(String email, OtpToken.OtpPurpose purpose) {
        // Always normalize email so save and verify use the same value
        String normalizedEmail = email.toLowerCase().trim();

        try {
            otpTokenRepository.deleteByEmailAndPurpose(normalizedEmail, purpose);
        } catch (Exception e) {
            System.err.println("[OtpService] Could not delete old OTPs: " + e.getMessage());
        }

        // Generate 6-digit OTP
        String otp = String.format("%06d", random.nextInt(1000000));

        OtpToken otpToken = new OtpToken();
        otpToken.setEmail(normalizedEmail);
        otpToken.setOtp(otp);
        otpToken.setPurpose(purpose);
        otpToken.setExpiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes));
        otpToken.setUsed(false);

        otpTokenRepository.save(otpToken);
        System.out.println("[OtpService] OTP saved for " + normalizedEmail + " purpose=" + purpose);

        emailService.sendOtpEmail(normalizedEmail, otp, purpose.name());
    }

    @Transactional
    public boolean verifyOtp(String email, String otp, OtpToken.OtpPurpose purpose) {
        // Normalize email and trim OTP before comparing
        String normalizedEmail = email.toLowerCase().trim();
        String trimmedOtp = otp != null ? otp.trim() : "";

        Optional<OtpToken> tokenOpt = otpTokenRepository
                .findTopByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(normalizedEmail, purpose);

        if (tokenOpt.isEmpty()) {
            System.out.println("[OtpService] No unused OTP found for " + normalizedEmail);
            return false;
        }

        OtpToken token = tokenOpt.get();

        if (token.isExpired()) {
            System.out.println("[OtpService] OTP expired for " + normalizedEmail);
            return false;
        }

        String storedOtp = token.getOtp() != null ? token.getOtp().trim() : "";
        if (!storedOtp.equals(trimmedOtp)) {
            System.out.println("[OtpService] OTP mismatch for " + normalizedEmail
                    + " — stored: " + storedOtp + ", provided: " + trimmedOtp);
            return false;
        }

        // Mark as used
        token.setUsed(true);
        otpTokenRepository.save(token);
        System.out.println("[OtpService] OTP verified successfully for " + normalizedEmail);
        return true;
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanExpiredOtps() {
        otpTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}
