package com.edufeedback.repository;

import com.edufeedback.model.FeedbackAnswer;
import com.edufeedback.model.FeedbackQuestion;
import com.edufeedback.model.FeedbackResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface FeedbackAnswerRepository extends JpaRepository<FeedbackAnswer, Long> {
    List<FeedbackAnswer> findByResponse(FeedbackResponse response);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    void deleteByResponse(FeedbackResponse response);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM FeedbackAnswer a WHERE a.question.id = :questionId")
    void deleteByQuestionId(Long questionId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM FeedbackAnswer a WHERE a.question IN :questions")
    void deleteByQuestionIn(List<FeedbackQuestion> questions);
}
