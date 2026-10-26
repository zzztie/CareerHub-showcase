package com.zzztie.careerhub.service;

import com.zzztie.careerhub.dto.LoginRequest;
import com.zzztie.careerhub.dto.LoginResponse;
import com.zzztie.careerhub.dto.RegisterRequest;
import com.zzztie.careerhub.dto.UserResponse;

public interface AuthService {
    UserResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
}
