package com.sky.findjob.dto;

import java.util.List;

/**
 * 通用分页响应。
 */
public record PageResponse<T>(
        List<T> items,
        long total,
        long page,
        long size
) {
}
