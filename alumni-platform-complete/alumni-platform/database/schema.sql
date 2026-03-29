-- ============================================================
-- Alumni Management & Career Networking Platform
-- MySQL Database Schema
-- ============================================================

CREATE DATABASE IF NOT EXISTS alumni_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE alumni_platform;

-- ============================================================
-- USERS TABLE (base auth table)
-- ============================================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('ALUMNI','STUDENT','ADMIN') NOT NULL,
    is_active BOOLEAN DEFAULT FALSE,
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_role (role)
);

-- ============================================================
-- ALUMNI PROFILES
-- ============================================================
CREATE TABLE alumni_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    avatar_url VARCHAR(500),
    bio TEXT,
    graduation_year INT,
    degree VARCHAR(100),
    branch VARCHAR(100),
    current_company VARCHAR(200),
    current_role VARCHAR(200),
    domain VARCHAR(100),
    location VARCHAR(200),
    linkedin_url VARCHAR(500),
    github_url VARCHAR(500),
    website_url VARCHAR(500),
    years_of_experience INT DEFAULT 0,
    available_for_mentorship BOOLEAN DEFAULT FALSE,
    profile_completeness INT DEFAULT 0,
    last_verified_at TIMESTAMP NULL,
    verification_reminder_sent BOOLEAN DEFAULT FALSE,
    resume_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_graduation_year (graduation_year),
    INDEX idx_domain (domain),
    INDEX idx_location (location),
    INDEX idx_mentorship (available_for_mentorship)
);

-- ============================================================
-- ALUMNI SKILLS
-- ============================================================
CREATE TABLE alumni_skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    alumni_id BIGINT NOT NULL,
    skill VARCHAR(100) NOT NULL,
    FOREIGN KEY (alumni_id) REFERENCES alumni_profiles(id) ON DELETE CASCADE,
    UNIQUE KEY unique_alumni_skill (alumni_id, skill),
    INDEX idx_skill (skill)
);

-- ============================================================
-- ALUMNI EXPERIENCE
-- ============================================================
CREATE TABLE alumni_experience (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    alumni_id BIGINT NOT NULL,
    company VARCHAR(200) NOT NULL,
    role VARCHAR(200) NOT NULL,
    start_date DATE,
    end_date DATE,
    is_current BOOLEAN DEFAULT FALSE,
    description TEXT,
    FOREIGN KEY (alumni_id) REFERENCES alumni_profiles(id) ON DELETE CASCADE
);

-- ============================================================
-- ALUMNI ACHIEVEMENTS
-- ============================================================
CREATE TABLE alumni_achievements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    alumni_id BIGINT NOT NULL,
    title VARCHAR(300) NOT NULL,
    description TEXT,
    achieved_at DATE,
    FOREIGN KEY (alumni_id) REFERENCES alumni_profiles(id) ON DELETE CASCADE
);

-- ============================================================
-- STUDENT PROFILES
-- ============================================================
CREATE TABLE student_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    avatar_url VARCHAR(500),
    roll_number VARCHAR(50) UNIQUE,
    enrollment_year INT,
    expected_graduation_year INT,
    branch VARCHAR(100),
    current_semester INT,
    bio TEXT,
    skills TEXT,
    resume_url VARCHAR(500),
    linkedin_url VARCHAR(500),
    cgpa DECIMAL(4,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================================
-- MENTORSHIP REQUESTS
-- ============================================================
CREATE TABLE mentorship_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    alumni_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    goal VARCHAR(500),
    status ENUM('PENDING','ACCEPTED','REJECTED','SCHEDULED','COMPLETED') DEFAULT 'PENDING',
    scheduled_at TIMESTAMP NULL,
    meeting_link VARCHAR(500),
    alumni_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES student_profiles(id) ON DELETE CASCADE,
    FOREIGN KEY (alumni_id) REFERENCES alumni_profiles(id) ON DELETE CASCADE,
    INDEX idx_status (status),
    INDEX idx_student (student_id),
    INDEX idx_alumni (alumni_id)
);

-- ============================================================
-- JOBS
-- ============================================================
CREATE TABLE jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    posted_by BIGINT NOT NULL,
    company_name VARCHAR(200) NOT NULL,
    job_title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    location VARCHAR(200),
    job_type ENUM('FULL_TIME','PART_TIME','INTERNSHIP','CONTRACT') DEFAULT 'FULL_TIME',
    experience_min INT DEFAULT 0,
    experience_max INT DEFAULT 5,
    salary_range VARCHAR(100),
    domain VARCHAR(100),
    application_deadline DATE,
    status ENUM('PENDING','APPROVED','REJECTED','CLOSED') DEFAULT 'PENDING',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (posted_by) REFERENCES alumni_profiles(id) ON DELETE CASCADE,
    INDEX idx_status (status),
    INDEX idx_domain (domain),
    INDEX idx_active (is_active)
);

