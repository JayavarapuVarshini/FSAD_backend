package com.edufeedback.service;

import com.edufeedback.dto.FeedbackFormDTO;
import com.edufeedback.dto.FeedbackSubmitRequest;
import com.edufeedback.dto.ApiResponse;
import com.edufeedback.model.*;
import com.edufeedback.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackFormRepository formRepository;

    @Autowired
    private FeedbackResponseRepository responseRepository;

    @Autowired
    private FeedbackAnswerRepository answerRepository;

    @Autowired
    private FeedbackQuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ---- Admin: Create Form ----
    @Transactional
    public ApiResponse createForm(FeedbackFormDTO dto, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        FeedbackForm form = new FeedbackForm();
        form.setTitle(dto.getTitle());
        form.setCourse(dto.getCourse());
        form.setInstructor(dto.getInstructor());
        form.setDescription(dto.getDescription());
        form.setStatus(FeedbackForm.FormStatus.ACTIVE);
        form.setCreatedBy(admin);
        FeedbackForm savedForm;
        try {
            savedForm = formRepository.save(form);
            System.out.println("[FeedbackService] Saved FeedbackForm id=" + savedForm.getId());
        } catch (Exception e) {
            System.err.println("[FeedbackService] Error saving FeedbackForm: " + e.getMessage());
            throw new RuntimeException("Failed to save feedback form: " + e.getMessage(), e);
        }

        if (dto.getQuestions() != null) {
            for (int i = 0; i < dto.getQuestions().size(); i++) {
                FeedbackFormDTO.QuestionDTO qDto = dto.getQuestions().get(i);
                FeedbackQuestion question = new FeedbackQuestion();
                question.setForm(savedForm);
                question.setQuestionText(qDto.getQuestionText());
                String rawType = qDto.getQuestionType() != null ? qDto.getQuestionType().toUpperCase().trim() : "TEXT";
                question.setQuestionType(FeedbackQuestion.QuestionType.valueOf(rawType));
                question.setOrderIndex(i);
                if (qDto.getOptions() != null && !qDto.getOptions().isEmpty()) {
                    try {
                        question.setOptions(objectMapper.writeValueAsString(qDto.getOptions()));
                    } catch (Exception e) {
                        question.setOptions("[]");
                    }
                }
                questionRepository.save(question);
            }
        }

        return new ApiResponse(true, "Feedback form created successfully!", savedForm.getId());
    }

    // ---- Admin: Update Form ----
    @Transactional
    public ApiResponse updateForm(Long formId, FeedbackFormDTO dto, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        FeedbackForm form = formRepository.findById(formId)
                .orElseThrow(() -> new RuntimeException("Form not found"));

        if (form.getCreatedBy() == null || !form.getCreatedBy().getId().equals(admin.getId())) {
            return new ApiResponse(false, "You are not authorized to update this form.");
        }

        form.setTitle(dto.getTitle());
        form.setCourse(dto.getCourse());
        form.setInstructor(dto.getInstructor());
        form.setDescription(dto.getDescription());
        formRepository.save(form);

        // Must delete answers before questions (FK constraint: answers reference questions)
        List<FeedbackQuestion> oldQuestions = questionRepository.findByFormOrderByOrderIndexAsc(form);
        if (!oldQuestions.isEmpty()) {
            answerRepository.deleteByQuestionIn(oldQuestions);
            answerRepository.flush();
            questionRepository.deleteByForm(form);
            questionRepository.flush();
        }

        if (dto.getQuestions() != null) {
            for (int i = 0; i < dto.getQuestions().size(); i++) {
                FeedbackFormDTO.QuestionDTO qDto = dto.getQuestions().get(i);
                FeedbackQuestion question = new FeedbackQuestion();
                question.setForm(form);
                question.setQuestionText(qDto.getQuestionText());
                String rawType = qDto.getQuestionType() != null ? qDto.getQuestionType().toUpperCase().trim() : "TEXT";
                question.setQuestionType(FeedbackQuestion.QuestionType.valueOf(rawType));
                question.setOrderIndex(i);
                if (qDto.getOptions() != null && !qDto.getOptions().isEmpty()) {
                    try {
                        question.setOptions(objectMapper.writeValueAsString(qDto.getOptions()));
                    } catch (Exception e) {
                        question.setOptions("[]");
                    }
                }
                questionRepository.save(question);
            }
        }

        return new ApiResponse(true, "Feedback form updated successfully!", formId);
    }

    // ---- Admin: Delete Form ----
    // Correct order: answers → responses → questions → form
    @Transactional
    public ApiResponse deleteForm(Long formId) {
        FeedbackForm form = formRepository.findById(formId).orElse(null);
        if (form == null) return new ApiResponse(false, "Form not found.");

        try {
            // 1. Delete all answers linked to responses of this form
            List<FeedbackResponse> responses = responseRepository.findByForm(form);
            for (FeedbackResponse response : responses) {
                answerRepository.deleteByResponse(response);
            }
            answerRepository.flush();

            // 2. Delete all answers linked to questions of this form (safety)
            List<FeedbackQuestion> questions = questionRepository.findByFormOrderByOrderIndexAsc(form);
            if (!questions.isEmpty()) {
                answerRepository.deleteByQuestionIn(questions);
                answerRepository.flush();
            }

            // 3. Delete all responses
            responseRepository.deleteByForm(form);
            responseRepository.flush();

            // 4. Delete all questions
            if (!questions.isEmpty()) {
                questionRepository.deleteByForm(form);
                questionRepository.flush();
            }

            // 5. Delete the form
            formRepository.deleteById(formId);
            formRepository.flush();

            System.out.println("[FeedbackService] Successfully deleted form id=" + formId);
            return new ApiResponse(true, "Form deleted successfully.");
        } catch (Exception e) {
            System.err.println("[FeedbackService] Error deleting form id=" + formId + ": " + e.getMessage());
            throw new RuntimeException("Failed to delete form: " + e.getMessage(), e);
        }
    }

    // ---- Get All Active Forms (for students) ----
    public List<FeedbackFormDTO> getActiveForms() {
        return formRepository.findByStatus(FeedbackForm.FormStatus.ACTIVE)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ---- Get Forms by Admin ----
    public List<FeedbackFormDTO> getFormsByAdmin(String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        return formRepository.findByCreatedBy(admin).stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ---- Toggle Form Status ----
    @Transactional
    public ApiResponse toggleFormStatus(Long formId) {
        FeedbackForm form = formRepository.findById(formId)
                .orElseThrow(() -> new RuntimeException("Form not found"));
        form.setStatus(form.getStatus() == FeedbackForm.FormStatus.ACTIVE
                ? FeedbackForm.FormStatus.INACTIVE : FeedbackForm.FormStatus.ACTIVE);
        formRepository.save(form);
        return new ApiResponse(true, "Form status updated.");
    }

    // ---- Submit Feedback (Student) ----
    @Transactional
    public ApiResponse submitFeedback(FeedbackSubmitRequest request, String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        FeedbackForm form = formRepository.findById(request.getFormId())
                .orElseThrow(() -> new RuntimeException("Form not found"));

        if (responseRepository.existsByFormAndStudent(form, student))
            return new ApiResponse(false, "You have already submitted feedback for this form.");

        FeedbackResponse response = new FeedbackResponse();
        response.setForm(form);
        response.setStudent(student);
        response.setOverallRating(request.getOverallRating());
        FeedbackResponse savedResponse = responseRepository.save(response);

        if (request.getAnswers() != null) {
            for (FeedbackSubmitRequest.AnswerDTO aDto : request.getAnswers()) {
                FeedbackAnswer answer = new FeedbackAnswer();
                answer.setResponse(savedResponse);
                FeedbackQuestion question = questionRepository.findById(aDto.getQuestionId())
                        .orElseThrow(() -> new RuntimeException("Question not found: " + aDto.getQuestionId()));
                answer.setQuestion(question);
                answer.setAnswerText(aDto.getAnswerText());
                answer.setRatingValue(aDto.getRatingValue());
                answerRepository.save(answer);
            }
        }

        return new ApiResponse(true, "Feedback submitted successfully!");
    }

    // ---- Get form IDs submitted by student ----
    public List<Long> getSubmittedFormIds(String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return responseRepository.findByStudent(student)
                .stream().map(r -> r.getForm().getId()).collect(Collectors.toList());
    }

    // ---- Admin Analytics ----
    public Map<String, Object> getAnalytics(String adminEmail) {
        Map<String, Object> analytics = new HashMap<>();
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        List<FeedbackForm> forms = formRepository.findByCreatedBy(admin);

        analytics.put("totalForms", forms.size());
        analytics.put("activeForms", forms.stream().filter(f -> f.getStatus() == FeedbackForm.FormStatus.ACTIVE).count());
        analytics.put("totalResponses", responseRepository.count());
        analytics.put("totalStudents", userRepository.countByRole(User.Role.STUDENT));

        LocalDateTime since = LocalDateTime.now().minusHours(24);
        Set<Long> adminFormIds = forms.stream().map(FeedbackForm::getId).collect(Collectors.toSet());
        List<FeedbackResponse> recentResponses = responseRepository.findAll().stream()
                .filter(r -> r.getSubmittedAt() != null && r.getSubmittedAt().isAfter(since))
                .filter(r -> adminFormIds.contains(r.getForm().getId()))
                .sorted(Comparator.comparing(FeedbackResponse::getSubmittedAt).reversed())
                .collect(Collectors.toList());

        List<Map<String, Object>> recentSubmissions = recentResponses.stream().map(r -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", r.getId());
            item.put("formId", r.getForm().getId());
            item.put("formTitle", r.getForm().getTitle());
            item.put("course", r.getForm().getCourse());
            item.put("studentName", r.getStudent().getFullName());
            item.put("overallRating", r.getOverallRating());
            item.put("submittedAt", r.getSubmittedAt().toString());
            return item;
        }).collect(Collectors.toList());
        analytics.put("recentSubmissions", recentSubmissions);
        analytics.put("newNotificationsCount", recentSubmissions.size());

        List<Map<String, Object>> formStats = forms.stream().map(form -> {
            Map<String, Object> stat = new HashMap<>();
            stat.put("formId", form.getId());
            stat.put("title", form.getTitle());
            stat.put("course", form.getCourse());
            stat.put("instructor", form.getInstructor());
            stat.put("status", form.getStatus().name().toLowerCase());
            stat.put("responseCount", responseRepository.countByForm(form));
            stat.put("avgRating", responseRepository.findAverageRatingByFormId(form.getId()).orElse(0.0));
            return stat;
        }).collect(Collectors.toList());

        analytics.put("formStats", formStats);
        return analytics;
    }

    // ---- Convert FeedbackForm to DTO ----
    private FeedbackFormDTO toDTO(FeedbackForm form) {
        FeedbackFormDTO dto = new FeedbackFormDTO();
        dto.setId(form.getId());
        dto.setTitle(form.getTitle());
        dto.setCourse(form.getCourse());
        dto.setInstructor(form.getInstructor());
        dto.setDescription(form.getDescription());
        dto.setStatus(form.getStatus().name().toLowerCase());
        dto.setResponseCount(responseRepository.countByForm(form));
        dto.setAverageRating(responseRepository.findAverageRatingByFormId(form.getId()).orElse(0.0));
        dto.setCreatedAt(form.getCreatedAt() != null ? form.getCreatedAt().toString() : "");

        List<FeedbackQuestion> questions = questionRepository.findByFormOrderByOrderIndexAsc(form);
        if (questions != null) {
            List<FeedbackFormDTO.QuestionDTO> qDtos = questions.stream().map(q -> {
                FeedbackFormDTO.QuestionDTO qDto = new FeedbackFormDTO.QuestionDTO();
                qDto.setId(q.getId());
                qDto.setQuestionText(q.getQuestionText());
                qDto.setQuestionType(q.getQuestionType().name());
                qDto.setOrderIndex(q.getOrderIndex());
                if (q.getOptions() != null && !q.getOptions().isBlank()) {
                    try {
                        qDto.setOptions(objectMapper.readValue(q.getOptions(), new TypeReference<List<String>>() {}));
                    } catch (Exception e) {
                        qDto.setOptions(List.of());
                    }
                }
                return qDto;
            }).collect(Collectors.toList());
            dto.setQuestions(qDtos);
        }

        return dto;
    }
}
