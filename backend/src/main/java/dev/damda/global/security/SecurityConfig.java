package dev.damda.global.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http.authorizeHttpRequests(auth -> auth.requestMatchers(
                        "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/error", "/v3/api-docs.yaml")
                .permitAll()
                .requestMatchers("/api/*/admin/**")
                .hasRole("ADMIN")
                .requestMatchers("/api/*/studio/**")
                .hasRole("CREATOR")
                .requestMatchers(HttpMethod.GET, "/api/v1/templates/**")
                .permitAll()
                .anyRequest()
                .authenticated());

        return http.build();
    }
}
