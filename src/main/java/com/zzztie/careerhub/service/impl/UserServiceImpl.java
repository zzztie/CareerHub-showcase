package com.zzztie.careerhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzztie.careerhub.domain.User;
import com.zzztie.careerhub.enums.UserStatus;
import com.zzztie.careerhub.exception.UserNotFoundException;
import com.zzztie.careerhub.mapper.UserMapper;
import com.zzztie.careerhub.service.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@Service
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User createUser(User user) {
        user.setStatus(UserStatus.ACTIVE);
        LocalDateTime now = LocalDateTime.now();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        userMapper.insert(user);
        return user;
    }

    @Override
    public User getUserById(Long id) {
        User user = userMapper.selectById(id);
        if(user == null){
            throw new UserNotFoundException(id);
        }
        return user;
    }

    @Override
    public User updateUser(User user) {
        User existingUser = userMapper.selectById(user.getId());
        if(existingUser == null){
            throw new UserNotFoundException(user.getId());
        }
        user.setUpdateTime(LocalDateTime.now());

        userMapper.updateById(user);
        return user;
    }

    @Override
    public boolean deleteUser(Long id) {
        int rows = userMapper.deleteById(id);
        if(rows == 0){
            throw new UserNotFoundException(id);
        }
        return true;
    }

    @Override
    public List<User> getAllUsers() {
        return userMapper.selectList(null);
    }

    @Override
    public List<User> searchUsers(String keyword, UserStatus status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null,
                User :: getStatus,
                status)
                .like(keyword != null && ! keyword.isBlank(),
                        User :: getUsername,keyword)
                .orderByDesc(User :: getId);
        return userMapper.selectList(wrapper);
    }

    @Override
    public Page<User> searchUsersPage(String keyword, UserStatus userStatus, long pageNum, long pageSize) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(userStatus != null,
                User :: getStatus,userStatus)
                .like(keyword!=null && !keyword.isBlank(),
                        User :: getUsername,keyword)
                .orderByDesc(User :: getId);
        Page<User> page = new Page<>(pageNum,pageSize);
        return userMapper.selectPage(page,wrapper);
    }
}
