package com.edufeedback.controller;

import com.edufeedback.dto.*;
import com.edufeedback.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@RequestBody RegisterRequest request) {
        try {
            return ResponseEntity.ok(authService.register(request));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse(false, "Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse> verifyEmail(@RequestBody OtpVerifyRequest request) {
        try {
            return ResponseEntity.ok(authService.verifyEmail(request));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse(false, "Verification failed: " + e.getMessage()));
        }
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse> resendOtp(@RequestBody OtpRequest request) {
        try {
            return ResponseEntity.ok(authService.resendOtp(request));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse(false, "Failed to resend OTP: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(authService.login(request));
        } catch (Exception e) {
            AuthResponse err = new AuthResponse();
            err.setMessage("Login failed: " + e.getMessage());
            return ResponseEntity.ok(err);
        }
    }

    @PostMapping("/admin-login-send-otp")
    public ResponseEntity<ApiResponse> adminLoginSendOtp(@RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(authService.adminLoginSendOtp(request));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse(false, "Failed: " + e.getMessage()));
        }
    }

    @PostMapping("/admin-login-verify-otp")
    public ResponseEntity<AuthResponse> adminLoginVerifyOtp(@RequestBody OtpVerifyRequest request) {
        try {
            return ResponseEntity.ok(authService.adminLoginVerifyOtp(request));
        } catch (Exception e) {
            AuthResponse err = new AuthResponse();
            err.setMessage("Verification failed: " + e.getMessage());
            return ResponseEntity.ok(err);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@RequestBody OtpRequest request) {
        try {
            return ResponseEntity.ok(authService.forgotPassword(request));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse(false, "Failed: " + e.getMessage()));
        }
    }

    @PostMapping("/verify-reset-otp")
    public ResponseEntity<ApiResponse> verifyResetOtp(@RequestBody OtpVerifyRequest request) {
        try {
            return ResponseEntity.ok(authService.verifyResetOtp(request));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse(false, "Verification failed: " + e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            return ResponseEntity.ok(authService.resetPassword(request));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse(false, "Reset failed: " + e.getMessage()));
        }
    }
}