package com.edufeedback.model;

import jakarta.persistence.*;

@Entity
@Table(name = "feedback_questions")
public class FeedbackQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "form_id", nullable = false)
    private FeedbackForm form;

    @Column(nullable = false)
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType questionType;

    @Column(length = 2000)
    private String options;

    @Column(nullable = false)
    private int orderIndex;

    public enum QuestionType { RATING, MCQ, TEXT }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public FeedbackForm getForm() { return form; }
    public void setForm(FeedbackForm form) { this.form = form; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public QuestionType getQuestionType() { return questionType; }
    public void setQuestionType(QuestionType questionType) { this.questionType = questionType; }

    public String getOptions() { return options; }
    public void setOptions(String options) { this.options = options; }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
}
