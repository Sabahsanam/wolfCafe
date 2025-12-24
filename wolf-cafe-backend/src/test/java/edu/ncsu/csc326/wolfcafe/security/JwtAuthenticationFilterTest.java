package edu.ncsu.csc326.wolfcafe.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;

import jakarta.servlet.ServletException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Unit tests for {@link JwtAuthenticationFilter}.
 */
class JwtAuthenticationFilterTest {

    @AfterEach
    void tearDown () {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testDoFilterInternalSetsAuthenticationWhenTokenValid () throws ServletException, IOException {
        final JwtTokenProvider jwtTokenProvider = Mockito.mock( JwtTokenProvider.class );
        final UserDetailsService userDetailsService = Mockito.mock( UserDetailsService.class );

        final JwtAuthenticationFilter filter = new JwtAuthenticationFilter( jwtTokenProvider, userDetailsService );

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader( "Authorization", "Bearer valid.jwt.token" );
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockFilterChain chain = new MockFilterChain();

        when( jwtTokenProvider.validateToken( eq( "valid.jwt.token" ) ) ).thenReturn( true );
        when( jwtTokenProvider.getUsername( eq( "valid.jwt.token" ) ) ).thenReturn( "jdoe" );

        final UserDetails userDetails = new User( "jdoe", "password", Collections.emptyList() );
        when( userDetailsService.loadUserByUsername( "jdoe" ) ).thenReturn( userDetails );

        filter.doFilterInternal( request, response, chain );

        assertNotNull( SecurityContextHolder.getContext().getAuthentication() );
        assertEquals( "jdoe", SecurityContextHolder.getContext().getAuthentication().getName() );
    }
}
