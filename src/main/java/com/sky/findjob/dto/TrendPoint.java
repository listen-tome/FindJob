package com.sky.findjob.dto;

/**
 * 趋势图上的一个点。
 */
public record TrendPoint(
        String date,
        String label,
        long applications,
        long interviews
) {
}
