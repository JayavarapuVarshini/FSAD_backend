package com.edufeedback.service;

import com.edufeedback.dto.*;
import com.edufeedback.model.OtpToken;
import com.edufeedback.model.User;
import com.edufeedback.repository.UserRepository;
import com.edufeedback.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpService otpService;

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // ---- Register ----
    @Transactional
    public User saveUser(RegisterRequest request) {
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        String role = request.getRole() == null ? "student" : request.getRole().toLowerCase();
        user.setRole("admin".equals(role) ? User.Role.ADMIN : User.Role.STUDENT);
        if ("admin".equals(role) && request.getDepartment() != null && !request.getDepartment().isBlank()) {
            user.setDepartment(request.getDepartment().trim());
        }
        user.setEmailVerified(false);
        return userRepository.save(user);
    }

    public ApiResponse register(RegisterRequest request) {
        if (!captchaService.verifyCaptcha(request.getCaptchaToken()))
            return new ApiResponse(false, "CAPTCHA verification failed.");
        if (request.getEmail() == null || request.getEmail().isBlank())
            return new ApiResponse(false, "Email is required.");
        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim()))
            return new ApiResponse(false, "An account with this email already exists.");

        User user;
        try {
            user = saveUser(request);
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            System.err.println("Registration saveUser error: " + e.getMessage());
            if (cause != e) System.err.println("Caused by: " + cause.getMessage());
            return new ApiResponse(false, "Registration failed: " + (cause.getMessage() != null ? cause.getMessage() : e.getMessage()));
        }

        try {
            otpService.generateAndSendOtp(user.getEmail(), OtpToken.OtpPurpose.EMAIL_VERIFICATION);
            return new ApiResponse(true, "Registration successful! OTP sent to " + user.getEmail() + ". Please verify your email.");
        } catch (Exception e) {
            return new ApiResponse(true, "Account created! Email sending failed (" + e.getMessage() + "). Use Resend OTP on verification screen.");
        }
    }

    // ---- Verify Email ----
    @Transactional
    public ApiResponse verifyEmail(OtpVerifyRequest request) {
        if (request.getEmail() == null || request.getOtp() == null)
            return new ApiResponse(false, "Email and OTP are required.");

        boolean valid = otpService.verifyOtp(
                request.getEmail().toLowerCase().trim(),
                request.getOtp().trim(),
                OtpToken.OtpPurpose.EMAIL_VERIFICATION
        );
        if (!valid)
            return new ApiResponse(false, "Invalid or expired OTP. Please request a new one.");

        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEmailVerified(true);
        userRepository.save(user);
        return new ApiResponse(true, "Email verified successfully! You can now log in.");
    }

    // ---- Resend OTP ----
    @Transactional
    public ApiResponse resendOtp(OtpRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank())
            return new ApiResponse(false, "Email is required.");
        String email = request.getEmail().toLowerCase().trim();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account found with this email."));
        if (user.isEmailVerified())
            return new ApiResponse(false, "Email is already verified.");
        try {
            otpService.generateAndSendOtp(email, OtpToken.OtpPurpose.EMAIL_VERIFICATION);
            return new ApiResponse(true, "OTP resent to " + email);
        } catch (Exception e) {
            return new ApiResponse(false, "Failed to send OTP email: " + e.getMessage());
        }
    }

    // ---- Login (students only flow; admin goes through 2-step) ----
    public AuthResponse login(LoginRequest request) {
        if (request.getEmail() == null || request.getPassword() == null)
            return new AuthResponse(null, null, null, null, null, false, "Email and password are required.");
        if (!captchaService.verifyCaptcha(request.getCaptchaToken()))
            return new AuthResponse(null, null, null, null, null, false, "CAPTCHA verification failed.");

        String email = request.getEmail().toLowerCase().trim();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword()))
            return new AuthResponse(null, null, null, null, null, false, "Invalid email or password.");
        if (!user.isEmailVerified())
            return new AuthResponse(null, null, null, null, null, false, "EMAIL_NOT_VERIFIED: Please verify your email first.");

        // Admin must use the 2-step login flow
        if (user.getRole() == User.Role.ADMIN)
            return new AuthResponse(null, null, null, null, null, false, "ADMIN_REQUIRES_OTP: Please use admin login.");

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getEmail(), user.getFullName(),
                user.getRole().name().toLowerCase(), user.getId(), user.isEmailVerified());
    }

    // ---- Admin Login Step 1: validate credentials + send OTP ----
    public ApiResponse adminLoginSendOtp(LoginRequest request) {
        if (request.getEmail() == null || request.getPassword() == null)
            return new ApiResponse(false, "Email and password are required.");
        if (!captchaService.verifyCaptcha(request.getCaptchaToken()))
            return new ApiResponse(false, "CAPTCHA verification failed.");

        String email = request.getEmail().toLowerCase().trim();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword()))
            return new ApiResponse(false, "Invalid email or password.");
        if (user.getRole() != User.Role.ADMIN)
            return new ApiResponse(false, "This login is for admins only.");
        if (!user.isEmailVerified())
            return new ApiResponse(false, "EMAIL_NOT_VERIFIED: Please verify your email first.");

        try {
            otpService.generateAndSendOtp(email, OtpToken.OtpPurpose.ADMIN_LOGIN_VERIFICATION);
            return new ApiResponse(true, "OTP sent to " + email + ". Please verify to complete login.");
        } catch (Exception e) {
            return new ApiResponse(false, "Failed to send OTP: " + e.getMessage());
        }
    }

    // ---- Admin Login Step 2: verify OTP + issue JWT ----
    @Transactional
    public AuthResponse adminLoginVerifyOtp(OtpVerifyRequest request) {
        if (request.getEmail() == null || request.getOtp() == null)
            return new AuthResponse(null, null, null, null, null, false, "Email and OTP are required.");

        String email = request.getEmail().toLowerCase().trim();

        boolean valid = otpService.verifyOtp(email, request.getOtp().trim(), OtpToken.OtpPurpose.ADMIN_LOGIN_VERIFICATION);
        if (!valid)
            return new AuthResponse(null, null, null, null, null, false, "Invalid or expired OTP. Please request a new one.");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getEmail(), user.getFullName(),
                user.getRole().name().toLowerCase(), user.getId(), user.isEmailVerified(),
                "Login successful!");
    }

    // ---- Forgot Password Step 1: send OTP ----
    public ApiResponse forgotPassword(OtpRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank())
            return new ApiResponse(false, "Email is required.");
        String email = request.getEmail().toLowerCase().trim();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null)
            return new ApiResponse(false, "No account found with this email.");
        try {
            otpService.generateAndSendOtp(email, OtpToken.OtpPurpose.PASSWORD_RESET);
            return new ApiResponse(true, "Password reset OTP sent to " + email + ". Check your inbox.");
        } catch (Exception e) {
            return new ApiResponse(false, "Failed to send OTP: " + e.getMessage());
        }
    }

    // ---- Forgot Password Step 2: verify OTP only (no password change yet) ----
    public ApiResponse verifyResetOtp(OtpVerifyRequest request) {
        if (request.getEmail() == null || request.getOtp() == null)
            return new ApiResponse(false, "Email and OTP are required.");

        String email = request.getEmail().toLowerCase().trim();

        boolean valid = otpService.verifyOtp(email, request.getOtp().trim(), OtpToken.OtpPurpose.PASSWORD_RESET);
        if (!valid)
            return new ApiResponse(false, "Invalid or expired OTP. Please request a new one.");

        return new ApiResponse(true, "OTP verified. Please set your new password.");
    }

    // ---- Forgot Password Step 3: change password (OTP already verified) ----
    @Transactional
    public ApiResponse resetPassword(ResetPasswordRequest request) {
        if (request.getEmail() == null || request.getNewPassword() == null)
            return new ApiResponse(false, "Email and new password are required.");

        String email = request.getEmail().toLowerCase().trim();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return new ApiResponse(true, "Password reset successfully! You can now log in with your new password.");
    }
}
