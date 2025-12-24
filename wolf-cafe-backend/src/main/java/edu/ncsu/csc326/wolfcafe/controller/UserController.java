package edu.ncsu.csc326.wolfcafe.controller;

import java.util.List;
import java.util.stream.Collectors;

import edu.ncsu.csc326.wolfcafe.exception.DuplicateUserException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.ncsu.csc326.wolfcafe.dto.RegisterDto;
import edu.ncsu.csc326.wolfcafe.dto.UserDto;
import edu.ncsu.csc326.wolfcafe.entity.Role;
import edu.ncsu.csc326.wolfcafe.entity.User;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.mapper.UserMapper;
import edu.ncsu.csc326.wolfcafe.repository.RoleRepository;
import edu.ncsu.csc326.wolfcafe.repository.UserRepository;
import edu.ncsu.csc326.wolfcafe.service.UserRegistrationService;
import lombok.AllArgsConstructor;

/**
 * Admin-facing user management endpoints (CRUD) plus role lookup.
 */
@RestController
@RequestMapping ( "/api" )
@AllArgsConstructor
@CrossOrigin ( "*" )
public class UserController {

    /** Repository for users */
    private final UserRepository          userRepository;

    /** Repository for roles */
    private final RoleRepository          roleRepository;

    /** Existing registration service (handles validation + password policy) */
    private final UserRegistrationService userRegistrationService;

    /**
     * Returns all users in the system. Requires ADMIN role.
     *
     * @return list of users as DTOs
     */
    @PreAuthorize ( "hasRole('ADMIN')" )
    @GetMapping ( "/users" )
    public ResponseEntity<List<UserDto>> getAllUsers () {
        final List<User> users = userRepository.findAll();
        final List<UserDto> dtos = users.stream().map( UserMapper::mapToUserDto ).collect( Collectors.toList() );
        return ResponseEntity.ok( dtos );
    }

    /**
     * Returns a single user by id. Requires ADMIN role.
     *
     * @param id
     *            user id
     * @return user DTO
     */
    @PreAuthorize ( "hasRole('ADMIN')" )
    @GetMapping ( "/users/{id}" )
    public ResponseEntity<UserDto> getUserById ( @PathVariable ( "id" ) final Long id ) {
        final User user = userRepository.findById( id )
                .orElseThrow( () -> new ResourceNotFoundException( "User not found with id " + id ) );
        return ResponseEntity.ok( UserMapper.mapToUserDto( user ) );
    }

    /**
     * Creates a new user account as an ADMIN action. Uses the existing
     * registration pipeline to enforce password rules.
     *
     * @param dto
     *            registration data
     * @return created user as DTO
     */
    @PreAuthorize ( "hasRole('ADMIN')" )
    @PostMapping ( value = "/users", consumes = "application/json" )
    public ResponseEntity<UserDto> createUser ( @RequestBody @Valid final RegisterDto dto ) {
        final User created = userRegistrationService.register( dto );
        final UserDto body = UserMapper.mapToUserDto( created );
        return ResponseEntity.status( HttpStatus.CREATED ).body( body );
    }

    /**
     * Updates basic information and roles for a user. Requires ADMIN.
     *
     * @param id
     *            user id to update
     * @param dto
     *            updated fields (name, username, email, roles)
     * @return updated user DTO
     */
    @PreAuthorize ( "hasRole('ADMIN')" )
    @PutMapping ( "/users/{id}" )
    public ResponseEntity<UserDto> updateUser ( @PathVariable ( "id" ) final Long id, @RequestBody final UserDto dto ) {

        final User user = userRepository.findById( id )
                .orElseThrow( () -> new ResourceNotFoundException( "User not found with id " + id ) );

        if ( dto.getName() != null ) {
            user.setName( dto.getName() );
        }
        if ( dto.getUsername() != null ) {
            user.setUsername( dto.getUsername() );
        }
        if ( dto.getEmail() != null ) {
            user.setEmail( dto.getEmail() );
        }

        if ( dto.getRoles() != null && !dto.getRoles().isEmpty() ) {
            final List<Role> newRoles = dto.getRoles().stream().map( roleName -> {
                String dbName = roleName;
                // Allow UI to send either "ADMIN" or "ROLE_ADMIN"
                if ( !dbName.startsWith( "ROLE_" ) ) {
                    dbName = "ROLE_" + dbName;
                }
                final Role role = roleRepository.findByName( dbName );
                if ( role == null ) {
                    throw new IllegalArgumentException( "Unknown role: " + roleName );
                }
                return role;
            } ).collect( Collectors.toList() );
            user.setRoles( newRoles );
        }

        final User saved = userRepository.save( user );
        return ResponseEntity.ok( UserMapper.mapToUserDto( saved ) );
    }

    /**
     * Deletes the given user. Requires ADMIN role.
     *
     * @param id
     *            id of user to delete
     * @return response indicating success or failure
     */
    @PreAuthorize ( "hasRole('ADMIN')" )
    @DeleteMapping ( "/users/{id}" )
    public ResponseEntity<String> deleteUser ( @PathVariable ( "id" ) final Long id ) {
        userRepository.findById( id )
                .orElseThrow( () -> new ResourceNotFoundException( "User not found with id " + id ) );
        userRepository.deleteById( id );
        return ResponseEntity.ok( "User deleted successfully." );
    }

    /**
     * Returns all role names from the database. Requires ADMIN role.
     *
     * @return list of role names (e.g., ["ROLE_ADMIN", "ROLE_CUSTOMER"])
     */
    @PreAuthorize ( "hasRole('ADMIN')" )
    @GetMapping ( "/roles" )
    public ResponseEntity<List<String>> getAllRoles () {
        final List<String> roles = roleRepository.findAll().stream().map( Role::getName )
                .collect( Collectors.toList() );
        return ResponseEntity.ok( roles );
    }

    /**
     * Maps illegal arguments to HTTP 400 responses.
     *
     * @param ex
     *            thrown exception
     * @return bad request response with error message
     */
    @ExceptionHandler ( IllegalArgumentException.class )
    public ResponseEntity<String> handleIllegalArgument ( final IllegalArgumentException ex ) {
        return ResponseEntity.badRequest().body( ex.getMessage() );
    }
}
