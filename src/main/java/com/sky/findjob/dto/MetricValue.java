package com.sky.findjob.dto;

/**
 * 单个指标值：数值 + 与上一周期相比的变化。
 */
public record MetricValue(double value, double delta) {
}
