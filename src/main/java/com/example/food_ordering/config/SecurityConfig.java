package com.example.food_ordering.config;

import lombok.RequiredArgsConstructor;
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
                        .requestMatchers("/", "/signin", "/signup", "/404", "/css/**", "/js/**", "/favicon.ico").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/food", "/api/food/**").permitAll()
                        .requestMatchers(HttpMethod.POST,   "/api/food/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.PATCH,  "/api/food/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/food/**").hasRole("MANAGER")
                        .requestMatchers(HttpMethod.GET, "/api/order/all").hasAnyRole("STAFF", "MANAGER")
                        .requestMatchers(HttpMethod.PATCH, "/api/order/*/advance").hasAnyRole("STAFF", "MANAGER")
                        .requestMatchers("/api/user/**").hasRole("MANAGER")
                        .requestMatchers("/api/notification/kitchen").hasAnyRole("STAFF", "MANAGER")
                        .requestMatchers("/customer/**", "/staff/**", "/manager/**").authenticated()
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