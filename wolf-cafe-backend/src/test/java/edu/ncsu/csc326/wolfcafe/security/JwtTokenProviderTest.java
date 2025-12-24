package edu.ncsu.csc326.wolfcafe.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for {@link JwtTokenProvider}.
 */
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp () {
        jwtTokenProvider = new JwtTokenProvider();

        final String secret = Base64.getEncoder()
                .encodeToString( "test-secret-key-for-jwt-provider-123".getBytes( StandardCharsets.UTF_8 ) );

        ReflectionTestUtils.setField( jwtTokenProvider, "jwtSecret", secret );
        ReflectionTestUtils.setField( jwtTokenProvider, "jwtExpirationDate", 3_600_000L ); // 1
                                                                                           // hour
    }

    @Test
    void testGetUsernameReturnsSubjectFromToken () {
        final Authentication authentication = new UsernamePasswordAuthenticationToken( "jdoe", "password",
                Collections.emptyList() );

        final String token = jwtTokenProvider.generateToken( authentication );

        final String username = jwtTokenProvider.getUsername( token );

        assertEquals( "jdoe", username );
    }

    @Test
    void testValidateTokenReturnsTrueForValidToken () {
        final Authentication authentication = new UsernamePasswordAuthenticationToken( "jdoe", "password",
                Collections.emptyList() );

        final String token = jwtTokenProvider.generateToken( authentication );

        assertTrue( jwtTokenProvider.validateToken( token ) );
    }
}
