package com.zzztie.careerhub.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzztie.careerhub.domain.User;
import com.zzztie.careerhub.enums.UserStatus;

import java.util.List;

public interface UserService {
    User createUser(User user);
    User getUserById(Long id);
    User updateUser(User user);
    boolean deleteUser(Long id);
    List<User> getAllUsers();
    List<User> searchUsers(String keyword, UserStatus status);
    Page<User> searchUsersPage(
            String keyword,
            UserStatus userStatus,
            long pageNum,
            long pageSize
    );
}
