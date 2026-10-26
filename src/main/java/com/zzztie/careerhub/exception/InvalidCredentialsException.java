package com.zzztie.careerhub.exception;

public class InvalidCredentialsException extends RuntimeException{
    public InvalidCredentialsException(){
        super("email or password is incorrect");
    }
}
