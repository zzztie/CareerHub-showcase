package com.zzztie.careerhub.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zzztie.careerhub.domain.Job;
import com.zzztie.careerhub.enums.JobStatus;

import java.util.List;

public interface JobService {
    Job createJob(Job job);
    Job getJobById(Long id);
    List<Job> getAllJobs();
    Job updateJob(Job job);
    boolean deleteJob(Long id);
    IPage<Job> searchJobs(String keyword, JobStatus status,long page ,long size);
    long countJobs();
}
