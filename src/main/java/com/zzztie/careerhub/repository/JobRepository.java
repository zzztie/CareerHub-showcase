package com.zzztie.careerhub.repository;

import com.zzztie.careerhub.domain.Job;

import java.util.List;

public interface JobRepository {
    Job save(Job job);
    Job findById(Long id);
    boolean existsById(Long id);
    boolean deleteById(Long id);
    List<Job> findAll();
}
