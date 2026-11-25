package com.sky.findjob.service;

import com.sky.findjob.dto.AnalyticsResponse;

public interface IAnalyticsService {

    AnalyticsResponse getAnalytics(String range);
}
