package com.sky.findjob.service;

import com.sky.findjob.dto.InterviewRequest;
import com.sky.findjob.dto.InterviewResponse;
import com.sky.findjob.dto.InterviewStatusRequest;
import com.sky.findjob.dto.ListResponse;

import java.time.LocalDate;

public interface IInterviewService {

    ListResponse<InterviewResponse> list(LocalDate from, LocalDate to, String status);

    InterviewResponse create(InterviewRequest request);

    InterviewResponse update(Long id, InterviewRequest request);

    InterviewResponse updateStatus(Long id, InterviewStatusRequest request);

    void delete(Long id);
}
