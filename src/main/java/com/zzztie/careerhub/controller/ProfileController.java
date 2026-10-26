package com.zzztie.careerhub.controller;

import com.zzztie.careerhub.context.UserContext;
import com.zzztie.careerhub.domain.User;
import com.zzztie.careerhub.dto.ApiResponse;
import com.zzztie.careerhub.dto.UserResponse;
import com.zzztie.careerhub.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@Tag(name = "User Profile",description = "APIs for current authenticated user")
public class ProfileController {
    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }
    @GetMapping("/profile")
    @Operation(summary = "Get current user profile")
    public ApiResponse<UserResponse> getprofile(){
        Long userId = UserContext.getUserId();
        User user = userService.getUserById(userId);
        UserResponse response = new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getStatus()
        );
        return new ApiResponse<>(
                200,
                "success",
                response
        );
    }
}
