package com.zzztie.careerhub.service;

public interface VerificationCodeService {
    void sendEmailCode(String email);
    boolean verifyEmailCode(String email, String code);
}