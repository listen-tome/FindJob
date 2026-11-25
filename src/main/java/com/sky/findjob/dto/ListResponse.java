package com.sky.findjob.dto;

import java.util.List;

/**
 * 不分页的列表响应：面试、简历等数量较少的场景。
 */
public record ListResponse<T>(List<T> items, long total) {
}
