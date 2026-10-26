package com.zzztie.careerhub.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzztie.careerhub.domain.User;
import com.zzztie.careerhub.dto.ApiResponse;
import com.zzztie.careerhub.dto.CreateUserRequest;
import com.zzztie.careerhub.dto.UpdateUserRequest;
import com.zzztie.careerhub.dto.UserResponse;
import com.zzztie.careerhub.enums.UserStatus;
import com.zzztie.careerhub.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(
        name = "User Management",
        description = "APIs for user creation,query,update,deletion and pagination"
)
@RestController
@ConditionalOnProperty(
        prefix = "careerhub",
        name = "user-management-enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    @GetMapping("/users/count")
    public int countUsers(){
        return userService.getAllUsers().size();
    }

    @Operation(
            summary = "Create user",
            description = "Create a new user with username and email"
    )
    @PostMapping("/users")
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request){
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        User savedUser = userService.createUser(user);
        UserResponse response = new UserResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getStatus()
        );
        return new ApiResponse<>(
                200,
                "success",
                response
        );
    }
    @Operation(summary = "Get user by ID")
    @GetMapping("/users/{id}")
    public ApiResponse<UserResponse> getUserById(
            @Positive(message = "id must be greater than 0")
            @PathVariable Long id){
        User user = userService.getUserById(id);
        UserResponse response = new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getStatus());
        return new ApiResponse<>(
                200,
                "success",
                response
        );
    }
    @Operation(summary = "Get all user")
    @GetMapping("/users")
    public ApiResponse<List<UserResponse>> getAllUsers(){
        List<User> users = userService.getAllUsers();
        List<UserResponse> responses = users.stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getStatus()
                ))
                .toList();
        return new ApiResponse<>(
                200,
                "success",
                responses
        );
    }
    @Operation(summary = "Update user ")
    @PutMapping("/users/{id}")
    public ApiResponse<UserResponse> updateUser(
            @Positive(message = "id must be greater than 0")
            @PathVariable Long id,
            @Valid
            @RequestBody UpdateUserRequest request
            ){
        User user = userService.getUserById(id);
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        User updateUser = userService.updateUser(user);
        UserResponse response = new UserResponse(
                updateUser.getId(),
                updateUser.getUsername(),
                updateUser.getEmail(),
                updateUser.getStatus()
        );
        return new ApiResponse<>(
                200,
                "success",
                response
        );
    }
    @Operation(summary = "Delete user ")
    @DeleteMapping("/users/{id}")
    public ApiResponse<Boolean> deleteUser(
            @Positive(message = "id must be greater than 0")
            @PathVariable Long id){
        boolean deleted = userService.deleteUser(id);

        return new ApiResponse<>(
                200,
                "success",
                deleted
        );
    }

    @Operation(summary = "Search users")
    @GetMapping("/users/search")
    public ApiResponse<List<UserResponse>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false)UserStatus userStatus){

        List<User> users = userService.searchUsers(keyword,userStatus);

        List<UserResponse> responses =  users.stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getStatus()
                ))
                .toList();

        return new ApiResponse<>(
                200,
                "success",
                responses
        );
    }

    @Operation(summary = "Get user by page")
    @GetMapping("/users/page")
    public ApiResponse<IPage<UserResponse>> searchUsersPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserStatus userStatus,

            @Min(value = 1,message = "pageNum must be at least 1")
            @RequestParam long pageNum,
            @Min(value = 1, message = "pageSize must be at least 1")
            @Max(value = 100,message = "pageSize must not exceed 100")
            @RequestParam long pageSize){

        Page<User> page = userService.searchUsersPage(
                keyword,
                userStatus,
                pageNum,
                pageSize
        );
        IPage<UserResponse> responseIPage =
                page.convert(user -> new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getStatus()
        ));
        return new ApiResponse<>(
                200,
                "success",
                responseIPage
        );
    }
}
