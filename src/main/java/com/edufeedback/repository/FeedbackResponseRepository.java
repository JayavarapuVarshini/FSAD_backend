package com.edufeedback.repository;

import com.edufeedback.model.FeedbackForm;
import com.edufeedback.model.FeedbackResponse;
import com.edufeedback.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackResponseRepository extends JpaRepository<FeedbackResponse, Long> {
    List<FeedbackResponse> findByForm(FeedbackForm form);
    List<FeedbackResponse> findByStudent(User student);
    boolean existsByFormAndStudent(FeedbackForm form, User student);

    @Query("SELECT AVG(r.overallRating) FROM FeedbackResponse r WHERE r.form.id = :formId")
    Optional<Double> findAverageRatingByFormId(Long formId);

    long countByForm(FeedbackForm form);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    void deleteByForm(FeedbackForm form);
}
