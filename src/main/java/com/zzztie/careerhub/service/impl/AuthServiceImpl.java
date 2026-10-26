package com.zzztie.careerhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzztie.careerhub.domain.User;
import com.zzztie.careerhub.dto.*;
import com.zzztie.careerhub.enums.UserStatus;
import com.zzztie.careerhub.exception.InvalidCredentialsException;
import com.zzztie.careerhub.mapper.UserMapper;
import com.zzztie.careerhub.service.AuthService;
import com.zzztie.careerhub.service.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder,JwtService jwtService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public UserResponse register(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.ACTIVE);
        userMapper.insert(user);
        UserResponse response = new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getStatus()
        );
        return response;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = authenticate(request);
        String token = jwtService.generateToken(user.getId());
        return new LoginResponse(token);
    }

    private User authenticate(LoginRequest request){
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User :: getEmail,request.getEmail());
        User user = userMapper.selectOne(wrapper);
        if(user == null){
            throw new InvalidCredentialsException();
        }
        boolean mached = passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash()
        );
        if(!mached){
            throw new InvalidCredentialsException();
        }
        return user;
    }

}
