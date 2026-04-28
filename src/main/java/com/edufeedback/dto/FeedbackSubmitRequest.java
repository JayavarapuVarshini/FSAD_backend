package com.edufeedback.dto;

import java.util.List;

public class FeedbackSubmitRequest {
    private Long formId;
    private Double overallRating;
    private List<AnswerDTO> answers;

    public FeedbackSubmitRequest() {}

    public Long getFormId() { return formId; }
    public void setFormId(Long formId) { this.formId = formId; }

    public Double getOverallRating() { return overallRating; }
    public void setOverallRating(Double overallRating) { this.overallRating = overallRating; }

    public List<AnswerDTO> getAnswers() { return answers; }
    public void setAnswers(List<AnswerDTO> answers) { this.answers = answers; }

    public static class AnswerDTO {
        private Long questionId;
        private String answerText;
        private Double ratingValue;

        public AnswerDTO() {}

        public Long getQuestionId() { return questionId; }
        public void setQuestionId(Long questionId) { this.questionId = questionId; }

        public String getAnswerText() { return answerText; }
        public void setAnswerText(String answerText) { this.answerText = answerText; }

        public Double getRatingValue() { return ratingValue; }
        public void setRatingValue(Double ratingValue) { this.ratingValue = ratingValue; }
    }
}
