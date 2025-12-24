package edu.ncsu.csc326.wolfcafe.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.ncsu.csc326.wolfcafe.exception.DuplicateUserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import edu.ncsu.csc326.wolfcafe.dto.RegisterDto;
import edu.ncsu.csc326.wolfcafe.entity.Role;
import edu.ncsu.csc326.wolfcafe.entity.User;
import edu.ncsu.csc326.wolfcafe.repository.RoleRepository;
import edu.ncsu.csc326.wolfcafe.repository.UserRepository;
import edu.ncsu.csc326.wolfcafe.service.impl.UserRegistrationServiceImpl;

/**
 * Class testing userregistrationserverImpl methods
 *
 */
@ExtendWith ( MockitoExtension.class )
class UserRegistrationServiceImplTest {

    /** Customer role constant */
    private static final String         ROLE_CUSTOMER = "ROLE_CUSTOMER";

    @Mock
    private UserRepository              userRepository;
    @Mock
    private RoleRepository              roleRepository;
    @Mock
    private PasswordEncoder             passwordEncoder;

    private UserRegistrationServiceImpl service;

    /**
     * Sets up service for each test
     */
    @BeforeEach
    void setup () {
        service = new UserRegistrationServiceImpl( userRepository, roleRepository, passwordEncoder );
    }

    /**
     * Makes a register dto for use in tests
     *
     * @param name
     * @param username
     * @param email
     * @param password
     * @return dto
     */
    private RegisterDto dto ( final String name, final String username, final String email, final String password ) {
        final RegisterDto d = new RegisterDto();
        d.setName( name );
        d.setUsername( username );
        d.setEmail( email );
        d.setPassword( password );
        return d;
    }

    @Nested
    @DisplayName ( "Successful registration" )
    class SuccessCases {

        /**
         * Tests registering with a real role
         */
        @Test
        @DisplayName ( "Registers with existing ROLE_CUSTOMER" )
        void registersWithExistingRole () {
            final RegisterDto dto = dto( "  Jane  ", "  jane  ", "  JANE@EXAMPLE.COM  ", "s3cret!" );

            when( userRepository.existsByUsername( "jane" ) ).thenReturn( false );
            when( userRepository.existsByEmail( "jane@example.com" ) ).thenReturn( false );

            final Role existingRole = new Role();
            existingRole.setId( 10L );
            existingRole.setName( ROLE_CUSTOMER );
            when( roleRepository.findByName( ROLE_CUSTOMER ) ).thenReturn( existingRole );

            when( passwordEncoder.encode( "s3cret!" ) ).thenReturn( "ENC(s3cret!)" );

            // return saved user with id
            when( userRepository.save( any( User.class ) ) ).thenAnswer( inv -> {
                final User u = inv.getArgument( 0, User.class );
                u.setId( 42L );
                return u;
            } );

            final User savedReturned = service.register( dto );
            assertNotNull( savedReturned );
            assertEquals( 42L, savedReturned.getId() );

            final ArgumentCaptor<User> captor = ArgumentCaptor.forClass( User.class );
            verify( userRepository ).save( captor.capture() );
            final User saved = captor.getValue();

            assertEquals( "Jane", saved.getName() );
            assertEquals( "jane", saved.getUsername() );
            assertEquals( "jane@example.com", saved.getEmail() );

            assertEquals( "ENC(s3cret!)", saved.getPassword() );
            assertNotNull( saved.getRoles() );
            assertEquals( 1, saved.getRoles().size() );
            assertTrue( saved.getRoles().stream().anyMatch( r -> ROLE_CUSTOMER.equals( r.getName() ) ) );
        }

