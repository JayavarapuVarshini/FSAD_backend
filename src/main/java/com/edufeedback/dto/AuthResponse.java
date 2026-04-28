package com.edufeedback.dto;

public class AuthResponse {
    private String token;
    private String email;
    private String fullName;
    private String role;
    private Long id;
    private boolean emailVerified;
    private String message;

    public AuthResponse() {}

    public AuthResponse(String token, String email, String fullName,
                        String role, Long id, boolean emailVerified) {
        this.token = token;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.id = id;
        this.emailVerified = emailVerified;
    }

    public AuthResponse(String token, String email, String fullName,
                        String role, Long id, boolean emailVerified, String message) {
        this(token, email, fullName, role, id, emailVerified);
        this.message = message;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
