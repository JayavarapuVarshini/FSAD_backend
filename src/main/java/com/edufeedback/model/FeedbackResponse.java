package com.edufeedback.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "feedback_responses")
public class FeedbackResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "form_id", nullable = false)
    private FeedbackForm form;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @OneToMany(mappedBy = "response", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FeedbackAnswer> answers;

    private Double overallRating;

    @Column(updatable = false)
    private LocalDateTime submittedAt;

    @PrePersist
    protected void onCreate() { submittedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public FeedbackForm getForm() { return form; }
    public void setForm(FeedbackForm form) { this.form = form; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public List<FeedbackAnswer> getAnswers() { return answers; }
    public void setAnswers(List<FeedbackAnswer> answers) { this.answers = answers; }

    public Double getOverallRating() { return overallRating; }
    public void setOverallRating(Double overallRating) { this.overallRating = overallRating; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
}