-- ============================================================
-- JOB REQUIRED SKILLS
-- ============================================================
CREATE TABLE job_required_skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id BIGINT NOT NULL,
    skill VARCHAR(100) NOT NULL,
    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    INDEX idx_skill (skill)
);

-- ============================================================
-- JOB APPLICATIONS
-- ============================================================
CREATE TABLE job_applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    resume_url VARCHAR(500),
    cover_letter TEXT,
    match_score DECIMAL(5,2) DEFAULT 0,
    match_category ENUM('HIGH','MODERATE','LOW') DEFAULT 'LOW',
    matched_skills TEXT,
    status ENUM('APPLIED','SHORTLISTED','INTERVIEWED','SELECTED','REJECTED') DEFAULT 'APPLIED',
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES student_profiles(id) ON DELETE CASCADE,
    UNIQUE KEY unique_application (job_id, student_id),
    INDEX idx_match_score (match_score),
    INDEX idx_match_category (match_category)
);

-- ============================================================
-- RESUME DATA (parsed resume info)
-- ============================================================
CREATE TABLE resume_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    raw_text TEXT,
    extracted_skills TEXT,
    extracted_experience TEXT,
    extracted_education TEXT,
    extracted_job_title VARCHAR(200),
    extracted_company VARCHAR(200),
    parsed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================================
-- EVENTS
-- ============================================================
CREATE TABLE events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_by BIGINT NOT NULL,
    title VARCHAR(300) NOT NULL,
    description TEXT,
    event_type ENUM('WEBINAR','WORKSHOP','NETWORKING','SEMINAR','OTHER') DEFAULT 'OTHER',
    event_date TIMESTAMP NOT NULL,
    location VARCHAR(300),
    is_virtual BOOLEAN DEFAULT FALSE,
    meeting_link VARCHAR(500),
    max_participants INT,
    status ENUM('UPCOMING','ONGOING','COMPLETED','CANCELLED') DEFAULT 'UPCOMING',
    banner_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_event_date (event_date),
    INDEX idx_status (status)
);

-- ============================================================
-- EVENT REGISTRATIONS
-- ============================================================
CREATE TABLE event_registrations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    attended BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_registration (event_id, user_id)
);

-- ============================================================
-- STUDY MATERIALS
-- ============================================================
CREATE TABLE materials (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    uploaded_by BIGINT NOT NULL,
    title VARCHAR(300) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    file_url VARCHAR(500) NOT NULL,
    file_type VARCHAR(50),
    file_size BIGINT,
    download_count INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (uploaded_by) REFERENCES alumni_profiles(id) ON DELETE CASCADE,
    INDEX idx_category (category)
);

-- ============================================================
-- CONTRIBUTION HISTORY
-- ============================================================
CREATE TABLE contribution_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    alumni_id BIGINT NOT NULL,
    contribution_type ENUM('JOB_POSTED','MENTORSHIP_COMPLETED','EVENT_ATTENDED','MATERIAL_UPLOADED','EVENT_ORGANIZED') NOT NULL,
    reference_id BIGINT,
    points INT DEFAULT 0,
    description VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (alumni_id) REFERENCES alumni_profiles(id) ON DELETE CASCADE,
    INDEX idx_alumni (alumni_id),
    INDEX idx_type (contribution_type)
);

-- ============================================================
-- PROFILE VERIFICATION LOGS
-- ============================================================
CREATE TABLE profile_verification_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    alumni_id BIGINT NOT NULL,
    verified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    changes_made BOOLEAN DEFAULT FALSE,
    changed_fields TEXT,
    FOREIGN KEY (alumni_id) REFERENCES alumni_profiles(id) ON DELETE CASCADE,
    INDEX idx_alumni (alumni_id),
    INDEX idx_verified_at (verified_at)
);

-- ============================================================
-- NOTIFICATIONS
-- ============================================================
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(300) NOT NULL,
    message TEXT NOT NULL,
    type ENUM('MENTORSHIP','JOB','EVENT','SYSTEM','VERIFICATION') DEFAULT 'SYSTEM',
    is_read BOOLEAN DEFAULT FALSE,
    reference_id BIGINT,
    reference_type VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user (user_id),
    INDEX idx_read (is_read)
);

