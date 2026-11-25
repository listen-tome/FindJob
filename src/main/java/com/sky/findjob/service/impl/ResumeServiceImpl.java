package com.sky.findjob.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sky.findjob.common.BusinessException;
import com.sky.findjob.common.UserContext;
import com.sky.findjob.dto.ListResponse;
import com.sky.findjob.dto.ResumeResponse;
import com.sky.findjob.dto.ResumeUpdateRequest;
import com.sky.findjob.entity.Application;
import com.sky.findjob.entity.Resume;
import com.sky.findjob.mapper.ApplicationMapper;
import com.sky.findjob.mapper.ResumeMapper;
import com.sky.findjob.service.IResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeServiceImpl implements IResumeService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");

    private static final Map<String, String> ALLOWED_EXTENSIONS = Map.of(
            "pdf", "application/pdf",
            "doc", "application/msword",
            "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private final ResumeMapper resumeMapper;
    private final ApplicationMapper applicationMapper;

    @Value("${app.upload-dir:uploads/resumes}")
    private String uploadDirSetting;

    @Value("${app.file-base-url:http://localhost:8080/files/resumes}")
    private String fileBaseUrl;

    @Override
    @Transactional(readOnly = true)
    public ListResponse<ResumeResponse> list() {
        Long userId = UserContext.require();
        List<ResumeResponse> items = resumeMapper.selectList(Wrappers.<Resume>lambdaQuery()
                        .eq(Resume::getUserId, userId)
                        .orderByDesc(Resume::getIsDefault)
                        .orderByDesc(Resume::getUpdatedAt)
                        .orderByDesc(Resume::getId))
                .stream()
                .map(resume -> ResumeResponse.from(resume, fileBaseUrl))
                .toList();
        return new ListResponse<>(items, items.size());
    }

    @Override
    @Transactional
    public ResumeResponse upload(MultipartFile file, String name, String targetRole) {
        Long userId = UserContext.require();
        validateFile(file);

        String cleanName = name == null ? "" : name.trim();
        String cleanTargetRole = targetRole == null ? "" : targetRole.trim();
        if (!StringUtils.hasText(cleanName)) {
            throw new BusinessException("简历名称不能为空", HttpStatus.BAD_REQUEST);
        }
        if (cleanName.length() > 100) {
            throw new BusinessException("简历名称最多 100 个字符", HttpStatus.BAD_REQUEST);
        }
        if (!StringUtils.hasText(cleanTargetRole)) {
            throw new BusinessException("目标岗位不能为空", HttpStatus.BAD_REQUEST);
        }
        if (cleanTargetRole.length() > 100) {
            throw new BusinessException("目标岗位最多 100 个字符", HttpStatus.BAD_REQUEST);
        }

        String originalName = safeFileName(file.getOriginalFilename());
        String extension = extensionOf(originalName);
        String storedName = UUID.randomUUID() + "." + extension;

        Path directory = uploadDirectory();
        Path target = directory.resolve(storedName).normalize();
        if (!target.startsWith(directory)) {
            throw new BusinessException("非法文件名", HttpStatus.BAD_REQUEST);
        }

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException("简历保存失败", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        int version = resumeMapper.selectList(Wrappers.<Resume>lambdaQuery()
                        .eq(Resume::getUserId, userId)
                        .eq(Resume::getTargetRole, cleanTargetRole))
                .stream()
                .map(Resume::getVersion)
                .filter(v -> v != null)
                .max(Integer::compareTo)
                .orElse(0) + 1;

        boolean firstResume = resumeMapper.selectCount(Wrappers.<Resume>lambdaQuery()
                .eq(Resume::getUserId, userId)) == 0;

        LocalDateTime now = LocalDateTime.now(ZONE).withNano(0);
        Resume resume = new Resume();
        resume.setUserId(userId);
        resume.setName(cleanName);
        resume.setTargetRole(cleanTargetRole);
        resume.setVersion(version);
        resume.setFileName(originalName);
        resume.setFileSize(file.getSize());
        resume.setFileType(contentTypeFor(extension, file.getContentType()));
        resume.setFilePath(storedName);
        resume.setIsDefault(firstResume);
        resume.setCreatedAt(now);
        resume.setUpdatedAt(now);

        try {
            resumeMapper.insert(resume);
        } catch (RuntimeException e) {
            deleteQuietly(target);
            throw e;
        }

        return ResumeResponse.from(resume, fileBaseUrl);
    }

    @Override
    @Transactional
    public ResumeResponse update(Long id, ResumeUpdateRequest request) {
        Long userId = UserContext.require();
        requireOwned(id, userId);

        Resume update = new Resume();
        update.setId(id);
        update.setName(request.name().trim());
        update.setTargetRole(request.targetRole().trim());
        update.setUpdatedAt(LocalDateTime.now(ZONE).withNano(0));
        resumeMapper.updateById(update);

        return ResumeResponse.from(requireOwned(id, userId), fileBaseUrl);
    }

    @Override
    @Transactional
    public ResumeResponse setDefault(Long id) {
        Long userId = UserContext.require();
        requireOwned(id, userId);

        resumeMapper.update(null, Wrappers.<Resume>lambdaUpdate()
                .eq(Resume::getUserId, userId)
                .set(Resume::getIsDefault, false));

        Resume update = new Resume();
        update.setId(id);
        update.setIsDefault(true);
        update.setUpdatedAt(LocalDateTime.now(ZONE).withNano(0));
        resumeMapper.updateById(update);

        return ResumeResponse.from(requireOwned(id, userId), fileBaseUrl);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Long userId = UserContext.require();
        Resume resume = requireOwned(id, userId);
        boolean wasDefault = Boolean.TRUE.equals(resume.getIsDefault());

        // 引用该简历的投递记录置空
        applicationMapper.update(null, Wrappers.<Application>lambdaUpdate()
                .eq(Application::getUserId, userId)
                .eq(Application::getResumeId, id)
                .set(Application::getResumeId, null));

        resumeMapper.deleteById(id);

        // 删除默认简历后，自动把剩余最新的一份设为默认
        if (wasDefault) {
            List<Resume> remaining = resumeMapper.selectList(Wrappers.<Resume>lambdaQuery()
                    .eq(Resume::getUserId, userId)
                    .orderByDesc(Resume::getUpdatedAt)
                    .orderByDesc(Resume::getId)
                    .last("LIMIT 1"));
            if (!remaining.isEmpty()) {
                Resume newDefault = new Resume();
                newDefault.setId(remaining.get(0).getId());
                newDefault.setIsDefault(true);
                newDefault.setUpdatedAt(LocalDateTime.now(ZONE).withNano(0));
                resumeMapper.updateById(newDefault);
            }
        }

        // 数据库删除成功后再删磁盘文件，失败只记日志
        deleteQuietly(uploadDirectory().resolve(resume.getFilePath()).normalize());
    }

    private Resume requireOwned(Long id, Long userId) {
        Resume resume = resumeMapper.selectOne(Wrappers.<Resume>lambdaQuery()
                .eq(Resume::getId, id)
                .eq(Resume::getUserId, userId));
        if (resume == null) {
            throw new BusinessException("简历不存在", HttpStatus.NOT_FOUND);
        }
        return resume;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的文件", HttpStatus.BAD_REQUEST);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("文件超过 10MB", HttpStatus.PAYLOAD_TOO_LARGE);
        }

        String originalName = safeFileName(file.getOriginalFilename());
        String extension = extensionOf(originalName);
        if (!ALLOWED_EXTENSIONS.containsKey(extension)) {
            throw new BusinessException("文件格式不支持，仅支持 PDF / DOC / DOCX", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }

        String contentType = file.getContentType();
        if (StringUtils.hasText(contentType)
                && !"application/octet-stream".equalsIgnoreCase(contentType)
                && !ALLOWED_EXTENSIONS.get(extension).equalsIgnoreCase(contentType)) {
            throw new BusinessException("文件格式不支持，仅支持 PDF / DOC / DOCX", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }
    }

    private Path uploadDirectory() {
        Path directory = Paths.get(uploadDirSetting).toAbsolutePath().normalize();
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new BusinessException("简历目录创建失败", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return directory;
    }

    private String safeFileName(String originalFilename) {
        String name = StringUtils.cleanPath(originalFilename == null ? "resume" : originalFilename);
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        return name.isBlank() ? "resume" : name;
    }

    private String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String contentTypeFor(String extension, String provided) {
        if ("application/octet-stream".equalsIgnoreCase(provided)) {
            return ALLOWED_EXTENSIONS.get(extension);
        }
        return StringUtils.hasText(provided) ? provided : ALLOWED_EXTENSIONS.get(extension);
    }

    private void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("删除简历文件失败: {}", path, e);
        }
    }
}
