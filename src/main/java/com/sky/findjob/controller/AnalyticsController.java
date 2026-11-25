package com.sky.findjob.controller;

import com.sky.findjob.Result;
import com.sky.findjob.service.IAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final IAnalyticsService analyticsService;

    @GetMapping
    public Result getAnalytics(@RequestParam(required = false) String range) {
        return Result.success(analyticsService.getAnalytics(range));
    }
}