-- ============================================================
-- SAMPLE DATA
-- ============================================================

-- Admin user (password: Admin@123)
INSERT INTO users (email, password, role, is_active, is_verified) VALUES
('admin@alumni.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', TRUE, TRUE);

-- Sample Alumni users (password: Alumni@123)
INSERT INTO users (email, password, role, is_active, is_verified) VALUES
('rahul.sharma@alumni.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ALUMNI', TRUE, TRUE),
('priya.patel@alumni.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ALUMNI', TRUE, TRUE),
('arjun.mehta@alumni.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ALUMNI', TRUE, TRUE),
('sneha.reddy@alumni.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ALUMNI', TRUE, TRUE),
('vikram.nair@alumni.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ALUMNI', TRUE, FALSE);

-- Sample Students (password: Student@123)
INSERT INTO users (email, password, role, is_active, is_verified) VALUES
('aman.kumar@student.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'STUDENT', TRUE, TRUE),
('divya.singh@student.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'STUDENT', TRUE, TRUE),
('rohan.gupta@student.edu', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'STUDENT', TRUE, TRUE);

-- Alumni Profiles
INSERT INTO alumni_profiles (user_id, first_name, last_name, bio, graduation_year, degree, branch, current_company, current_role, domain, location, years_of_experience, available_for_mentorship, profile_completeness, last_verified_at, linkedin_url) VALUES
(2, 'Rahul', 'Sharma', 'Senior Software Engineer at Google with 8+ years of experience in distributed systems and cloud computing. Passionate about mentoring students.', 2016, 'B.Tech', 'Computer Science', 'Google', 'Senior Software Engineer', 'Software Engineering', 'Bangalore, India', 8, TRUE, 95, NOW(), 'https://linkedin.com/in/rahulsharma'),
(3, 'Priya', 'Patel', 'Product Manager at Microsoft. Love working at the intersection of technology and business. Happy to guide students on product management careers.', 2018, 'B.Tech', 'Information Technology', 'Microsoft', 'Senior Product Manager', 'Product Management', 'Hyderabad, India', 6, TRUE, 90, NOW(), 'https://linkedin.com/in/priyapatel'),
(4, 'Arjun', 'Mehta', 'Data Scientist at Amazon. Specializing in ML and AI. Research enthusiast with 3 published papers.', 2019, 'B.Tech', 'Computer Science', 'Amazon', 'Data Scientist', 'Data Science', 'Mumbai, India', 5, FALSE, 85, DATE_SUB(NOW(), INTERVAL 7 MONTH), 'https://linkedin.com/in/arjunmehta'),
(5, 'Sneha', 'Reddy', 'Full Stack Developer at Flipkart. React and Node.js expert. Open source contributor.', 2020, 'B.Tech', 'Electronics & Communication', 'Flipkart', 'Software Developer', 'Web Development', 'Pune, India', 4, TRUE, 80, NOW(), 'https://linkedin.com/in/snehareddy'),
(6, 'Vikram', 'Nair', 'DevOps Engineer at Infosys. Cloud infrastructure and CI/CD specialist.', 2021, 'B.Tech', 'Computer Science', 'Infosys', 'DevOps Engineer', 'DevOps', 'Chennai, India', 3, FALSE, 55, NULL, NULL);

-- Alumni Skills
INSERT INTO alumni_skills (alumni_id, skill) VALUES
(1, 'Java'), (1, 'Distributed Systems'), (1, 'Kubernetes'), (1, 'Go'), (1, 'System Design'), (1, 'Cloud Computing'),
(2, 'Product Management'), (2, 'Agile'), (2, 'Roadmapping'), (2, 'SQL'), (2, 'User Research'), (2, 'Figma'),
(3, 'Python'), (3, 'Machine Learning'), (3, 'TensorFlow'), (3, 'PyTorch'), (3, 'SQL'), (3, 'Statistics'),
(4, 'React'), (4, 'Node.js'), (4, 'JavaScript'), (4, 'MongoDB'), (4, 'TypeScript'), (4, 'Docker'),
(5, 'Docker'), (5, 'Kubernetes'), (5, 'AWS'), (5, 'CI/CD'), (5, 'Terraform'), (5, 'Linux');

-- Student Profiles
INSERT INTO student_profiles (user_id, first_name, last_name, roll_number, enrollment_year, expected_graduation_year, branch, current_semester, cgpa) VALUES
(7, 'Aman', 'Kumar', 'CS2021001', 2021, 2025, 'Computer Science', 7, 8.5),
(8, 'Divya', 'Singh', 'CS2022002', 2022, 2026, 'Information Technology', 5, 8.8),
(9, 'Rohan', 'Gupta', 'CS2021003', 2021, 2025, 'Computer Science', 7, 7.9);

-- Jobs
INSERT INTO jobs (posted_by, company_name, job_title, description, location, job_type, experience_min, experience_max, salary_range, domain, application_deadline, status) VALUES
(1, 'Google', 'Software Engineer L4', 'Join our team building next-gen distributed systems. You will work on infrastructure serving billions of users.', 'Bangalore / Remote', 'FULL_TIME', 2, 5, '25-40 LPA', 'Software Engineering', DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'APPROVED'),
(2, 'Microsoft', 'Associate Product Manager', 'Drive product strategy for Azure cloud services. Collaborate with engineering and design teams.', 'Hyderabad', 'FULL_TIME', 0, 2, '18-25 LPA', 'Product Management', DATE_ADD(CURDATE(), INTERVAL 45 DAY), 'APPROVED'),
(3, 'Amazon', 'Data Science Intern', 'Work on recommendation systems and forecasting models using ML. Great opportunity for final year students.', 'Bangalore', 'INTERNSHIP', 0, 1, '50-70K/month', 'Data Science', DATE_ADD(CURDATE(), INTERVAL 20 DAY), 'APPROVED'),
(4, 'Flipkart', 'Frontend Developer', 'Build scalable React applications for Flipkart Commerce. Work with a 10M+ user product.', 'Pune / Bangalore', 'FULL_TIME', 1, 3, '12-18 LPA', 'Web Development', DATE_ADD(CURDATE(), INTERVAL 25 DAY), 'APPROVED');

-- Job Required Skills
INSERT INTO job_required_skills (job_id, skill) VALUES
(1, 'Java'), (1, 'System Design'), (1, 'Distributed Systems'), (1, 'Algorithms'), (1, 'Data Structures'),
(2, 'Product Management'), (2, 'SQL'), (2, 'Agile'), (2, 'User Research'),
(3, 'Python'), (3, 'Machine Learning'), (3, 'SQL'), (3, 'Statistics'),
(4, 'React'), (4, 'JavaScript'), (4, 'TypeScript'), (4, 'Node.js');

-- Events
INSERT INTO events (created_by, title, description, event_type, event_date, location, is_virtual, max_participants, status) VALUES
(2, 'Tech Talk: Cloud Native Development', 'Deep dive into Kubernetes, microservices and cloud-native application development by Rahul Sharma from Google.', 'WEBINAR', DATE_ADD(NOW(), INTERVAL 7 DAY), 'Online', TRUE, 200, 'UPCOMING'),
(3, 'Product Management Career Workshop', 'Everything you need to know to break into Product Management — by Priya Patel, Senior PM at Microsoft.', 'WORKSHOP', DATE_ADD(NOW(), INTERVAL 14 DAY), 'College Auditorium', FALSE, 100, 'UPCOMING'),
(2, 'Annual Alumni Networking Night 2025', 'Connect with alumni across batches, industries and geographies. Virtual networking rooms by domain.', 'NETWORKING', DATE_ADD(NOW(), INTERVAL 21 DAY), 'Online', TRUE, 500, 'UPCOMING');

-- Contributions
INSERT INTO contribution_history (alumni_id, contribution_type, reference_id, points, description) VALUES
(1, 'JOB_POSTED', 1, 10, 'Posted Software Engineer role at Google'),
(1, 'MENTORSHIP_COMPLETED', NULL, 20, 'Completed mentorship session with student'),
(1, 'EVENT_ORGANIZED', 1, 15, 'Organized Cloud Native Tech Talk'),
(2, 'JOB_POSTED', 2, 10, 'Posted APM role at Microsoft'),
(2, 'EVENT_ORGANIZED', 2, 15, 'Organized PM Career Workshop'),
(3, 'JOB_POSTED', 3, 10, 'Posted Data Science Intern role at Amazon'),
(4, 'JOB_POSTED', 4, 10, 'Posted Frontend Developer role at Flipkart'),
(4, 'MENTORSHIP_COMPLETED', NULL, 20, 'Completed mentorship session');

-- Notifications
INSERT INTO notifications (user_id, title, message, type) VALUES
(7, 'New Job: Software Engineer at Google', 'Rahul Sharma posted a new Software Engineer role at Google. Check it out!', 'JOB'),
(8, 'Tech Talk Webinar Next Week', 'Cloud Native Development webinar is scheduled in 7 days. Register now!', 'EVENT');
