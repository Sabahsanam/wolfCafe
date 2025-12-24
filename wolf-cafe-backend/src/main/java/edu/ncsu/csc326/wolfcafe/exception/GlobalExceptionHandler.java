package edu.ncsu.csc326.wolfcafe.exception;

import java.time.LocalDateTime;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

/** * Handles global errors. */
@ControllerAdvice
public class GlobalExceptionHandler {
    /**
     * * Handles global API exceptions * @param exception a WolfCafeAPI
     * exception * @param webRequest the request that caused the exception
     * * @return a ResponseEntity encapsulating the exception information for
     * presentation to the front end
     */
    @ExceptionHandler ( WolfCafeAPIException.class )
    public ResponseEntity<ErrorDetails> handleAPIException ( final WolfCafeAPIException exception,
            final WebRequest webRequest ) {
        final ErrorDetails errorDetails = new ErrorDetails( LocalDateTime.now(), exception.getMessage(),
                webRequest.getDescription( false ) );
        return new ResponseEntity<>( errorDetails, exception.getStatus() );
    }

    /**
     * Handles global validation exceptions
     *
     * @param exception
     *            a WolfCafeAPI
     * @param webRequest
     *            the request that caused the exception
     * @return a ResponseEntity encapsulating the exception information for
     *         presentation to the front end
     */
    @ExceptionHandler ( MethodArgumentNotValidException.class )
    public ResponseEntity<ErrorDetails> handleValidationException ( final MethodArgumentNotValidException ex,
            final WebRequest request ) {
        final String message = ex.getBindingResult().getFieldErrors().stream().findFirst()
                .map( DefaultMessageSourceResolvable::getDefaultMessage ).orElse( "Validation failed" );
        final ErrorDetails errorDetails = new ErrorDetails( LocalDateTime.now(), message,
                request.getDescription( false ) );
        return new ResponseEntity<>( errorDetails, HttpStatus.BAD_REQUEST );
    }

    /**
     * Handles global constraint violations
     *
     * @param exception
     *            a WolfCafeAPI
     * @param webRequest
     *            the request that caused the exception
     * @return a ResponseEntity encapsulating the exception information for
     *         presentation to the front end
     */
    @ExceptionHandler ( ConstraintViolationException.class )
    public ResponseEntity<ErrorDetails> handleConstraintViolation ( final ConstraintViolationException ex,
            final WebRequest request ) {
        final String message = ex.getConstraintViolations().stream().findFirst().map( ConstraintViolation::getMessage )
                .orElse( "Validation failed" );
        final ErrorDetails errorDetails = new ErrorDetails( LocalDateTime.now(), message,
                request.getDescription( false ) );
        return new ResponseEntity<>( errorDetails, HttpStatus.BAD_REQUEST );
    }

    @ExceptionHandler ( DuplicateUserException.class )
    public ResponseEntity<String> handleDuplicateUser ( DuplicateUserException ex ) {
        return ResponseEntity.status( HttpStatus.CONFLICT ).body( ex.getMessage() );
    }

}
