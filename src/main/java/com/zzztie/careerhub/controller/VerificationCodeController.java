package com.zzztie.careerhub.controller;

import com.zzztie.careerhub.dto.ApiResponse;
import com.zzztie.careerhub.service.VerificationCodeService;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/verification")
public class VerificationCodeController {

    private final VerificationCodeService verificationCodeService;

    public VerificationCodeController(
            VerificationCodeService verificationCodeService
    ) {
        this.verificationCodeService = verificationCodeService;
    }

    @PostMapping("/email/send")
    public ApiResponse<String> sendEmailCode(
            @RequestParam String email
    ) {
        verificationCodeService.sendEmailCode(email);
        return new ApiResponse<>(
                200,
                "success",
                "verification code sent"
        );
    }

    @PostMapping("/email/verify")
    public ApiResponse<Boolean> verifyEmailCode(
            @RequestParam String email,
            @RequestParam String code
    ) {

        boolean verified =
                verificationCodeService.verifyEmailCode(
                        email,
                        code
                );

        return new ApiResponse<>(
                200,
                "success",
                verified
        );
    }
}
