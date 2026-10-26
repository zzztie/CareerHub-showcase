package com.zzztie.careerhub.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzztie.careerhub.domain.Job;
import com.zzztie.careerhub.exception.JobNotFoundException;
import com.zzztie.careerhub.mapper.JobMapper;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class JobServiceImplTest {
    @Test
    void deleteJob_whenNotFound_shouldThrowException(){
        JobMapper jobMapper = mock(JobMapper.class);
        StringRedisTemplate redis = mock(StringRedisTemplate.class);

        JobServiceImpl service = new JobServiceImpl(
                jobMapper,
                new ObjectMapper(),
                redis
        );

        when(jobMapper.deleteById(999999L)).thenReturn(0);

        assertThrows(
                JobNotFoundException.class,
                () -> service.deleteJob(999999L)
        );

        verify(redis,never()).delete(anyString());
    }
    @Test
    void deleteJob_whenExists_shouldInvalidateCache(){
        JobMapper jobMapper = mock(JobMapper.class);
        StringRedisTemplate redis = mock(StringRedisTemplate.class);

        JobServiceImpl service = new JobServiceImpl(
                jobMapper,
                new ObjectMapper(),
                redis
        );
        // 模拟 MySQL 成功删除 1 行
        when(jobMapper.deleteById(1L)).thenReturn(1);

        // 模拟 Redis 删除缓存成功
        when(redis.delete("job:detail:1")).thenReturn(true);

        // 执行业务
        boolean result = service.deleteJob(1L);
        // 检查业务返回值
        assertTrue(result);

        // 验证数据库删除被调用一次
        verify(jobMapper,times(1)).deleteById(1L);

        // 验证缓存删除被调用一次
        verify(redis,times(1)).delete("job:detail:1");
    }
    @Test
    void deleteJob_WhenRedisFailsOnce_shouldRetrySuccessfully(){
        JobMapper jobMapper = mock(JobMapper.class);
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        JobServiceImpl service = new JobServiceImpl(
                jobMapper,
                new ObjectMapper(),
                redis
        );
        // 模拟 MySQL 删除成功
        when(jobMapper.deleteById(1L)).thenReturn(1);

        // 模拟 Redis 第一次失败，第二次成功
        when(redis.delete("job:detail:1"))
                .thenThrow(new DataAccessResourceFailureException(
                        "Simulated Redis failure"
                ))
                .thenReturn(true);

        // 执行业务方法
        boolean result = service.deleteJob(1L);
        // 验证业务最终成功
        assertTrue(result);

        // 验证 MySQL 只执行一次删除
        verify(jobMapper,times(1)).deleteById(1L);

        // 验证 Redis 删除总共尝试两次
        verify(redis,times(2)).delete("job:detail:1");
    }
    @Test
    void deleteJob_whenRedisAlwaysFails_shouldThrowAfterThreeAttempts(){
        JobMapper jobMapper = mock(JobMapper.class);
        StringRedisTemplate redis = mock(StringRedisTemplate.class);

        JobServiceImpl service = new JobServiceImpl(
                jobMapper,
                new ObjectMapper(),
                redis
        );
        // 模拟数据库删除成功
        when(jobMapper.deleteById(1L)).thenReturn(1);

        // 模拟 Redis 一直发生异常
        when(redis.delete("job:detail:1"))
                .thenThrow(new DataAccessResourceFailureException(
                        "Simulated Redis unavailable"
                ));

        // 验证最终会抛出异常
        assertThrows(
                DataAccessResourceFailureException.class,
                () -> service.deleteJob(1L)
        );

        // 数据库删除只执行一次
        verify(jobMapper,times(1)).deleteById(1L);

        // Redis 删除总共尝试三次
        verify(redis,times(3)).delete("job:detail:1");
    }
    @Test
    void getJobById_whenCacheHit_shouldNotQueryMysql(){
        JobMapper jobMapper = mock(JobMapper.class);
        StringRedisTemplate redis = mock(StringRedisTemplate.class);

        @SuppressWarnings("unchecked")
        ValueOperations<String,String> valueOps =
                mock(ValueOperations.class);

        // 模拟 Redis 的操作对象
        when(redis.opsForValue()).thenReturn(valueOps);

        // 模拟 Redis 缓存命中
        when(valueOps.get("job:detail:1"))
                .thenReturn(
                        "{\"id\":1,\"title\":\"Java Intern\"}"
                );

        JobServiceImpl service = new JobServiceImpl(
                jobMapper,
                new ObjectMapper(),
                redis
        );
        Job job = service.getJobById(1L);
        assertEquals(1L,job.getId());
        assertEquals("Java Intern",job.getTitle());

        verify(valueOps,times(1)).get("job:detail:1");
        verifyNoInteractions(jobMapper);
    }
    @Test
    void getJobById_whenCacheMiss_shouldQueryMysqlAndFillCache(){
        // ① 创建模拟依赖
        JobMapper jobMapper = mock(JobMapper.class);
        StringRedisTemplate redis = mock(StringRedisTemplate.class);

        @SuppressWarnings("unchecked")
        ValueOperations<String,String> valueops =
                mock(ValueOperations.class);

        // ② 配置 Redis 的模拟行为
        when(redis.opsForValue()).thenReturn(valueops);

        // Redis 中没有缓存
        when(valueops.get("job:detail:1"))
                .thenReturn(null);

        Job jobFromDb = new Job();
        jobFromDb.setId(1L);
        jobFromDb.setTitle("Java Intern");

        when(jobMapper.selectById(1L))
                .thenReturn(jobFromDb);

        ObjectMapper objectMapper =
                new ObjectMapper().findAndRegisterModules();

        JobServiceImpl service = new JobServiceImpl(
                jobMapper,
                objectMapper,
                redis
        );
        Job result = service.getJobById(1L);

        assertEquals(1L,result.getId());
        assertEquals("Java Intern", result.getTitle());

        // ⑦ 验证 Redis 被查询一次
        verify(valueops, times(1)).get("job:detail:1");

        // ⑧ 验证 MySQL 被查询一次
        verify(jobMapper, times(1)).selectById(1L);

        // ⑨ 验证 Redis 缓存回填
        verify(valueops, times(1)).set(
                eq("job:detail:1"),
                anyString(),
                eq(10L),
                eq(TimeUnit.MINUTES)
        );
    }
    @Test
    void getJobById_whenNotFound_shouldThrowException(){
        // ① 创建模拟依赖
        JobMapper jobMapper = mock(JobMapper.class);
        StringRedisTemplate redis = mock(StringRedisTemplate.class);

        @SuppressWarnings("unchecked")
        ValueOperations<String,String> valueops = mock(ValueOperations.class);
        // ② 配置 Redis 缓存未命中
        when(redis.opsForValue()).thenReturn(valueops);
        when(valueops.get("job:detail:999999")).thenReturn(null);

        // ③ 配置 MySQL 查询不到 Job
        when(jobMapper.selectById(999999L)).thenReturn(null);
        JobServiceImpl service = new JobServiceImpl(
                jobMapper,
                new ObjectMapper().findAndRegisterModules(),
                redis
        );
        // ⑤ 执行业务，并断言异常
        assertThrows(
                JobNotFoundException.class,
                () -> service.getJobById(999999L)
        );
        // ⑥ 验证 Redis 查询一次
        verify(valueops,times(1)).get("job:detail:999999");
        // ⑦ 验证 MySQL 查询一次
        verify(jobMapper,times(1)).selectById(999999L);
        // ⑧ 验证 Redis 没有执行其他操作，例如回填
        verifyNoMoreInteractions(valueops);
    }
    @Test
    void getJobById_whenCacheJsonInvalid_shouldReloadFromMysql(){
        JobMapper jobMapper = mock(JobMapper.class);
        StringRedisTemplate redis = mock(StringRedisTemplate.class);

        @SuppressWarnings("unchecked")ValueOperations<String,String> valueops
                = mock(ValueOperations.class);

        when(redis.opsForValue()).thenReturn(valueops);
        when(valueops.get("job:detail:1")).thenReturn("{invalid-json}");
        Job jobFromDb =new Job();
        jobFromDb.setTitle("Java Intern");
        jobFromDb.setId(1L);
        when(jobMapper.selectById(1L)).thenReturn(jobFromDb);
        JobServiceImpl service = new JobServiceImpl(
                jobMapper,
                new ObjectMapper().findAndRegisterModules(),
                redis
        );
        Job result = service.getJobById(1L);
        assertEquals(1L,result.getId());
        assertEquals("Java Intern",result.getTitle());
        verify(redis,times(1)).delete("job:detail:1");
        verify(jobMapper,times(1)).selectById(1L);
        verify(valueops,times(1)).set(
                eq("job:detail:1"),
                anyString(),
                eq(10L),
                eq(TimeUnit.MINUTES)
        );
    }
    @Test
    void uodateJob_whenExists_shouldUpdateDBAndInvalidateCache(){
        JobMapper jobMapper = mock(JobMapper.class);
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        // ② 模拟数据库中已有的 Job
        Job existingJob = new Job();
        existingJob.setId(1L);
        existingJob.setTitle("Java Intern");
        // ③ 准备管理员提交的新 Job
        when(jobMapper.selectById(1L)).thenReturn(existingJob);
        Job updateJob = new Job();
        updateJob.setTitle("Senior Java Intern");
        updateJob.setId(1L);
        // ④ 模拟 MySQL 更新成功
        when(jobMapper.updateById(updateJob)).thenReturn(1);
        // ⑤ 模拟 Redis 删除缓存成功
        when(redis.delete("job:detail:1")).thenReturn(true);
        JobServiceImpl service = new JobServiceImpl(
                jobMapper,
                new ObjectMapper().findAndRegisterModules() ,
                redis
        );
        // ⑦ 执行真实业务
        Job result = service.updateJob(updateJob);
        // ⑧ 验证返回结果
        assertSame(updateJob,result);
        assertEquals("Senior Java Intern",result.getTitle());
        // ⑨ 验证查询原 Job
        verify(jobMapper,times(1)).selectById(1L);
        // ⑩ 验证更新 MySQL
        verify(jobMapper,times(1)).updateById(updateJob);
        // ⑪ 验证删除 Redis 缓存
        verify(redis,times(1)).delete("job:detail:1");
    }
    @Test
    void updateJob_whenNotFound_shouldThrowAndNotInvalidateCache(){
        JobMapper jobMapper = mock(JobMapper.class);
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        // ② 准备管理员提交的数据
        Job updateJob = new Job();
        updateJob.setId(999999L);
        updateJob.setTitle("Senior Java Intern");
        // ③ 模拟 MySQL 查询不到这个 Job
        when(jobMapper.selectById(999999L)).thenReturn(null);
        JobServiceImpl service = new JobServiceImpl(
                jobMapper,
                new ObjectMapper().findAndRegisterModules() ,
                redis
        );
        assertThrows(
                JobNotFoundException.class,
                () -> service.updateJob(updateJob)
        );
        // ⑥ 验证执行了一次存在性查询
        verify(jobMapper,times(1)).selectById(999999L);
        verify(jobMapper,never()).updateById(any(Job.class));
        verifyNoInteractions(redis);
    }
}
