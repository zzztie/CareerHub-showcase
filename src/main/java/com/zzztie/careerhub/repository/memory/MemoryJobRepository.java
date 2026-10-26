package com.zzztie.careerhub.repository.memory;

import com.zzztie.careerhub.domain.Job;
import com.zzztie.careerhub.exception.JobNotFoundException;
import com.zzztie.careerhub.repository.JobRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Repository
public class MemoryJobRepository implements JobRepository {
    private final Map<Long,Job> jobs =new HashMap<>();
    @Override
    public Job save(Job job) {
        jobs.put(job.getId(),job);
        return job;
    }

    @Override
    public Job findById(Long id) {
        return jobs.get(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jobs.containsKey(id);
    }

    @Override
    public boolean deleteById(Long id) {
        return jobs.remove(id) != null;
    }

    @Override
    public List<Job> findAll() {
        return new ArrayList<>(jobs.values());
    }
}
