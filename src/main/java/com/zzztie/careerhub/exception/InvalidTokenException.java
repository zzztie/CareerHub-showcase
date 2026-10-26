package com.zzztie.careerhub.exception;

public class InvalidTokenException extends RuntimeException{
    public InvalidTokenException(){
        super("invalid or expired token");
    }
}
