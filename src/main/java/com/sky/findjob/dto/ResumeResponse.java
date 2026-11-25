package com.sky.findjob.dto;

import com.sky.findjob.entity.Resume;

import java.time.LocalDateTime;

public record ResumeResponse(
        String id,
        String name,
        String targetRole,
        Integer version,
        String fileName,
        Long fileSize,
        String fileType,
        String fileUrl,
        boolean isDefault,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ResumeResponse from(Resume resume, String fileBaseUrl) {
        String base = fileBaseUrl == null ? "" : fileBaseUrl.replaceAll("/+$", "");
        String url = resume.getFilePath() == null ? null : base + "/" + resume.getFilePath();
        return new ResumeResponse(
                String.valueOf(resume.getId()),
                resume.getName(),
                resume.getTargetRole(),
                resume.getVersion(),
                resume.getFileName(),
                resume.getFileSize(),
                resume.getFileType(),
                url,
                Boolean.TRUE.equals(resume.getIsDefault()),
                resume.getCreatedAt(),
                resume.getUpdatedAt()
        );
    }
}
