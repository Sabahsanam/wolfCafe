package edu.ncsu.csc326.wolfcafe.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Information needed to register a new customer.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDto {

    /** User's name */
    private String name;

    /** User's username */
    private String username;

    /** User's email */
    @Email ( message = "Invalid email address." )
    private String email;

    /** User's password */
    private String password;
}
