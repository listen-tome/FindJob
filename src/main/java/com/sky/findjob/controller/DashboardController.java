package com.sky.findjob.controller;

import com.sky.findjob.Result;
import com.sky.findjob.service.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final IDashboardService dashboardService;

    @GetMapping
    public Result getDashboard() {
        return Result.success(dashboardService.getDashboard());
    }
}
