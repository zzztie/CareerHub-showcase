package com.zzztie.careerhub.dto;

import com.zzztie.careerhub.enums.UserStatus;

public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private UserStatus status;

    public UserResponse(Long id, String username, String email, UserStatus status) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public UserStatus getStatus() {
        return status;
    }
}
