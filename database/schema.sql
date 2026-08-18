-- ============================================================
-- Alumni Portal - MySQL Database Schema
-- ============================================================
-- NOTE: You do NOT need to run this manually for local development.
-- The Spring Boot backend uses spring.jpa.hibernate.ddl-auto=update,
-- which auto-creates/updates these exact tables on startup against
-- the `alumni_portal` database (auto-created too).
--
-- This file is provided as a deliverable / reference so you can:
--   1. Inspect the schema without running the backend.
--   2. Provision the schema manually in a production environment
--      where you'd rather control migrations explicitly.
-- ============================================================

CREATE DATABASE IF NOT EXISTS alumni_portal;
USE alumni_portal;

-- ---------------------------------------------------------
-- USERS
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'ALUMNI', 'STUDENT') NOT NULL,
    approved BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL
);

-- ---------------------------------------------------------
-- ALUMNI PROFILES
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS alumni_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    company VARCHAR(255),
    domain VARCHAR(255),
    skills VARCHAR(1000),
    location VARCHAR(255),
    graduation_year INT,
    current_role VARCHAR(255),
    experience INT,
    achievements VARCHAR(2000),
    bio VARCHAR(2000),
    linkedin_url VARCHAR(500),
    resume_file_path VARCHAR(500),
    available_for_mentorship BOOLEAN NOT NULL DEFAULT FALSE,
    last_verified_at DATETIME,
    profile_completeness INT DEFAULT 0,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_alumni_profiles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ---------------------------------------------------------
-- STUDENT PROFILES
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS student_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    branch VARCHAR(255),
    graduation_year INT,
    skills VARCHAR(1000),
    bio VARCHAR(2000),
    resume_file_path VARCHAR(500),
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_student_profiles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ---------------------------------------------------------
-- JOBS
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    posted_by BIGINT NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(4000),
    required_skills VARCHAR(1000) NOT NULL,
    experience_required INT,
    location VARCHAR(255),
    apply_link VARCHAR(500),
    type ENUM('JOB', 'INTERNSHIP') NOT NULL DEFAULT 'JOB',
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_jobs_posted_by FOREIGN KEY (posted_by) REFERENCES users(id) ON DELETE CASCADE
);

-- ---------------------------------------------------------
-- JOB APPLICATIONS
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS job_applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    resume_file_path VARCHAR(500),
    match_score DOUBLE DEFAULT 0,
    match_category ENUM('HIGH', 'MEDIUM', 'LOW') DEFAULT 'LOW',
    status ENUM('APPLIED', 'SHORTLISTED', 'REFERRED', 'REJECTED') DEFAULT 'APPLIED',
    `rank` INT DEFAULT 0,
    applied_at DATETIME NOT NULL,
    CONSTRAINT fk_job_applications_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    CONSTRAINT fk_job_applications_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_job_student UNIQUE (job_id, student_id)
);

-- ---------------------------------------------------------
-- MENTORSHIP REQUESTS
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS mentorship_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    alumni_id BIGINT NOT NULL,
    message VARCHAR(2000),
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'SCHEDULED', 'COMPLETED') DEFAULT 'PENDING',
    scheduled_at DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    CONSTRAINT fk_mentorship_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_mentorship_alumni FOREIGN KEY (alumni_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ---------------------------------------------------------
-- MESSAGES
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    mentorship_request_id BIGINT,
    content VARCHAR(4000) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    sent_at DATETIME NOT NULL,
    CONSTRAINT fk_messages_sender FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_receiver FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_messages_mentorship FOREIGN KEY (mentorship_request_id) REFERENCES mentorship_requests(id) ON DELETE SET NULL
);

-- ---------------------------------------------------------
-- EVENTS
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(3000),
    event_date DATETIME,
    location VARCHAR(255),
    created_by BIGINT,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_events_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- ---------------------------------------------------------
-- EVENT REGISTRATIONS  (supporting join table for "Students register for events")
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS event_registrations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    registered_at DATETIME NOT NULL,
    attended BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_event_reg_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT fk_event_reg_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_event_user UNIQUE (event_id, user_id)
);

-- ---------------------------------------------------------
-- MATERIALS
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS materials (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    file_url VARCHAR(500) NOT NULL,
    event_id BIGINT,
    uploaded_at DATETIME NOT NULL,
    CONSTRAINT fk_materials_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE SET NULL
);

-- ---------------------------------------------------------
-- RESUME DATA
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS resume_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    raw_text LONGTEXT,
    extracted_skills VARCHAR(1000),
    extracted_company VARCHAR(255),
    extracted_role VARCHAR(255),
    extracted_experience INT,
    source ENUM('RESUME_UPLOAD', 'LINKEDIN_URL'),
    uploaded_at DATETIME NOT NULL,
    CONSTRAINT fk_resume_data_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ---------------------------------------------------------
-- CONTRIBUTION HISTORY
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS contribution_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    alumni_id BIGINT NOT NULL,
    type ENUM('JOB_POSTED', 'MENTORSHIP_SESSION', 'EVENT_ATTENDED') NOT NULL,
    reference_id BIGINT,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_contribution_alumni FOREIGN KEY (alumni_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ---------------------------------------------------------
-- PROFILE VERIFICATION LOGS
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS profile_verification_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    alumni_id BIGINT NOT NULL,
    reminded_at DATETIME,
    verified_at DATETIME,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_verification_alumni FOREIGN KEY (alumni_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================================
-- End of schema
-- ============================================================
