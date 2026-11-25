package com.sky.findjob.controller;

import com.sky.findjob.Result;
import com.sky.findjob.dto.ApplicationSaveRequest;
import com.sky.findjob.dto.ApplicationStatusRequest;
import com.sky.findjob.service.IApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final IApplicationService applicationService;

    /**
     * 分页查询投递列表。
     */
    @GetMapping
    public Result list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) String channel,
                       @RequestParam(required = false, defaultValue = "1") Integer page,
                       @RequestParam(required = false, defaultValue = "20") Integer size,
                       @RequestParam(required = false, defaultValue = "updatedAt,desc") String sort) {
        return Result.success(
                applicationService.page(keyword, status, channel, page, size, sort));
    }

    @GetMapping("/{id}")
    public Result get(@PathVariable Long id) {
        return Result.success(applicationService.get(id));
    }

    @PostMapping
    public ResponseEntity<Result> create(@Valid @RequestBody ApplicationSaveRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Result.success(applicationService.create(request)));
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id,
                         @Valid @RequestBody ApplicationSaveRequest request) {
        return Result.success(applicationService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public Result updateStatus(@PathVariable Long id,
                               @Valid @RequestBody ApplicationStatusRequest request) {
        return Result.success(applicationService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        applicationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
