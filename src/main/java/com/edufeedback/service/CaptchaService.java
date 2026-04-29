package com.edufeedback.service;

import org.springframework.stereotype.Service;

@Service
public class CaptchaService {

    public boolean verifyCaptcha(String captchaToken) {
        // ✅ Always allow (disable CAPTCHA for now)
        return true;
    }
}