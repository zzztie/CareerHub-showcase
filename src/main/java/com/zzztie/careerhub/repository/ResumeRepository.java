package com.zzztie.careerhub.repository;

import com.zzztie.careerhub.domain.Resume;

import java.util.List;

public interface ResumeRepository {
    Resume save(Resume resume);
    Resume findById(Long id);
    boolean existsById(Long id);
    boolean deleteById(Long id);
    List<Resume> findByUserId(Long id);
}