        /**
         * Creates a role when no customer role present
         */
        @Test
        @DisplayName ( "Registers and creates ROLE_CUSTOMER when missing" )
        void createsRoleWhenMissing () {
            final RegisterDto dto = dto( "Pat", "pat", "pat@example.com", "pw" );

            when( userRepository.existsByUsername( "pat" ) ).thenReturn( false );
            when( userRepository.existsByEmail( "pat@example.com" ) ).thenReturn( false );

            // role missing initially
            when( roleRepository.findByName( ROLE_CUSTOMER ) ).thenReturn( null );

            // role gets created
            when( roleRepository.save( any( Role.class ) ) ).thenAnswer( inv -> {
                final Role r = inv.getArgument( 0, Role.class );
                r.setId( 99L );
                return r;
            } );

            when( passwordEncoder.encode( "pw" ) ).thenReturn( "ENC(pw)" );

            when( userRepository.save( any( User.class ) ) ).thenAnswer( inv -> {
                final User u = inv.getArgument( 0, User.class );
                u.setId( 7L );
                return u;
            } );

            final User savedReturned = service.register( dto );
            assertNotNull( savedReturned );
            assertEquals( 7L, savedReturned.getId() );

            // verify role creation
            final ArgumentCaptor<Role> roleCap = ArgumentCaptor.forClass( Role.class );
            verify( roleRepository ).save( roleCap.capture() );
            assertEquals( ROLE_CUSTOMER, roleCap.getValue().getName() );

            // verify user roles contain newly created role
            final ArgumentCaptor<User> userCap = ArgumentCaptor.forClass( User.class );
            verify( userRepository ).save( userCap.capture() );
            final User saved = userCap.getValue();
            assertEquals( 1, saved.getRoles().size() );
            final Role onlyRole = saved.getRoles().iterator().next();
            assertEquals( 99L, onlyRole.getId() );
            assertEquals( ROLE_CUSTOMER, onlyRole.getName() );
        }
    }

    /**
     * class for testing failure conditions within the registration service
     *
     */
    @Nested
    @DisplayName ( "Validation & conflicts" )
    class FailureCases {

        /**
         * Testing for failure witha duplicate username
         */
        @Test
        @DisplayName ( "Rejects duplicate username" )
        void duplicateUsername () {
            final RegisterDto dto = dto( "A", "alex", "alex@example.com", "pw" );
            when( userRepository.existsByUsername( "alex" ) ).thenReturn( true );

            final DuplicateUserException ex = assertThrows( DuplicateUserException.class,
                    () -> service.register( dto ) );
            assertTrue( ex.getMessage().toLowerCase().contains( "username" ) );
            verify( userRepository, never() ).save( any() );
        }

        /**
         * Testing for failure with a duplicate email
         */
        @Test
        @DisplayName ( "Rejects duplicate email" )
        void duplicateEmail () {
            final RegisterDto dto = dto( "A", "alex", "alex@example.com", "pw" );
            when( userRepository.existsByUsername( "alex" ) ).thenReturn( false );
            when( userRepository.existsByEmail( "alex@example.com" ) ).thenReturn( true );

            final DuplicateUserException ex = assertThrows( DuplicateUserException.class,
                    () -> service.register( dto ) );
            assertTrue( ex.getMessage().toLowerCase().contains( "email" ) );
            verify( userRepository, never() ).save( any() );
        }

        /**
         * Testing for failure with no username
         */
        @Test
        @DisplayName ( "Requires username" )
        void requiresUsername () {
            final RegisterDto dto = dto( "A", "  ", "a@example.com", "pw" );
            final IllegalArgumentException ex = assertThrows( IllegalArgumentException.class,
                    () -> service.register( dto ) );
            assertTrue( ex.getMessage().toLowerCase().contains( "username" ) );
            verify( userRepository, never() ).save( any() );
        }

        /**
         * Testing for failure with no email
         */
        @Test
        @DisplayName ( "Requires email" )
        void requiresEmail () {
            final RegisterDto dto = dto( "A", "alpha", "   ", "pw" );
            final IllegalArgumentException ex = assertThrows( IllegalArgumentException.class,
                    () -> service.register( dto ) );
            assertTrue( ex.getMessage().toLowerCase().contains( "email" ) );
            verify( userRepository, never() ).save( any() );
        }

        /**
         * Testing for failure with no password
         */
        @Test
        @DisplayName ( "Requires password" )
        void requiresPassword () {
            final RegisterDto dto = dto( "A", "alpha", "a@example.com", "   " );
            final IllegalArgumentException ex = assertThrows( IllegalArgumentException.class,
                    () -> service.register( dto ) );
            assertTrue( ex.getMessage().toLowerCase().contains( "password" ) );
            verify( userRepository, never() ).save( any() );
        }
    }
}
