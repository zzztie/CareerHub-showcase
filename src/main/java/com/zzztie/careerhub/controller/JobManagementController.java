package com.zzztie.careerhub.controller;

import com.zzztie.careerhub.domain.Job;
import com.zzztie.careerhub.service.JobService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

@RestController
@ConditionalOnProperty(
        prefix = "careerhub",
        name = "job-management-enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class JobManagementController {

    private final JobService jobService;

    public JobManagementController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping("/jobs")
    public Job createJob(@RequestBody Job job) {
        return jobService.createJob(job);
    }

    @PutMapping("/jobs/{id}")
    public Job updateJob(
            @PathVariable Long id,
            @RequestBody Job job) {

        job.setId(id);
        return jobService.updateJob(job);
    }

    @DeleteMapping("/jobs/{id}")
    public boolean deleteJob(@PathVariable Long id) {
        return jobService.deleteJob(id);
    }
}