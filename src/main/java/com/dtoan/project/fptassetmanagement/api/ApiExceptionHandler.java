package com.dtoan.project.fptassetmanagement.api;

import com.dtoan.project.fptassetmanagement.api.dto.ApiDtos;
import com.dtoan.project.fptassetmanagement.exception.TicketAlreadyClaimedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.dtoan.project.fptassetmanagement.api")
public class ApiExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiDtos.SimpleMessageResponse> handleBadCredentials(BadCredentialsException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiDtos.SimpleMessageResponse("Tên đăng nhập hoặc mật khẩu không đúng."));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiDtos.SimpleMessageResponse> handleDisabled(DisabledException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiDtos.SimpleMessageResponse("Tài khoản của bạn đã bị admin khóa."));
    }

    @ExceptionHandler(TicketAlreadyClaimedException.class)
    public ResponseEntity<ApiDtos.SimpleMessageResponse> handleTicketClaimConflict(TicketAlreadyClaimedException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiDtos.SimpleMessageResponse(exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiDtos.SimpleMessageResponse> handleBadRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest()
                .body(new ApiDtos.SimpleMessageResponse(exception.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiDtos.SimpleMessageResponse> handleIllegalState(IllegalStateException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiDtos.SimpleMessageResponse(exception.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiDtos.SimpleMessageResponse> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        String message = exception.getMostSpecificCause() != null ? exception.getMostSpecificCause().getMessage() : exception.getMessage();
        if (message != null && message.toLowerCase().contains("rooms") && message.toLowerCase().contains("code")) {
            return ResponseEntity.badRequest()
                    .body(new ApiDtos.SimpleMessageResponse("Mã phòng đã tồn tại."));
        }
        return ResponseEntity.badRequest()
                .body(new ApiDtos.SimpleMessageResponse("Dữ liệu đã tồn tại hoặc không hợp lệ."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiDtos.SimpleMessageResponse> handleGeneric(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiDtos.SimpleMessageResponse(exception.getMessage() == null ? "Có lỗi xảy ra." : exception.getMessage()));
    }
}
