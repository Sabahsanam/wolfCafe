package edu.ncsu.csc326.wolfcafe.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import edu.ncsu.csc326.wolfcafe.exception.DuplicateUserException;
import edu.ncsu.csc326.wolfcafe.exception.ErrorDetails;
import edu.ncsu.csc326.wolfcafe.exception.GlobalExceptionHandler;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ncsu.csc326.wolfcafe.WolfCafeApplication;
import edu.ncsu.csc326.wolfcafe.dto.RegisterDto;
import edu.ncsu.csc326.wolfcafe.entity.Role;
import edu.ncsu.csc326.wolfcafe.entity.User;
import edu.ncsu.csc326.wolfcafe.repository.RoleRepository;
import edu.ncsu.csc326.wolfcafe.repository.UserRepository;
import edu.ncsu.csc326.wolfcafe.service.UserRegistrationService;
import org.springframework.web.context.request.WebRequest;

/**
 * Tests the {@link UserController}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration ( classes = WolfCafeApplication.class )
public class UserControllerTest {

    /** Mock MVC for testing controller. */
    @Autowired
    private MockMvc                   mvc;

    /** Mocked user repository. */
    @MockitoBean
    private UserRepository            userRepository;

    /** Mocked role repository. */
    @MockitoBean
    private RoleRepository            roleRepository;

    /** Mocked registration service. */
    @MockitoBean
    private UserRegistrationService   userRegistrationService;

    /** JSON object mapper. */
    private static final ObjectMapper MAPPER     = new ObjectMapper();

    /** Base path for user endpoints. */
    private static final String       USERS_PATH = "/api/users";

    /** Path for roles endpoint. */
    private static final String       ROLES_PATH = "/api/roles";

    /** Default encoding. */
    private static final String       ENCODING   = StandardCharsets.UTF_8.name();

    /**
     * Helper to build a Role.
     *
     * @param id
     *            role id
     * @param name
     *            role name
     * @return built role
     */
    private Role buildRole ( final Long id, final String name ) {
        final Role role = new Role();
        role.setId( id );
        role.setName( name );
        return role;
    }

    /**
     * Helper to build a User with a single role.
     *
     * @param id
     *            user id
     * @param name
     *            user name
     * @param username
     *            username
     * @param email
     *            email
     * @param roleName
     *            role name
     * @return user entity
     */
    private User buildUser ( final Long id, final String name, final String username, final String email,
            final String roleName ) {
        final Role role = buildRole( id, roleName );
        final User user = new User();
        user.setId( id );
        user.setName( name );
        user.setUsername( username );
        user.setEmail( email );
        user.setPassword( "encoded-password" );
        user.setRoles( List.of( role ) );
        return user;
    }

    /**
     * Tests that an admin can get the list of all users.
     *
     * @throws Exception
     *             if an error occurs
     */
    @Test
    @Transactional
    @WithMockUser ( username = "admin", roles = "ADMIN" )
    public void testGetAllUsersAsAdmin () throws Exception {
        final User user = buildUser( 1L, "Admin User", "admin", "admin@example.com", "ROLE_ADMIN" );
        Mockito.when( userRepository.findAll() ).thenReturn( Collections.singletonList( user ) );

        mvc.perform( get( USERS_PATH ).accept( MediaType.APPLICATION_JSON ).characterEncoding( ENCODING ) )
                .andExpect( status().isOk() ).andExpect( jsonPath( "$[0].id" ).value( 1 ) )
                .andExpect( jsonPath( "$[0].name" ).value( "Admin User" ) )
                .andExpect( jsonPath( "$[0].username" ).value( "admin" ) )
                .andExpect( jsonPath( "$[0].email" ).value( "admin@example.com" ) );

        Mockito.verify( userRepository ).findAll();
    }

    /**
     * Tests that an admin can get a single user by id.
     *
     * @throws Exception
     *             if an error occurs
     */
    @Test
    @Transactional
    @WithMockUser ( username = "admin", roles = "ADMIN" )
    public void testGetUserById () throws Exception {
        final User user = buildUser( 2L, "Bob", "bob", "bob@example.com", "ROLE_CUSTOMER" );
        Mockito.when( userRepository.findById( 2L ) ).thenReturn( Optional.of( user ) );

        mvc.perform( get( USERS_PATH + "/2" ).accept( MediaType.APPLICATION_JSON ).characterEncoding( ENCODING ) )
                .andExpect( status().isOk() ).andExpect( jsonPath( "$.id" ).value( 2 ) )
                .andExpect( jsonPath( "$.name" ).value( "Bob" ) ).andExpect( jsonPath( "$.username" ).value( "bob" ) )
                .andExpect( jsonPath( "$.email" ).value( "bob@example.com" ) );

        Mockito.verify( userRepository ).findById( 2L );
    }

    /**
     * Tests that an admin can create a new user.
     *
     * @throws Exception
     *             if an error occurs
     */
    @Test
    @Transactional
    @WithMockUser ( username = "admin", roles = "ADMIN" )
    public void testCreateUser () throws Exception {
        final RegisterDto registerDto = new RegisterDto( "Carol", "carol", "carol@example.com", "password123" );
        final User created = buildUser( 3L, "Carol", "carol", "carol@example.com", "ROLE_CUSTOMER" );

        Mockito.when( userRegistrationService.register( ArgumentMatchers.any( RegisterDto.class ) ) )
                .thenReturn( created );

        final String json = MAPPER.writeValueAsString( registerDto );

        mvc.perform( post( USERS_PATH ).contentType( MediaType.APPLICATION_JSON ).characterEncoding( ENCODING )
                .content( json ).accept( MediaType.APPLICATION_JSON ) ).andExpect( status().isCreated() )
                .andExpect( jsonPath( "$.id" ).value( 3 ) ).andExpect( jsonPath( "$.name" ).value( "Carol" ) )
                .andExpect( jsonPath( "$.username" ).value( "carol" ) )
                .andExpect( jsonPath( "$.email" ).value( "carol@example.com" ) );

        Mockito.verify( userRegistrationService ).register( ArgumentMatchers.any( RegisterDto.class ) );
    }

    @Test
    @Transactional
    @WithMockUser ( username = "admin", roles = "ADMIN" )
    public void testCreateUserWithExistingUsername () throws Exception {
        final RegisterDto registerDto = new RegisterDto( "Duplicate", "existinguser", "new@example.com",
                "password123" );
        Mockito.when( userRegistrationService.register( ArgumentMatchers.any( RegisterDto.class ) ) )
                .thenThrow( new DuplicateUserException( "Username already exists" ) );
        final String json = MAPPER.writeValueAsString( registerDto );

        mvc.perform( post( USERS_PATH ).contentType( MediaType.APPLICATION_JSON ).characterEncoding( ENCODING )
                .content( json ).accept( MediaType.APPLICATION_JSON ) ).andExpect( status().isConflict() )
                .andExpect( content().string( Matchers.containsString( "Username already exists" ) ) );
        Mockito.verify( userRegistrationService ).register( ArgumentMatchers.any( RegisterDto.class ) );
    }

    @Test
    @Transactional
    @WithMockUser ( username = "admin", roles = "ADMIN" )
    public void testCreateUserWithInvalidEmail () throws Exception {

        // invalid email format
        RegisterDto registerDto = new RegisterDto( "Invalid Email User", "someuser", "notanemail", // <-
                                                                                                   // invalid
                "password123" );

        String json = MAPPER.writeValueAsString( registerDto );

        mvc.perform( post( USERS_PATH ).contentType( MediaType.APPLICATION_JSON ).characterEncoding( ENCODING )
                .content( json ).accept( MediaType.APPLICATION_JSON ) ).andExpect( status().isBadRequest() )
                .andExpect( jsonPath( "$.message" ).exists() );

        // Service should NEVER be called because validation fails earlier
        Mockito.verify( userRegistrationService, Mockito.never() )
                .register( ArgumentMatchers.any( RegisterDto.class ) );
    }

    /**
     * Tests that an admin can update a user including roles.
     *
     * @throws Exception
     *             if an error occurs
     */
    @Test
    @Transactional
    @WithMockUser ( username = "admin", roles = "ADMIN" )
    public void testUpdateUser () throws Exception {
        final User existing = buildUser( 4L, "Dave Old", "dave_old", "old@example.com", "ROLE_CUSTOMER" );
        Mockito.when( userRepository.findById( 4L ) ).thenReturn( Optional.of( existing ) );

        final Role adminRole = buildRole( 10L, "ROLE_ADMIN" );
        Mockito.when( roleRepository.findByName( "ROLE_ADMIN" ) ).thenReturn( adminRole );

        final User saved = buildUser( 4L, "Dave New", "dave", "new@example.com", "ROLE_ADMIN" );
        Mockito.when( userRepository.save( ArgumentMatchers.any( User.class ) ) ).thenReturn( saved );

        final String json = "{" + "\"name\":\"Dave New\"," + "\"username\":\"dave\"," + "\"email\":\"new@example.com\","
                + "\"roles\":[\"ADMIN\"]" + "}";

        mvc.perform( put( USERS_PATH + "/4" ).contentType( MediaType.APPLICATION_JSON ).characterEncoding( ENCODING )
                .content( json ).accept( MediaType.APPLICATION_JSON ) ).andExpect( status().isOk() )
                .andExpect( jsonPath( "$.id" ).value( 4 ) ).andExpect( jsonPath( "$.name" ).value( "Dave New" ) )
                .andExpect( jsonPath( "$.username" ).value( "dave" ) )
                .andExpect( jsonPath( "$.email" ).value( "new@example.com" ) );

        Mockito.verify( userRepository ).findById( 4L );
        Mockito.verify( roleRepository ).findByName( "ROLE_ADMIN" );
        Mockito.verify( userRepository ).save( ArgumentMatchers.any( User.class ) );
    }

    /**
     * Tests that updating a user with an unknown role results in a bad request
     * (IllegalArgumentException handled by the controller).
     *
     * @throws Exception
     *             if an error occurs
     */
    @Test
    @Transactional
    @WithMockUser ( username = "admin", roles = "ADMIN" )
    public void testUpdateUserUnknownRole () throws Exception {
        final User existing = buildUser( 5L, "Eve", "eve", "eve@example.com", "ROLE_CUSTOMER" );
        Mockito.when( userRepository.findById( 5L ) ).thenReturn( Optional.of( existing ) );

        // roleRepository returns null to trigger IllegalArgumentException in
        // controller
        Mockito.when( roleRepository.findByName( "ROLE_MANAGER" ) ).thenReturn( null );

        final String json = "{" + "\"roles\":[\"MANAGER\"]" + "}";

        mvc.perform( put( USERS_PATH + "/5" ).contentType( MediaType.APPLICATION_JSON ).characterEncoding( ENCODING )
                .content( json ).accept( MediaType.APPLICATION_JSON ) ).andExpect( status().isBadRequest() )
                .andExpect( content().string( Matchers.containsString( "Unknown role" ) ) );

        Mockito.verify( userRepository ).findById( 5L );
        Mockito.verify( roleRepository ).findByName( "ROLE_MANAGER" );
        Mockito.verify( userRepository, Mockito.never() ).save( ArgumentMatchers.any( User.class ) );
    }

    /**
     * Tests that an admin can delete a user.
     *
     * @throws Exception
     *             if an error occurs
     */
    @Test
    @Transactional
    @WithMockUser ( username = "admin", roles = "ADMIN" )
    public void testDeleteUser () throws Exception {
        final User existing = buildUser( 6L, "Frank", "frank", "frank@example.com", "ROLE_CUSTOMER" );
        Mockito.when( userRepository.findById( 6L ) ).thenReturn( Optional.of( existing ) );

        mvc.perform( delete( USERS_PATH + "/6" ).accept( MediaType.TEXT_PLAIN ).characterEncoding( ENCODING ) )
                .andExpect( status().isOk() ).andExpect( content().string( "User deleted successfully." ) );

        Mockito.verify( userRepository ).findById( 6L );
        Mockito.verify( userRepository ).deleteById( 6L );
    }

    /**
     * Tests that an admin can get the list of roles.
     *
     * @throws Exception
     *             if an error occurs
     */
    @Test
    @Transactional
    @WithMockUser ( username = "admin", roles = "ADMIN" )
    public void testGetAllRoles () throws Exception {
        final Role admin = buildRole( 1L, "ROLE_ADMIN" );
        final Role staff = buildRole( 2L, "ROLE_STAFF" );

        Mockito.when( roleRepository.findAll() ).thenReturn( List.of( admin, staff ) );

        mvc.perform( get( ROLES_PATH ).accept( MediaType.APPLICATION_JSON ).characterEncoding( ENCODING ) )
                .andExpect( status().isOk() ).andExpect( jsonPath( "$[0]" ).value( "ROLE_ADMIN" ) )
                .andExpect( jsonPath( "$[1]" ).value( "ROLE_STAFF" ) );

        Mockito.verify( roleRepository ).findAll();
    }

    @Test
    void testHandleConstraintViolationReturnsBadRequest () {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        @SuppressWarnings ( "unchecked" )
        ConstraintViolation<Object> violation = Mockito.mock( ConstraintViolation.class );
        Mockito.when( violation.getMessage() ).thenReturn( "Some constraint was violated" );

        ConstraintViolationException ex = new ConstraintViolationException( "Validation failed", Set.of( violation ) );

        WebRequest request = Mockito.mock( WebRequest.class );
        Mockito.when( request.getDescription( false ) ).thenReturn( "uri=/api/something" );

        ResponseEntity<ErrorDetails> response = handler.handleConstraintViolation( ex, request );

        assertEquals( HttpStatus.BAD_REQUEST, response.getStatusCode() );
        assertNotNull( response.getBody() );
        assertEquals( "Some constraint was violated", response.getBody().getMessage() );
    }
}
