package com.sky.findjob.service;

import com.sky.findjob.dto.ApplicationResponse;
import com.sky.findjob.dto.ApplicationSaveRequest;
import com.sky.findjob.dto.ApplicationStatusRequest;
import com.sky.findjob.dto.PageResponse;

public interface IApplicationService {

    /**
     * 分页查询当前登录用户的投递记录。
     */
    PageResponse<ApplicationResponse> page(String keyword,
                                           String status,
                                           String channel,
                                           Integer page,
                                           Integer size,
                                           String sort);

    ApplicationResponse get(Long id);

    ApplicationResponse create(ApplicationSaveRequest request);

    ApplicationResponse update(Long id, ApplicationSaveRequest request);

    ApplicationResponse updateStatus(Long id, ApplicationStatusRequest request);

    void delete(Long id);
}
