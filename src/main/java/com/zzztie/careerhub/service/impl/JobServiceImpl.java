package com.zzztie.careerhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzztie.careerhub.domain.Job;
import com.zzztie.careerhub.enums.JobStatus;
import com.zzztie.careerhub.exception.JobNotFoundException;
import com.zzztie.careerhub.mapper.JobMapper;
import com.zzztie.careerhub.service.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 岗位详情采用旁路缓存：MySQL 保存事实，Redis 加速按 ID 查询。
 * 读取缓存失败时回退到 MySQL；岗位变更后尝试删除旧缓存。
 */
@Service
public class JobServiceImpl implements JobService {
    private static final Logger log = LoggerFactory.getLogger(JobServiceImpl.class);
    private static final String JOB_DETAIL_KEY_PREFIX = "job:detail:";

    private final JobMapper jobMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private void invalidateJobCache(Long jobId) {
        String key = buildJobDetailKey(jobId);
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                Boolean deleted = stringRedisTemplate.delete(key);
                log.info(
                        "event=job_cache.invalidated key={} deleted={}",
                        key,
                        deleted
                );
                return;
            } catch (DataAccessException e) {
                log.warn(
                        "event=job_cache.invalidation_failed key={} attempt={}",
                        key,
                        attempt,
                        e
                );
                if (attempt == 3) {
                    log.error(
                            "event=job_cache.invalidation_exhausted key={} attempts=3",
                            key
                    );
                    throw e;
                }
            }
        }
    }

    public JobServiceImpl(JobMapper jobMapper, ObjectMapper objectMapper,
                          StringRedisTemplate stringRedisTemplate) {
        this.jobMapper = jobMapper;
        this.objectMapper = objectMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public String buildJobDetailKey(Long id) {
        return JOB_DETAIL_KEY_PREFIX + id;
    }

    @Override
    public Job createJob(Job job) {
        LocalDateTime now = LocalDateTime.now();
        if (job.getStatus() == null) {
            job.setStatus(JobStatus.OPEN);
        }
        job.setCreateTime(now);
        job.setUpdateTime(now);
        jobMapper.insert(job);
        return job;
    }

    @Override
    public Job getJobById(Long id) {
        String key = buildJobDetailKey(id);

        // 缓存是加速层；缓存数据损坏或 Redis 故障时仍可从 MySQL 读取。
        try {
            String cachedJobJson =
                    stringRedisTemplate.opsForValue().get(key);

            if (cachedJobJson != null) {
                try {
                    return objectMapper.readValue(
                            cachedJobJson, Job.class
                    );
                } catch (JsonProcessingException e) {
                    log.warn("event=job_cache.invalid_json jobId={}", id, e);
                    stringRedisTemplate.delete(key);
                }
            }
        } catch (DataAccessException e) {
            log.warn("event=job_cache.read_failed jobId={}", id, e);
        }

        // MySQL 是岗位详情的事实来源。
        Job job = jobMapper.selectById(id);

        if (job == null) {
            throw new JobNotFoundException(id);
        }

        // 回填失败不改变已经读到的岗位结果。
        try {
            String jobJson =
                    objectMapper.writeValueAsString(job);

            stringRedisTemplate.opsForValue().set(
                    key,
                    jobJson,
                    10,
                    TimeUnit.MINUTES
            );
        } catch (JsonProcessingException e) {
            log.warn("event=job_cache.serialization_failed jobId={}", id, e);
        } catch (DataAccessException e) {
            log.warn("event=job_cache.write_failed jobId={}", id, e);
        }

        return job;
    }

    @Override
    public List<Job> getAllJobs() {
        return jobMapper.selectList(null);
    }

    @Override
    public Job updateJob(Job job) {
        Job existingJob = jobMapper.selectById(job.getId());
        if (existingJob == null) {
            throw new JobNotFoundException(job.getId());
        }
        job.setUpdateTime(LocalDateTime.now());
        jobMapper.updateById(job);
        invalidateJobCache(job.getId());
        return job;
    }

    @Override
    public boolean deleteJob(Long id) {
        int rows = jobMapper.deleteById(id);
        if (rows == 0) {
            throw new JobNotFoundException(id);
        }
        invalidateJobCache(id);
        return true;
    }

    @Override
    public IPage<Job> searchJobs(String keyword, JobStatus status, long page, long size) {
        LambdaQueryWrapper<Job> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w
                    .like(Job::getTitle, keyword)
                    .or()
                    .like(Job::getCompany, keyword)
                    .or()
                    .like(Job::getDescription, keyword));
        }
        if (status != null) {
            wrapper.eq(Job::getStatus, status);
        }
        Page<Job> pageRequest = new Page<>(page, size);
        return jobMapper.selectPage(pageRequest, wrapper);
    }

    @Override
    public long countJobs() {
        return jobMapper.selectCount(null);
    }
}
