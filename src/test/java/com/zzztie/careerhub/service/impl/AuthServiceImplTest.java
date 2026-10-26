package com.zzztie.careerhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzztie.careerhub.domain.User;
import com.zzztie.careerhub.dto.LoginRequest;
import com.zzztie.careerhub.dto.LoginResponse;
import com.zzztie.careerhub.dto.RegisterRequest;
import com.zzztie.careerhub.dto.UserResponse;
import com.zzztie.careerhub.enums.UserStatus;
import com.zzztie.careerhub.exception.InvalidCredentialsException;
import com.zzztie.careerhub.mapper.UserMapper;
import com.zzztie.careerhub.service.JwtService;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {

    @Test
    void register_shouldEncodePasswordAndInsertUser() {
        // ① 创建模拟依赖
        UserMapper userMapper = mock(UserMapper.class);
        PasswordEncoder passwordEncoder =
                mock(PasswordEncoder.class);
        JwtService jwtService = mock(JwtService.class);
        // ② 模拟注册请求
        RegisterRequest request = mock(RegisterRequest.class);
        when(request.getUsername()).thenReturn("alice");
        when(request.getEmail()).thenReturn("alice@example.com");
        when(request.getPassword()).thenReturn("example-login-password");
        // ③ 模拟密码加密结果
        when(passwordEncoder.encode("example-login-password"))
                .thenReturn("encoded_password_example-login-password");
        // ④ 创建真实 AuthService
        AuthServiceImpl service = new AuthServiceImpl(
                userMapper,
                passwordEncoder,
                jwtService
        );
        // ⑤ 执行真实注册业务
        UserResponse result = service.register(request);
        // ⑥ 捕获插入数据库的 User
        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);
        verify(userMapper, times(1))
                .insert(captor.capture());
        User insertedUser = captor.getValue();
        // ⑦ 检查用户字段
        assertEquals("alice", insertedUser.getUsername());
        assertEquals(
                "alice@example.com",
                insertedUser.getEmail()
        );
        assertEquals(UserStatus.ACTIVE, insertedUser.getStatus());
        // ⑧ 检查保存的是编码后的密码
        assertEquals(
                "encoded_password_example-login-password",
                insertedUser.getPasswordHash()
        );
        assertNotEquals(
                "example-login-password",
                insertedUser.getPasswordHash()
        );
        // ⑨ 验证密码编码方法调用一次
        verify(passwordEncoder, times(1))
                .encode("example-login-password");
        // ⑩ 注册返回响应对象
        assertNotNull(result);
        // 注册阶段没有使用 JWT
        verifyNoInteractions(jwtService);
    }
    @Test
    void login_whenCredentialsValid_shouldGenerateToken() {
        // ① 创建模拟依赖
        UserMapper userMapper = mock(UserMapper.class);
        PasswordEncoder passwordEncoder =
                mock(PasswordEncoder.class);
        JwtService jwtService = mock(JwtService.class);
        // ② 模拟登录请求
        LoginRequest request = mock(LoginRequest.class);
        when(request.getEmail())
                .thenReturn("alice@example.com");
        when(request.getPassword())
                .thenReturn("example-login-password");
        // ③ 模拟 MySQL 中存在该用户
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("alice@example.com");
        existingUser.setPasswordHash("hashed_password");
        when(userMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(existingUser);
        // ④ 模拟密码校验成功
        when(passwordEncoder.matches(
                "example-login-password", "hashed_password"
        )).thenReturn(true);
        // ⑤ 模拟 JWT 生成结果
        when(jwtService.generateToken(1L))
                .thenReturn("fake-jwt-token");
        // ⑥ 创建真实 Service
        AuthServiceImpl service = new AuthServiceImpl(
                userMapper,
                passwordEncoder,
                jwtService
        );
        // ⑦ 执行真实登录业务
        LoginResponse result = service.login(request);
        // ⑧ 验证返回了登录响应
        assertNotNull(result);
        // ⑨ 验证 MySQL 查询一次
        verify(userMapper, times(1))
                .selectOne(any(LambdaQueryWrapper.class));
        // ⑩ 验证密码校验一次
        verify(passwordEncoder, times(1))
                .matches("example-login-password", "hashed_password");
        // ⑪ 验证使用用户 ID 生成 JWT
        verify(jwtService, times(1))
                .generateToken(1L);
    }
    @Test
    void login_whenPasswordWrong_shouldThrowAndNotGenerateToken() {
        // ① 创建模拟依赖
        UserMapper userMapper = mock(UserMapper.class);
        PasswordEncoder passwordEncoder =
                mock(PasswordEncoder.class);
        JwtService jwtService = mock(JwtService.class);
        // ② 模拟登录请求
        LoginRequest request = mock(LoginRequest.class);
        when(request.getEmail())
                .thenReturn("alice@example.com");
        when(request.getPassword())
                .thenReturn("wrong_password");
        // ③ 模拟数据库中存在该用户
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("alice@example.com");
        existingUser.setPasswordHash("hashed_password");
        when(userMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(existingUser);
        // ④ 模拟密码校验失败
        when(passwordEncoder.matches(
                "wrong_password",
                "hashed_password"
        )).thenReturn(false);
        // ⑤ 创建真实 Service
        AuthServiceImpl service = new AuthServiceImpl(
                userMapper,
                passwordEncoder,
                jwtService
        );
        // ⑥ 验证登录抛出异常
        assertThrows(
                InvalidCredentialsException.class,
                () -> service.login(request)
        );
        // ⑦ 验证查询执行一次
        verify(userMapper, times(1))
                .selectOne(any(LambdaQueryWrapper.class));
        // ⑧ 验证密码校验执行一次
        verify(passwordEncoder, times(1))
                .matches("wrong_password", "hashed_password");
        // ⑨ 验证没有调用 JWT 服务
        verifyNoInteractions(jwtService);
    }
}