package edu.ncsu.csc326.wolfcafe.service.impl;

import java.util.List;

import edu.ncsu.csc326.wolfcafe.exception.DuplicateUserException;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.ncsu.csc326.wolfcafe.dto.RegisterDto;
import edu.ncsu.csc326.wolfcafe.entity.Role;
import edu.ncsu.csc326.wolfcafe.entity.User;
import edu.ncsu.csc326.wolfcafe.repository.RoleRepository;
import edu.ncsu.csc326.wolfcafe.repository.UserRepository;
import edu.ncsu.csc326.wolfcafe.service.UserRegistrationService;
import org.springframework.validation.annotation.Validated;

/**
 * Class holding methods for registering without permissions
 *
 */
@Service
public class UserRegistrationServiceImpl implements UserRegistrationService {

    /** User repository */
    private final UserRepository  userRepository;
    /** role repository */
    private final RoleRepository  roleRepository;
    /** encode for password */
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor for service implementation
     *
     * @param userRepo
     *            repository for users
     * @param roleRepo
     *            repository for roles
     * @param passwordEncoder
     *            encoder for passwords
     */
    public UserRegistrationServiceImpl ( final UserRepository userRepo, final RoleRepository roleRepo,
            final PasswordEncoder passwordEncoder ) {
        this.userRepository = userRepo;
        this.roleRepository = roleRepo;
        this.passwordEncoder = passwordEncoder;

    }

    /**
     * Registers the user in the system as a customer
     *
     * @param registerDto
     *            registration information
     * @return Registered user
     */
    @Override
    @Transactional
    @Validated
    public User register ( @Valid final RegisterDto registerDto ) {
        if ( registerDto.getUsername() == null || registerDto.getUsername().isBlank() ) {
            throw new IllegalArgumentException( "Username is required" );
        }
        if ( registerDto.getPassword() == null || registerDto.getPassword().isBlank() ) {
            throw new IllegalArgumentException( "Password is required" );
        }
        if ( registerDto.getEmail() == null || registerDto.getEmail().isBlank() ) {
            throw new IllegalArgumentException( "Email is required" );
        }

        final String username = registerDto.getUsername().trim();
        final String email = registerDto.getEmail().trim().toLowerCase();

        if ( userRepository.existsByUsername( username ) ) {
            throw new DuplicateUserException( "Username already exists" );
        }
        if ( userRepository.existsByEmail( email ) ) {
            throw new DuplicateUserException( "Email already exists" );
        }

        final User u = new User();
        u.setName( registerDto.getName() != null ? registerDto.getName().trim() : "" );
        u.setUsername( username );
        u.setEmail( email );
        u.setPassword( passwordEncoder.encode( registerDto.getPassword() ) );

        Role role = roleRepository.findByName( "ROLE_CUSTOMER" );
        if ( role == null ) {
            role = new Role();
            role.setName( "ROLE_CUSTOMER" );
            role = roleRepository.save( role );
        }
        u.setRoles( new java.util.HashSet<>( java.util.List.of( role ) ) );

        return userRepository.save( u );
    }

}
