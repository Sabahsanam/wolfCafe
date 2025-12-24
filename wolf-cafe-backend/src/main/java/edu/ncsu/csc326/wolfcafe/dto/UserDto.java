package edu.ncsu.csc326.wolfcafe.dto;

import java.util.List;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Information needed for handling users
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    /** User id */
    private Long         id;

    /** User's display name */
    private String       name;

    /** Unique username used for login */
    private String       username;

    /** User's email address */
    @Email ( message = "Invalid email address." )
    private String       email;

    /**
     * collection of role names for this user
     */
    private List<String> roles;
}
