package com.edufeedback.repository;

import com.edufeedback.model.FeedbackForm;
import com.edufeedback.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackFormRepository extends JpaRepository<FeedbackForm, Long> {
    List<FeedbackForm> findByStatus(FeedbackForm.FormStatus status);
    List<FeedbackForm> findByCreatedBy(User user);
}
