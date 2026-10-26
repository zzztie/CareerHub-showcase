package com.zzztie.careerhub.repository.memory;

import com.zzztie.careerhub.domain.Resume;
import com.zzztie.careerhub.repository.ResumeRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryResumeRepository implements ResumeRepository {

    private final Map<Long,Resume> resumes = new HashMap<>();

    @Override
    public Resume save(Resume resume) {
        resumes.put(resume.getId(),resume);
        return resume;
    }

    @Override
    public Resume findById(Long id) {
        return resumes.get(id);
    }

    @Override
    public boolean existsById(Long id) {
        return resumes.containsKey(id);
    }

    @Override
    public boolean deleteById(Long id) {
        return resumes.remove(id) != null;
    }

    @Override
    public List<Resume> findByUserId(Long userId) {
        return resumes.values()
                .stream()
                .filter(resume -> resume.getUserId().equals(userId))
                .toList();
    }
}
