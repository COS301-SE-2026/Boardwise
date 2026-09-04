package com.boardwise.backend.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.boardwise.backend.shared.security.JwtFilter;
import com.boardwise.backend.user_service.services.MyUserDetailsService;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;


@Configuration
@EnableWebSecurity
// @Profile("!test")
public class SecurityConfig {

    private final MyUserDetailsService userDetailsService; 

    private final JwtFilter jwtFilter;

    SecurityConfig(MyUserDetailsService userDetailsService, JwtFilter jwtFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http){
        return http
                .cors(Customizer.withDefaults()) // Enables CORS integration
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(request ->
                request 
                    .requestMatchers(HttpMethod.OPTIONS,"/**").permitAll()
                    .requestMatchers(
                        "/api/auth/hello",
                        "/api/auth/register",
                        "/api/auth/login",
                        "/api/auth/resetPassword",
                        "/api/auth/forgotPassword",
                        "/api/boardgames/",
                        "/api/vault/rulebooks",
                         "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html"
                        
                    ).permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/vault/rulebooks/*/text").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/vault/rulebooks/*", "/api/vault/rulebooks/*").permitAll()
                    .requestMatchers("/api/stomp","/api/stomp/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/boardgames/genres").permitAll()
                    .anyRequest()
                    .authenticated()
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"error\": \"Unauthorized: Missing or invalid token\"}");
                    })
                )
                .sessionManagement(sess -> 
                    sess.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS
                    )
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(new BCryptPasswordEncoder(12));
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config){
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration configuration = new CorsConfiguration();

        // Specify frontend origin
        configuration.setAllowedOrigins(List.of(
            "http://localhost:3000",
            "http://127.0.0.1:3000",
            "https://www.boardwise.games",
            "https://boardwise.games"
        ));

        // Allow OPTIONS for preflight requests
        configuration.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));

        // Allow headers including bearer token
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept","Access-Control-Request-Headers", "Access-Control-Request-Method"));
        configuration.setExposedHeaders(List.of("Authorization"));

        // To use cookies or auth headers across domains
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // Apply configuration to all API endpoints
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }
}
