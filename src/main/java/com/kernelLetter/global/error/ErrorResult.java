package com.kernelLetter.global.error;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Getter
@Builder
@Slf4j
public class ErrorResult {

    private String errorCode;
    private String errorMessage;
    private LocalDateTime timestamp;

    public static ErrorResult of (String errorCode, String message){

        log.info("ErrorResult.message : " + message);

        return ErrorResult.builder()
                .errorCode(errorCode)
                .errorMessage(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
