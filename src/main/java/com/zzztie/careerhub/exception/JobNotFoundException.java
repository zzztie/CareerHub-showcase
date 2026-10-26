package com.zzztie.careerhub.exception;

public class JobNotFoundException extends RuntimeException {
    public JobNotFoundException(Long id) {
        super("job not found"+id);
    }
}
