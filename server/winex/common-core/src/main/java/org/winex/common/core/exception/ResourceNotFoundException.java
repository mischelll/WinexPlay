package org.winex.common.core.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a requested resource (user, bet, match, etc.) is not found.
 */
public class ResourceNotFoundException extends WinexException {

    public ResourceNotFoundException(String resourceType, String identifier) {
        super(
            String.format("%s not found with identifier: %s", resourceType, identifier),
            "RESOURCE_NOT_FOUND",
            HttpStatus.NOT_FOUND
        );
    }

    public ResourceNotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND);
    }
}
