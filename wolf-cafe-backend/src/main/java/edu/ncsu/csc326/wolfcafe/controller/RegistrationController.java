package edu.ncsu.csc326.wolfcafe.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.ncsu.csc326.wolfcafe.dto.RegisterDto;
import edu.ncsu.csc326.wolfcafe.service.UserRegistrationService;

@RestController
@RequestMapping ( "/api/auth" )
public class RegistrationController {

    private final UserRegistrationService registrationService;

    public RegistrationController ( final UserRegistrationService registrationService ) {
        this.registrationService = registrationService;
    }

    @PostMapping ( value = "/register", consumes = "application/json" )
    public ResponseEntity< ? > register ( @RequestBody final RegisterDto dto ) {
        registrationService.register( dto );
        return ResponseEntity.status( HttpStatus.CREATED ).body( "User registered successfully." );
    }
}
