package com.sky.findjob.controller;

import com.sky.findjob.Result;
import com.sky.findjob.dto.InterviewRequest;
import com.sky.findjob.dto.InterviewStatusRequest;
import com.sky.findjob.service.IInterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final IInterviewService interviewService;

    @GetMapping
    public Result list(@RequestParam(required = false)
                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                       @RequestParam(required = false)
                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                       @RequestParam(required = false) String status) {
        return Result.success(interviewService.list(from, to, status));
    }

    @PostMapping
    public ResponseEntity<Result> create(@Valid @RequestBody InterviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Result.success(interviewService.create(request)));
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id,
                         @Valid @RequestBody InterviewRequest request) {
        return Result.success(interviewService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public Result updateStatus(@PathVariable Long id,
                               @Valid @RequestBody InterviewStatusRequest request) {
        return Result.success(interviewService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        interviewService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
