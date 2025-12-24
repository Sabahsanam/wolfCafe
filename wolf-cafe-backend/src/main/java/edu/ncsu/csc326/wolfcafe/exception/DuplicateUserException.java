package edu.ncsu.csc326.wolfcafe.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception when attempting to create a user with existing username
 */
@ResponseStatus ( HttpStatus.CONFLICT )
public class DuplicateUserException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a DuplicateUserException
     *
     * @param message
     *            exception message
     */
    public DuplicateUserException ( final String message ) {
        super( message );
    }
}
