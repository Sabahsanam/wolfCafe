package edu.ncsu.csc326.wolfcafe.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Exception for WolfCafe API calls.
 */
@Getter
@AllArgsConstructor
public class WolfCafeAPIException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** HttpStatus for exception */
    private HttpStatus        status;

    /** Exception message */
    private String            message;
}
