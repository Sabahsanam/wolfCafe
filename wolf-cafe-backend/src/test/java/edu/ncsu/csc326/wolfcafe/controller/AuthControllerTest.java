package edu.ncsu.csc326.wolfcafe.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ncsu.csc326.wolfcafe.TestUtils;
import edu.ncsu.csc326.wolfcafe.dto.LoginDto;
import edu.ncsu.csc326.wolfcafe.dto.RegisterDto;
import edu.ncsu.csc326.wolfcafe.repository.UserRepository;

/**
 * Tests the authorization controller.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    /** Admin password from application.properties */
    @Value ( "${app.admin-user-password}" )
    private String             adminUserPassword;

    /** Mocked MVC for testing */
    @Autowired
    private MockMvc            mvc;

    /** User repo to look up IDs created during tests */
    @Autowired
    private UserRepository     userRepository;

    /** Auth controller (used for direct coverage of register()) */
    @Autowired
    private AuthController     authController;

    /** JSON mapper for extracting tokens, etc. */
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Tests logging in as an admin user.
     *
     * @throws Exception
     *             if error
     */
    @Test
    @Transactional
    public void testLoginAdmin () throws Exception {
        final LoginDto loginDto = new LoginDto( "admin", adminUserPassword );

        mvc.perform( post( "/api/auth/login" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( loginDto ) ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() ).andExpect( jsonPath( "$.tokenType" ).value( "Bearer" ) )
                .andExpect( jsonPath( "$.role" ).value( "ROLE_ADMIN" ) );
    }

    /**
     * Tests creating a customer user via the JSON /api/auth/register endpoint
     * (handled by RegistrationController) and then logging in.
     *
     * @throws Exception
     *             if error
     */
    @Test
    @Transactional
    public void testCreateCustomerAndLogin () throws Exception {
        final RegisterDto registerDto = new RegisterDto( "Jordan Estes", "jestes", "vitae.erat@yahoo.edu",
                "JXB16TBD4LC" );

        mvc.perform( post( "/api/auth/register" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( registerDto ) ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isCreated() ).andExpect( content().string( "User registered successfully." ) );

        final LoginDto loginDto = new LoginDto( "jestes", "JXB16TBD4LC" );

        mvc.perform( post( "/api/auth/login" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( loginDto ) ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() ).andExpect( jsonPath( "$.tokenType" ).value( "Bearer" ) )
                .andExpect( jsonPath( "$.role" ).value( "ROLE_CUSTOMER" ) );
    }

    /**
     * Directly exercises AuthController.register so that the controller method
     * that uses AuthService.register() is covered (the JSON endpoint is handled
     * by RegistrationController instead).
     */
    @Test
    @Transactional
    public void testRegister_viaAuthControllerDirect () {
        final RegisterDto dto = new RegisterDto( "Controller User", "controllerUser", "controller@example.edu",
                "CtrlPass123!" );

        final ResponseEntity<String> response = authController.register( dto );

        assertEquals( HttpStatus.CREATED, response.getStatusCode() );
        assertEquals( "User registered successfully.", response.getBody() );
    }

    // ======== Helper: login as admin and get token ========

    /**
     * Logs in as the default admin user and returns the access token.
     *
     * @return JWT access token for admin
     * @throws Exception
     *             if login fails
     */
    private String loginAsAdminAndGetToken () throws Exception {
        final LoginDto loginDto = new LoginDto( "admin", adminUserPassword );

        final String json = mvc
                .perform( post( "/api/auth/login" ).contentType( MediaType.APPLICATION_JSON )
                        .content( TestUtils.asJsonString( loginDto ) ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isOk() ).andExpect( jsonPath( "$.tokenType" ).value( "Bearer" ) )
                .andExpect( jsonPath( "$.role" ).value( "ROLE_ADMIN" ) ).andReturn().getResponse()
                .getContentAsString( StandardCharsets.UTF_8 );

        final JsonNode node = mapper.readTree( json );
        return node.get( "accessToken" ).asText();
    }

    /**
     * Tests deleting a user as admin via DELETE /api/auth/user/{id}.
     */
    @Test
    @Transactional
    public void testDeleteUser_success () throws Exception {
        // Create a user to delete through the JSON registration endpoint
        final RegisterDto reg = new RegisterDto( "Delete Me", "deleteMe", "delete@example.edu", "Delete123!" );
        mvc.perform( post( "/api/auth/register" ).contentType( MediaType.APPLICATION_JSON )
                .content( TestUtils.asJsonString( reg ) ).accept( MediaType.APPLICATION_JSON ) )
                .andExpect( status().isCreated() );

        final Long id = userRepository.findByUsername( "deleteMe" ).orElseThrow().getId();

        final String adminToken = loginAsAdminAndGetToken();

        mvc.perform( delete( "/api/auth/user/{id}", id ).header( "Authorization", "Bearer " + adminToken ) )
                .andExpect( status().isOk() ).andExpect( content().string( "User deleted successfully." ) );
    }
}
