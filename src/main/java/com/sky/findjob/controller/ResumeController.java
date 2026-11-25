package com.sky.findjob.controller;

import com.sky.findjob.Result;
import com.sky.findjob.dto.ResumeUpdateRequest;
import com.sky.findjob.service.IResumeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final IResumeService resumeService;

    @GetMapping
    public Result list() {
        return Result.success(resumeService.list());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Result> upload(@RequestPart("file") MultipartFile file,
                                         @RequestParam("name") String name,
                                         @RequestParam("targetRole") String targetRole) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Result.success(resumeService.upload(file, name, targetRole)));
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id,
                         @Valid @RequestBody ResumeUpdateRequest request) {
        return Result.success(resumeService.update(id, request));
    }

    @PatchMapping("/{id}/default")
    public Result setDefault(@PathVariable Long id) {
        return Result.success(resumeService.setDefault(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        resumeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
