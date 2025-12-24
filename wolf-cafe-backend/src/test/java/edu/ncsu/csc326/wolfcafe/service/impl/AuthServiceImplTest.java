package edu.ncsu.csc326.wolfcafe.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import edu.ncsu.csc326.wolfcafe.dto.JwtAuthResponse;
import edu.ncsu.csc326.wolfcafe.dto.LoginDto;
import edu.ncsu.csc326.wolfcafe.dto.RegisterDto;
import edu.ncsu.csc326.wolfcafe.entity.Role;
import edu.ncsu.csc326.wolfcafe.entity.User;
import edu.ncsu.csc326.wolfcafe.exception.ResourceNotFoundException;
import edu.ncsu.csc326.wolfcafe.exception.WolfCafeAPIException;
import edu.ncsu.csc326.wolfcafe.repository.RoleRepository;
import edu.ncsu.csc326.wolfcafe.repository.UserRepository;
import edu.ncsu.csc326.wolfcafe.security.JwtTokenProvider;

/**
 * Unit tests for {@link AuthServiceImpl}.
 */
@ExtendWith ( MockitoExtension.class )
public class AuthServiceImplTest {

    /** Mock user repository */
    @Mock
    private UserRepository        userRepository;

    /** Mock role repository */
    @Mock
    private RoleRepository        roleRepository;

    /** Mock password encoder */
    @Mock
    private PasswordEncoder       passwordEncoder;

    /** Mock authentication manager */
    @Mock
    private AuthenticationManager authenticationManager;

    /** Mock JWT token provider */
    @Mock
    private JwtTokenProvider      jwtTokenProvider;

    /** Service under test */
    @InjectMocks
    private AuthServiceImpl       authService;

    /**
     * Clear the security context before each test.
     */
    @BeforeEach
    public void setup () {
        SecurityContextHolder.clearContext();
    }

    /**
     * Tests successfully registering a new user.
     */
    @Test
    public void testRegisterSuccess () {
        final RegisterDto dto = new RegisterDto( "Test User", "testuser", "test@example.com", "password" );

        when( userRepository.existsByUsername( "testuser" ) ).thenReturn( false );
        when( userRepository.existsByEmail( "test@example.com" ) ).thenReturn( false );
        when( passwordEncoder.encode( "password" ) ).thenReturn( "encodedPassword" );

        final Role role = new Role();
        role.setId( 1L );
        role.setName( "ROLE_CUSTOMER" );
        when( roleRepository.findByName( "ROLE_CUSTOMER" ) ).thenReturn( role );

        final String response = authService.register( dto );

        assertEquals( "User registered successfully.", response );
        verify( userRepository ).save( any( User.class ) );
    }

    /**
     * Tests that registering with a duplicate username throws an exception.
     */
    @Test
    public void testRegisterDuplicateUsername () {
        final RegisterDto dto = new RegisterDto( "Test User", "testuser", "test@example.com", "password" );

        when( userRepository.existsByUsername( "testuser" ) ).thenReturn( true );

        final WolfCafeAPIException ex = assertThrows( WolfCafeAPIException.class, () -> authService.register( dto ) );

        assertTrue( ex.getMessage().contains( "Username already exists" ) );
        verify( userRepository, never() ).save( any( User.class ) );
    }

    /**
     * Tests that registering with a duplicate email throws an exception.
     */
    @Test
    public void testRegisterDuplicateEmail () {
        final RegisterDto dto = new RegisterDto( "Test User", "testuser", "test@example.com", "password" );

        when( userRepository.existsByUsername( "testuser" ) ).thenReturn( false );
        when( userRepository.existsByEmail( "test@example.com" ) ).thenReturn( true );

        final WolfCafeAPIException ex = assertThrows( WolfCafeAPIException.class, () -> authService.register( dto ) );

        assertTrue( ex.getMessage().contains( "Email already exists" ) );
        verify( userRepository, never() ).save( any( User.class ) );
    }

    /**
     * Tests logging in successfully and returning a token and role.
     */
    @Test
    public void testLoginSuccess () {
        final LoginDto loginDto = new LoginDto( "testuser", "password" );

        final Authentication authentication = new UsernamePasswordAuthenticationToken( loginDto.getUsernameOrEmail(),
                loginDto.getPassword() );

        when( authenticationManager.authenticate( any( UsernamePasswordAuthenticationToken.class ) ) )
                .thenReturn( authentication );
        when( jwtTokenProvider.generateToken( authentication ) ).thenReturn( "jwt-token" );

        final Role role = new Role();
        role.setId( 1L );
        role.setName( "ROLE_CUSTOMER" );

        final User user = new User();
        user.setRoles( Collections.singletonList( role ) );

        when( userRepository.findByUsernameOrEmail( "testuser", "testuser" ) ).thenReturn( Optional.of( user ) );

        final JwtAuthResponse response = authService.login( loginDto );

        assertEquals( "jwt-token", response.getAccessToken() );
        assertEquals( "ROLE_CUSTOMER", response.getRole() );
    }

    /**
     * Tests logging in when the user has no roles associated.
     */
    @Test
    public void testLoginUserWithoutRole () {
        final LoginDto loginDto = new LoginDto( "testuser", "password" );

        final Authentication authentication = new UsernamePasswordAuthenticationToken( loginDto.getUsernameOrEmail(),
                loginDto.getPassword() );

        when( authenticationManager.authenticate( any( UsernamePasswordAuthenticationToken.class ) ) )
                .thenReturn( authentication );
        when( jwtTokenProvider.generateToken( authentication ) ).thenReturn( "jwt-token" );

        final User user = new User();
        user.setRoles( Collections.emptyList() );

        when( userRepository.findByUsernameOrEmail( "testuser", "testuser" ) ).thenReturn( Optional.of( user ) );

        final JwtAuthResponse response = authService.login( loginDto );

        assertEquals( "jwt-token", response.getAccessToken() );
        assertNull( response.getRole() );
    }

    /**
     * Tests deleting a user by id when the user exists.
     */
    @Test
    public void testDeleteUserByIdSuccess () {
        User user = new User();
        user.setId( 99L );

        when( userRepository.findById( 99L ) ).thenReturn( Optional.of( user ) );

        authService.deleteUserById( 99L );

        verify( userRepository ).findById( 99L );
        verify( userRepository ).save( user );
        verify( userRepository ).delete( user );
    }

    /**
     * Tests deleting a user by id when the user does not exist.
     */
    @Test
    public void testDeleteUserByIdNotFound () {
        when( userRepository.findById( anyLong() ) ).thenReturn( Optional.empty() );

        assertThrows( ResourceNotFoundException.class, () -> authService.deleteUserById( 42L ) );

        verify( userRepository, never() ).deleteById( anyLong() );
    }
}
