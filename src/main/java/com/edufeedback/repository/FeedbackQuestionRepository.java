package com.edufeedback.repository;

import com.edufeedback.model.FeedbackQuestion;
import com.edufeedback.model.FeedbackForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface FeedbackQuestionRepository extends JpaRepository<FeedbackQuestion, Long> {
    List<FeedbackQuestion> findByFormOrderByOrderIndexAsc(FeedbackForm form);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    void deleteByForm(FeedbackForm form);
}
