package com.edufeedback.dto;

import java.util.List;

public class FeedbackFormDTO {
    private Long id;
    private String title;
    private String course;
    private String instructor;
    private String description;
    private String status;
    private long responseCount;
    private Double averageRating;
    private String createdAt;
    private List<QuestionDTO> questions;

    public FeedbackFormDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }

    public String getInstructor() { return instructor; }
    public void setInstructor(String instructor) { this.instructor = instructor; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getResponseCount() { return responseCount; }
    public void setResponseCount(long responseCount) { this.responseCount = responseCount; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public List<QuestionDTO> getQuestions() { return questions; }
    public void setQuestions(List<QuestionDTO> questions) { this.questions = questions; }

    public static class QuestionDTO {
        private Long id;
        private String questionText;
        private String questionType;
        private List<String> options;
        private int orderIndex;

        public QuestionDTO() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getQuestionText() { return questionText; }
        public void setQuestionText(String questionText) { this.questionText = questionText; }

        public String getQuestionType() { return questionType; }
        public void setQuestionType(String questionType) { this.questionType = questionType; }

        public List<String> getOptions() { return options; }
        public void setOptions(List<String> options) { this.options = options; }

        public int getOrderIndex() { return orderIndex; }
        public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
    }
}
