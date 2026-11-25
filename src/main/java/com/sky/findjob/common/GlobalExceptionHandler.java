package com.sky.findjob.common;

import com.sky.findjob.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result> handleBusiness(BusinessException e) {
        return ResponseEntity.status(e.getStatus()).body(Result.fail(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result> handleValidation(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError == null ? "参数校验失败" : fieldError.getDefaultMessage();
        return ResponseEntity.badRequest().body(Result.fail(message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result> handleUnreadable(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(Result.fail("请求参数格式错误"));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result> handleMissingParameter(MissingServletRequestParameterException e) {
        return ResponseEntity.badRequest().body(Result.fail("缺少参数：" + e.getParameterName()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.badRequest().body(Result.fail("参数格式错误：" + e.getName()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Result> handleMaxUpload(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(Result.fail("文件超过 10MB"));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<Result> handleMissingPart(MissingServletRequestPartException e) {
        return ResponseEntity.badRequest().body(Result.fail("缺少文件参数：" + e.getRequestPartName()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(Result.fail("请求方法不支持：" + e.getMethod()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result> handleNoResource(NoResourceFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Result.fail("接口不存在"));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Result> handleDuplicateKey(DuplicateKeyException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Result.fail("邮箱已注册"));
    }

    @ExceptionHandler(BizException.class)
    public ResponseEntity<Result> handleBiz(BizException e) {
        HttpStatus status = switch (e.getCode()) {
            case "UNAUTHORIZED", "INVALID_CREDENTIALS" -> HttpStatus.UNAUTHORIZED;
            case "FORBIDDEN"                            -> HttpStatus.FORBIDDEN;
            case "APPLICATION_NOT_FOUND",
                 "INTERVIEW_NOT_FOUND",
                 "RESUME_NOT_FOUND"                     -> HttpStatus.NOT_FOUND;
            case "EMAIL_ALREADY_EXISTS"                 -> HttpStatus.CONFLICT;
            default                                     -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status).body(Result.fail(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result> handleUnknown(Exception e) {
        log.error("未处理的服务端异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.fail("服务端异常"));
    }
}
