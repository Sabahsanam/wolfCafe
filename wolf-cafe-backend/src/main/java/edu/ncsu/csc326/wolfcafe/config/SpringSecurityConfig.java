package edu.ncsu.csc326.wolfcafe.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import edu.ncsu.csc326.wolfcafe.security.JwtAuthenticationEntryPoint;
import edu.ncsu.csc326.wolfcafe.security.JwtAuthenticationFilter;

/**
 * Details about roles and permissions. This file should be edited with any
 * global roles/permissions for the application.
 */
@Configuration
@EnableMethodSecurity
@AllArgsConstructor
public class SpringSecurityConfig {

    /** JWT authentication entry point for an authenticated user */
    private JwtAuthenticationEntryPoint authenticationEntryPoint;

    /** Filters for authentication */
    private JwtAuthenticationFilter     authenticationFilter;

    /**
     * Encodes passwords
     * 
     * @return object to encode passwords
     */
    @Bean
    public static PasswordEncoder passwordEncoder () {
        return new BCryptPasswordEncoder();
    }

    /**
     * Create global permission structures for roles.
     * 
     * @param http
     *            the security object
     * @return the SecurityFilterChain with permission information
     * @throws Exception
     *             if error
     */
    @Bean
    public SecurityFilterChain securityFilterChain ( HttpSecurity http ) throws Exception {
        http.csrf( ( csrf ) -> csrf.disable() ).cors( Customizer.withDefaults() )
                .authorizeHttpRequests( ( authorize ) -> {
                    authorize.requestMatchers( "/api/auth/**" ).permitAll();
                    authorize.requestMatchers( HttpMethod.OPTIONS, "/**" ).permitAll(); // allows
                                                                                        // preflight
                                                                                        // requests
                    authorize.anyRequest().authenticated();
                } ).httpBasic( Customizer.withDefaults() );

        http.exceptionHandling( exception -> exception.authenticationEntryPoint( authenticationEntryPoint ) );

        http.addFilterBefore( authenticationFilter, UsernamePasswordAuthenticationFilter.class );

        return http.build();
    }

    /**
     * Returns the AuthenticationManager for the project.
     * 
     * @param configuration
     *            configuration information for authentication
     * @return AuthenticationManager
     * @throws Exception
     *             if error
     */
    @Bean
    public AuthenticationManager authenticationManager ( AuthenticationConfiguration configuration ) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource () {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin( "*" );
        configuration.addAllowedMethod( "*" );
        configuration.addAllowedHeader( "*" );
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration( "/**", configuration );
        return source;
    }
}
