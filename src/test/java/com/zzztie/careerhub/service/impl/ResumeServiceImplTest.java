package com.zzztie.careerhub.service.impl;

import com.zzztie.careerhub.context.UserContext;
import com.zzztie.careerhub.domain.User;
import com.zzztie.careerhub.mapper.ResumeMapper;
import com.zzztie.careerhub.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
public class ResumeServiceImplTest {
    @Test
    void uploadResume_whenFileEmpty_shouldThrowAndNotSave() throws IOException {
        // ① 创建模拟依赖
        ResumeMapper resumeMapper = mock(ResumeMapper.class);
        UserService userService = mock(UserService.class);
        MultipartFile file = mock(MultipartFile.class);
        Long userId = 1L;
        // ② 模拟用户存在
        User user = new User();
        user.setId(userId);
        when(userService.getUserById(userId))
                .thenReturn(user);
        // ③ 模拟上传文件为空
        when(file.isEmpty()).thenReturn(true);
        // ④ 创建真实 Service
        ResumeServiceImpl service =
                new ResumeServiceImpl(resumeMapper, userService);
        // ⑤ 模拟当前登录用户
        try (MockedStatic<UserContext> mockedContext =
                     mockStatic(UserContext.class)) {
            mockedContext.when(UserContext::getUserId)
                    .thenReturn(userId);
            // ⑥ 验证空文件抛出异常
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> service.uploadResume(userId, file)
            );
            assertEquals(
                    "resume file cannot be empty",
                    exception.getMessage()
            );
            mockedContext.verify(
                    UserContext::getUserId,
                    times(1)
            );
        }
        // ⑦ 确实检查过用户
        verify(userService, times(1))
                .getUserById(userId);
        // ⑧ 确实检查过文件是否为空
        verify(file, times(1))
                .isEmpty();
        // ⑨ 空文件不允许写入磁盘
        verify(file, never())
                .transferTo(any(Path.class));
        // ⑩ 空文件不允许操作简历数据库
        verifyNoInteractions(resumeMapper);
    }
}
