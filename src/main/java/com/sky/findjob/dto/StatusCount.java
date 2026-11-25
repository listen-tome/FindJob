package com.sky.findjob.dto;

import com.sky.findjob.enums.ApplicationStatus;

public record StatusCount(ApplicationStatus status, long value) {
}
