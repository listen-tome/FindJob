package com.sky.findjob.service;

import com.sky.findjob.dto.ListResponse;
import com.sky.findjob.dto.ResumeResponse;
import com.sky.findjob.dto.ResumeUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

public interface IResumeService {

    ListResponse<ResumeResponse> list();

    ResumeResponse upload(MultipartFile file, String name, String targetRole);

    ResumeResponse update(Long id, ResumeUpdateRequest request);

    ResumeResponse setDefault(Long id);

    void delete(Long id);
}
