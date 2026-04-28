package com.edufeedback.model;

import jakarta.persistence.*;

@Entity
@Table(name = "feedback_answers")
public class FeedbackAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "response_id", nullable = false)
    private FeedbackResponse response;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private FeedbackQuestion question;

    @Column(length = 2000)
    private String answerText;

    private Double ratingValue;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public FeedbackResponse getResponse() { return response; }
    public void setResponse(FeedbackResponse response) { this.response = response; }

    public FeedbackQuestion getQuestion() { return question; }
    public void setQuestion(FeedbackQuestion question) { this.question = question; }

    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }

    public Double getRatingValue() { return ratingValue; }
    public void setRatingValue(Double ratingValue) { this.ratingValue = ratingValue; }
}
