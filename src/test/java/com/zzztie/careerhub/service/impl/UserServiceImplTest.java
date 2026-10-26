package com.zzztie.careerhub.service.impl;

import com.zzztie.careerhub.domain.User;
import com.zzztie.careerhub.enums.UserStatus;
import com.zzztie.careerhub.exception.UserNotFoundException;
import com.zzztie.careerhub.mapper.UserMapper;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {
    @Test
    void createUser_shouldSetDefaultStatusAndTimestamps() {
        // ① 模拟 Mapper
        UserMapper userMapper = mock(UserMapper.class);
        // ② 创建真实的 Service
        UserServiceImpl service =
                new UserServiceImpl(userMapper);
        // ③ 准备创建用户的数据
        User user = new User();
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        // ④ 执行真实业务
        User result = service.createUser(user);
        // ⑤ 捕获传给 Mapper 的 User
        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);
        verify(userMapper, times(1))
                .insert(captor.capture());
        User insertedUser = captor.getValue();
        // ⑥ 检查用户状态
        assertEquals(
                UserStatus.ACTIVE,
                insertedUser.getStatus()
        );
        // ⑦ 检查时间字段
        assertNotNull(insertedUser.getCreateTime());
        assertNotNull(insertedUser.getUpdateTime());
        assertEquals(
                insertedUser.getCreateTime(),
                insertedUser.getUpdateTime()
        );
        // ⑧ 验证返回的是传入的 User
        assertSame(user, result);
    }
    @Test
    void getUserById_whenNotFound_shouldThrowsException(){
        UserMapper userMapper = mock(UserMapper.class);
        // ② 配置查询不到用户
        when(userMapper.selectById(999999L)).thenReturn(null);

        UserServiceImpl service = new UserServiceImpl(userMapper);
        assertThrows(
                UserNotFoundException.class,
                () -> service.getUserById(999999L)
        );
        // ⑤ 验证查询执行了一次
        verify(userMapper,times(1)).selectById(999999L);
        // ⑥ 验证没有其他 Mapper 调用
        verifyNoMoreInteractions(userMapper);

    }
    @Test
    void getUserById_whenExists_shouldReturnUser(){
        UserMapper userMapper = mock(UserMapper.class);

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("alice");
        existingUser.setEmail("alice@example.com");

        when(userMapper.selectById(1L)).thenReturn(existingUser);

        UserServiceImpl service = new UserServiceImpl(userMapper
        );
        User result = service.getUserById(1L);

        assertEquals(1L,result.getId());
        assertEquals("alice",result.getUsername());

        assertSame(existingUser,result);

        verify(userMapper,times(1)).selectById(1L);
        verifyNoMoreInteractions(userMapper);
    }
    @Test
    void updateUser_whenExists_shouldUpdateUser(){
        UserMapper userMapper = mock(UserMapper.class);
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("alice");
        when(userMapper.selectById(1L)).thenReturn(existingUser);
        User updateUser = new User();
        updateUser.setId(1L);
        updateUser.setUsername("alice_new");

        when(userMapper.updateById(updateUser)).thenReturn(1);

        UserServiceImpl service = new UserServiceImpl(userMapper);
        User result = service.updateUser(updateUser);

        assertSame(updateUser,result);
        assertEquals("alice_new",result.getUsername());

        assertNotNull(result.getUpdateTime());

        verify(userMapper,times(1)).selectById(1L);

        verify(userMapper,times(1)).updateById(updateUser);
        verifyNoMoreInteractions(userMapper);
    }
    @Test
    void updateUser_whenNotFound_shouldThrowException(){
        UserMapper userMapper = mock(UserMapper.class);

        User updateUser = new User();
        updateUser.setUsername("alice_new");
        updateUser.setId(999999L);
        when(userMapper.selectById(999999L)).thenReturn(null);
        UserServiceImpl service = new UserServiceImpl(userMapper);

        assertThrows(
                UserNotFoundException.class,
                () -> service.updateUser(updateUser)
        );
        // 应该查询一次，确认用户不存在
        verify(userMapper, times(1))
                .selectById(updateUser.getId());
        // 用户不存在，不允许更新
        verify(userMapper, never())
                .updateById(any(User.class));
        verifyNoMoreInteractions(userMapper);
    }
    @Test
    void deleteUser_whenExists_shouldReturnTrue(){
        UserMapper userMapper = mock(UserMapper.class);
        when(userMapper.deleteById(1L)).thenReturn(1);
        UserServiceImpl service = new UserServiceImpl(userMapper);
        boolean result = service.deleteUser(1L);
        assertTrue(result);
        verify(userMapper,times(1)).deleteById(1L);
        verifyNoMoreInteractions(userMapper);
    }
    @Test
    void deleteUser_whenNotFound_shouldThrowException(){
        UserMapper userMapper = mock(UserMapper.class);

        when(userMapper.deleteById(999999L)).thenReturn(0);
        UserServiceImpl service = new UserServiceImpl(userMapper);

        assertThrows(
                UserNotFoundException.class ,
                () -> service.deleteUser(999999L)
        );
        verify(userMapper,times(1)).deleteById(999999L);
        verifyNoMoreInteractions(userMapper);
    }
}