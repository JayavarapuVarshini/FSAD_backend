-- =====================================================
-- EduFeedback Database Schema
-- Tables auto-created by Spring Boot on startup
-- =====================================================

CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('STUDENT', 'ADMIN', 'TEACHER') NOT NULL DEFAULT 'STUDENT',
    department VARCHAR(255),
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME,
    updated_at DATETIME,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS otp_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    otp VARCHAR(10) NOT NULL,
    expires_at DATETIME NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    purpose ENUM('EMAIL_VERIFICATION', 'PASSWORD_RESET', 'ADMIN_LOGIN_VERIFICATION') NOT NULL,
    created_at DATETIME,
    PRIMARY KEY (id),
    INDEX idx_otp_email_purpose (email, purpose)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Fix existing tables: update purpose ENUM if the column still has the old values
-- This handles already-existing databases that were created with the old schema
ALTER TABLE otp_tokens
    MODIFY COLUMN purpose ENUM('EMAIL_VERIFICATION', 'PASSWORD_RESET', 'ADMIN_LOGIN_VERIFICATION') NOT NULL;

CREATE TABLE IF NOT EXISTS feedback_forms (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    course VARCHAR(255) NOT NULL,
    instructor VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_by BIGINT,
    created_at DATETIME,
    updated_at DATETIME,
    PRIMARY KEY (id),
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS feedback_questions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    form_id BIGINT NOT NULL,
    question_text VARCHAR(1000) NOT NULL,
    question_type ENUM('RATING', 'MCQ', 'TEXT') NOT NULL,
    options TEXT,
    order_index INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    FOREIGN KEY (form_id) REFERENCES feedback_forms(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS feedback_responses (
    id BIGINT NOT NULL AUTO_INCREMENT,
    form_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    overall_rating DOUBLE,
    submitted_at DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY unique_form_student (form_id, student_id),
    FOREIGN KEY (form_id) REFERENCES feedback_forms(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS feedback_answers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    response_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer_text TEXT,
    rating_value DOUBLE,
    PRIMARY KEY (id),
    FOREIGN KEY (response_id) REFERENCES feedback_responses(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES feedback_questions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
