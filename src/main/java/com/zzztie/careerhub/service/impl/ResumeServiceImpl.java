package com.zzztie.careerhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzztie.careerhub.context.UserContext;
import com.zzztie.careerhub.domain.Resume;
import com.zzztie.careerhub.exception.ResumeNotFoundException;
import com.zzztie.careerhub.mapper.ResumeMapper;
import com.zzztie.careerhub.service.ResumeService;
import com.zzztie.careerhub.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/** 简历的访问、上传与删除都先核对当前登录用户，不能只相信请求里的 userId。 */
@Service
public class ResumeServiceImpl implements ResumeService {
    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    private final ResumeMapper resumeMapper;
    private final UserService userService;

    public ResumeServiceImpl(ResumeMapper resumeMapper, UserService userService) {
        this.resumeMapper = resumeMapper;
        this.userService = userService;
    }

    private void checkResumeOwner(Long userId) {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null || !Objects.equals(currentUserId, userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not have permission to access this resume"
            );
        }
    }

    @Override
    public Resume createResume(Resume resume) {
        checkResumeOwner(resume.getUserId());
        userService.getUserById(resume.getUserId());
        LocalDateTime now = LocalDateTime.now();
        resume.setCreateTime(now);
        resume.setUpdateTime(now);
        resumeMapper.insert(resume);
        return resume;
    }

    @Override
    public Resume getResumeById(Long id) {
        Resume resume = resumeMapper.selectById(id);
        if (resume == null) {
            throw new ResumeNotFoundException(id);
        }
        checkResumeOwner(resume.getUserId());
        return resume;
    }

    @Override
    public List<Resume> getResumeByUserId(Long userId) {
        checkResumeOwner(userId);
        userService.getUserById(userId);
        LambdaQueryWrapper<Resume> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Resume::getUserId, userId);
        return resumeMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public boolean deleteResume(Long id) {
        // 通过统一读取方法先验证简历归属，避免按任意 ID 删除其他用户的文件。
        Resume resume = getResumeById(id);
        int rows = resumeMapper.deleteById(id);
        if (rows == 0) {
            throw new ResumeNotFoundException(id);
        }
        // 文件系统不参加数据库事务；删除文件失败时抛异常，让数据库删除回滚。
        Path filePath = Paths.get(resume.getFilepath());
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException(
                    "failed to delete resume file", e
            );
        }
        return true;
    }

    @Override
    public Resume uploadResume(Long userId, MultipartFile file) {
        checkResumeOwner(userId);
        userService.getUserById(userId);

        if (file.isEmpty()) {
            throw new IllegalArgumentException("resume file cannot be empty");
        }
        String originalFileName = file.getOriginalFilename();
        String contentType = file.getContentType();

        if (originalFileName == null || !originalFileName.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new IllegalArgumentException("resume file must be PDF");
        }

        if (!"application/pdf".equals(contentType)) {
            throw new IllegalArgumentException("resume file must be PDF");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("resume file size cannot exceed 5MB");
        }

        String storedFilename = UUID.randomUUID() + ".pdf";

        // 磁盘文件名由服务端生成，不直接使用上传者提供的文件名作为路径。
        Path uploadDir = Paths.get("uploads", "resumes");

        Path targetPath = uploadDir.resolve(storedFilename);
        try {
            Files.createDirectories(uploadDir);
            file.transferTo(targetPath);
        } catch (IOException e) {
            throw new RuntimeException("failed to save resume file", e);
        }
        Resume resume = new Resume();
        resume.setUserId(userId);
        resume.setFilename(originalFileName);
        resume.setFilepath(targetPath.toString());
        return createResume(resume);
    }
}
