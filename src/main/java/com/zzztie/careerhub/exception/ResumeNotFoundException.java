package com.zzztie.careerhub.exception;

public class ResumeNotFoundException extends RuntimeException {
    public ResumeNotFoundException(Long id)
    {
        super("resume not found "+id);
    }
}
