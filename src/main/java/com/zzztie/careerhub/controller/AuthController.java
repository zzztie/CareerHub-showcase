package com.zzztie.careerhub.controller;

import com.mysql.cj.log.Log;
import com.zzztie.careerhub.dto.*;
import com.zzztie.careerhub.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication",description = "APIs for registration and login")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    @PostMapping("/register")
    @Operation(summary = "Register user")
    public ApiResponse<UserResponse> register(
            @Valid
            @RequestBody RegisterRequest request
            ){
        UserResponse response = authService.register(request);
        return new ApiResponse<>(
                200,
                "success",
                response
        );
    }
    @PostMapping("/login")
    @Operation(summary = "Login user")
    public ApiResponse<LoginResponse> login (
            @Valid
            @RequestBody LoginRequest request
            ){
        LoginResponse response = authService.login(request);
        return new ApiResponse<>(
                200,
                "success",
                response
        );
    }

}
