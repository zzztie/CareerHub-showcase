package com.zzztie.careerhub.controller;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.zzztie.careerhub.domain.Resume;
import com.zzztie.careerhub.service.ResumeService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/resumes")
public class ResumeController {
    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public Resume uploadResume(
            @RequestParam Long userId,
            @RequestPart("file") MultipartFile file
    ) {
        return resumeService.uploadResume(userId, file);
    }

    @GetMapping
    public List<Resume> getResumesByUserId(
            @RequestParam Long userId
    ){
        return resumeService.getResumeByUserId(userId);
    }

    @GetMapping("/{id}")
    public Resume getResumeById(@PathVariable Long id){
        return resumeService.getResumeById(id);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadResume(@PathVariable Long id) {
        Resume resume = resumeService.getResumeById(id);
        Path filePath = Paths.get(resume.getFilepath());
        Resource resource = new FileSystemResource(filePath);
        if (!resource.exists()) {
            throw new RuntimeException("resume file not found");
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resume.getFilename() + "\""
                )
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public boolean deleteResume(
            @PathVariable Long id
    ){
        return resumeService.deleteResume(id);
    }
}
