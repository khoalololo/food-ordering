package com.example.food_ordering.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher .AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF since we use stateless JWT, not sessions
                .csrf(AbstractHttpConfigurer::disable)
                // Allow our frontend origin
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Stateless — no HTTP sessions
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers(
                                new AntPathRequestMatcher("/"),
                                new AntPathRequestMatcher("/signin"),
                                new AntPathRequestMatcher("/signup"),
                                new AntPathRequestMatcher("/404"),
                                new AntPathRequestMatcher("/error"),
                                new AntPathRequestMatcher("/favicon.ico"),
                                new AntPathRequestMatcher("/css/**"),
                                new AntPathRequestMatcher("/js/**")
                        ).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/auth/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/food", "GET")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/food/**", "GET")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/food/**", "POST")).hasRole("MANAGER")
                        .requestMatchers(new AntPathRequestMatcher("/api/food/**", "PATCH")).hasRole("MANAGER")
                        .requestMatchers(new AntPathRequestMatcher("/api/food/**", "DELETE")).hasRole("MANAGER")
                        .requestMatchers(new AntPathRequestMatcher("/api/order/all", "GET")).hasAnyRole("STAFF", "MANAGER")
                        .requestMatchers(new AntPathRequestMatcher("/api/order/*/advance", "PATCH")).hasAnyRole("STAFF", "MANAGER")
                        .requestMatchers(new AntPathRequestMatcher("/api/user/**")).hasRole("MANAGER")
                        .requestMatchers(new AntPathRequestMatcher("/api/notification/kitchen")).hasAnyRole("STAFF", "MANAGER")
                        .requestMatchers(
                                new AntPathRequestMatcher("/customer/**"),
                                new AntPathRequestMatcher("/staff/**"),
                                new AntPathRequestMatcher("/manager/**")
                        ).authenticated()
                        .anyRequest().authenticated()
                )

                // Plug in our JWT filter before Spring's default username/password filter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // In production replace * with your actual frontend origin
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}