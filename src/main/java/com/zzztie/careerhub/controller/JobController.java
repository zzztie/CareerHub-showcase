package com.zzztie.careerhub.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zzztie.careerhub.domain.Job;
import com.zzztie.careerhub.enums.JobStatus;
import com.zzztie.careerhub.service.JobService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;


@RestController
public class JobController {
    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping("/jobs/count")
    public long countJob() {
        return jobService.countJobs();
    }

    @GetMapping("/jobs/{id}")
    public Job getJobByid(
            @PathVariable Long id
    ){
        return jobService.getJobById(id);
    }

    @GetMapping("/jobs/search")
    public IPage<Job> searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) JobStatus status,
            @Min(value = 1, message = "page must be at least 1")
            @RequestParam(defaultValue = "1") long page,
            @Min(value = 1, message = "size must be at least 1")
            @Max(value = 100, message = "size must not exceed 100")
            @RequestParam(defaultValue = "10") long size) {
        return jobService.searchJobs(
                keyword,
                status,
                page,
                size
        );
    }
}
