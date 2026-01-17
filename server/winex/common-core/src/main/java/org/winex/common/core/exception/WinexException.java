package org.winex.common.core.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception for all WinexPlay domain exceptions.
 */
@Getter
public class WinexException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus status;

    public WinexException(String message, String errorCode, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public WinexException(String message, String errorCode, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.status = status;
    }
}
