package org.winex.common.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a business rule is violated (e.g., insufficient balance, market suspended).
 */
public class BusinessRuleException extends WinexException {

    public BusinessRuleException(String message, String errorCode) {
        super(message, errorCode, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public BusinessRuleException(String message) {
        super(message, "BUSINESS_RULE_VIOLATION", HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
