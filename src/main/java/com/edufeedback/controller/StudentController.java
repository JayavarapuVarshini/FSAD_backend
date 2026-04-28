package com.edufeedback.controller;

import com.edufeedback.dto.ApiResponse;
import com.edufeedback.dto.FeedbackFormDTO;
import com.edufeedback.dto.FeedbackSubmitRequest;
import com.edufeedback.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student")
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {

    @Autowired
    private FeedbackService feedbackService;

    // Get all active feedback forms
    @GetMapping("/forms")
    public ResponseEntity<List<FeedbackFormDTO>> getActiveForms() {
        return ResponseEntity.ok(feedbackService.getActiveForms());
    }

    // Submit feedback
    @PostMapping("/submit-feedback")
    public ResponseEntity<ApiResponse> submitFeedback(
            @RequestBody FeedbackSubmitRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        ApiResponse response = feedbackService.submitFeedback(request, email);
        return ResponseEntity.ok(response);
    }

    // Get IDs of forms the student has already submitted
    @GetMapping("/submitted-forms")
    public ResponseEntity<List<Long>> getSubmittedForms(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(feedbackService.getSubmittedFormIds(email));
    }
}
