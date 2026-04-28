package com.edufeedback.controller;

import com.edufeedback.dto.ApiResponse;
import com.edufeedback.dto.FeedbackFormDTO;
import com.edufeedback.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private FeedbackService feedbackService;

    // Create a new feedback form
    @PostMapping("/forms")
    public ResponseEntity<ApiResponse> createForm(
            @RequestBody FeedbackFormDTO dto,
            Authentication authentication) {
        String email = authentication.getName();
        ApiResponse response = feedbackService.createForm(dto, email);
        return ResponseEntity.ok(response);
    }

    // Get only forms created by this admin
    @GetMapping("/forms")
    public ResponseEntity<List<FeedbackFormDTO>> getAllForms(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(feedbackService.getFormsByAdmin(email));
    }

    // Update an existing form (only if created by this admin)
    @PutMapping("/forms/{id}")
    public ResponseEntity<ApiResponse> updateForm(
            @PathVariable Long id,
            @RequestBody FeedbackFormDTO dto,
            Authentication authentication) {
        String email = authentication.getName();
        ApiResponse response = feedbackService.updateForm(id, dto, email);
        return ResponseEntity.ok(response);
    }

    // Toggle form active/inactive
    @PutMapping("/forms/{id}/toggle-status")
    public ResponseEntity<ApiResponse> toggleStatus(@PathVariable Long id) {
        return ResponseEntity.ok(feedbackService.toggleFormStatus(id));
    }

    // Delete form
    @DeleteMapping("/forms/{id}")
    public ResponseEntity<ApiResponse> deleteForm(@PathVariable Long id) {
        return ResponseEntity.ok(feedbackService.deleteForm(id));
    }

    // Get analytics dashboard data (only for this admin's forms)
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(feedbackService.getAnalytics(email));
    }
}
