package com.kernelLetter.global.error.exception;

import com.kernelLetter.global.error.ErrorCode;
import lombok.Getter;

@Getter
public class ControllerException extends RuntimeException {

    public ErrorCode errorCode;

    public ControllerException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ControllerException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
