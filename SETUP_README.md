# EduFeedback Backend - Setup Guide (Spring Boot + MySQL)

## 📁 Project Structure
```
feedback-backend/
├── src/main/java/com/edufeedback/
│   ├── FeedbackBackendApplication.java     ← Main entry point
│   ├── controller/
│   │   ├── AuthController.java             ← /api/auth/*
│   │   ├── AdminController.java            ← /api/admin/*
│   │   └── StudentController.java          ← /api/student/*
│   ├── service/
│   │   ├── AuthService.java                ← Register, Login logic
│   │   ├── OtpService.java                 ← OTP generate/verify
│   │   ├── EmailService.java               ← Gmail SMTP sender
│   │   ├── CaptchaService.java             ← Google reCAPTCHA verify
│   │   └── FeedbackService.java            ← Forms, responses, analytics
│   ├── model/                              ← JPA entities (auto-create tables)
│   ├── repository/                         ← Spring Data JPA repos
│   ├── dto/                                ← Request/Response objects
│   ├── security/JwtAuthenticationFilter   ← JWT filter
│   ├── config/SecurityConfig               ← CORS, security rules
│   └── util/JwtUtil                        ← JWT generate/validate
└── src/main/resources/
    ├── application.properties              ← ⚠️ CONFIGURE THIS
    └── schema.sql                          ← MySQL schema reference
```

## 🛠️ Step-by-Step Setup

### Step 1: Create MySQL Database
Open MySQL Workbench or CLI and run:
```sql
CREATE DATABASE edufeedback_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Step 2: Configure application.properties
Edit `src/main/resources/application.properties`:

```properties
# MySQL - Change password to your MySQL root password
spring.datasource.url=jdbc:mysql://localhost:3306/edufeedback_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD

# Gmail SMTP - Use your Gmail + App Password
# Get App Password: Google Account → Security → 2-Step Verification → App Passwords
spring.mail.username=your_gmail@gmail.com
spring.mail.password=your_16_char_app_password

# reCAPTCHA - Get from https://www.google.com/recaptcha/admin
recaptcha.secret.key=YOUR_RECAPTCHA_SECRET_KEY
```

### Step 3: Import into Eclipse
1. Open Eclipse → File → Import → Maven → Existing Maven Projects
2. Browse to the `feedback-backend` folder
3. Click Finish
4. Wait for Maven to download all dependencies (~2 min)

### Step 4: Run the Application
- Right-click `FeedbackBackendApplication.java` → Run As → Java Application
- Server starts at: http://localhost:8080

### Step 5: Verify it works
Open browser: http://localhost:8080/api/auth/login
You should see a 405 Method Not Allowed (correct — it expects POST)

## 🔑 API Endpoints Summary

| Method | URL | Auth | Description |
|--------|-----|------|-------------|
| POST | /api/auth/register | None | Register + send OTP |
| POST | /api/auth/verify-email | None | Verify OTP |
| POST | /api/auth/resend-otp | None | Resend OTP |
| POST | /api/auth/login | None | Login → get JWT token |
| GET | /api/admin/forms | Admin JWT | Get all forms |
| POST | /api/admin/forms | Admin JWT | Create form |
| PUT | /api/admin/forms/{id}/toggle-status | Admin JWT | Toggle active/inactive |
| DELETE | /api/admin/forms/{id} | Admin JWT | Delete form |
| GET | /api/admin/analytics | Admin JWT | Dashboard analytics |
| GET | /api/student/forms | Student JWT | Get active forms |
| POST | /api/student/submit-feedback | Student JWT | Submit feedback |
| GET | /api/student/submitted-forms | Student JWT | Get submitted form IDs |

## ⚙️ How Email OTP Works
1. User registers → OTP generated (6-digit) → stored in `otp_tokens` table
2. Email sent via Gmail SMTP with HTML template
3. User enters OTP → verified against DB → `email_verified = true` in `users` table
4. OTPs expire in 10 minutes and are cleaned up automatically

## 🔒 How CAPTCHA Works
1. Frontend renders Google reCAPTCHA v2 widget
2. User solves CAPTCHA → gets a token
3. Token sent to backend with login/register request
4. Backend calls Google's verify API with your secret key
5. If Google says token is valid → proceed; else → reject

## 🗄️ Database Tables (auto-created by JPA)
- `users` — Student and admin accounts
- `otp_tokens` — OTP codes with expiry
- `feedback_forms` — Forms created by admins
- `feedback_questions` — Questions per form
- `feedback_responses` — Student submissions
- `feedback_answers` — Per-question answers
