package com.ppks.fernet.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.ALWAYS;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    // private final LogoutHandler logoutHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // ignore our stomp endpoints since they are protected using Stomp headers
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .headers(headers -> headers
                        // allow same origin to frame our site to support iframe SockJS
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                )
                .authorizeHttpRequests(req ->
                        req
                                .requestMatchers("/auth/**").permitAll()
//                                .requestMatchers(antMatcher("/admin/**")).hasAuthority(Role.ADMIN.toString())
//                                .requestMatchers(antMatcher("/ticket/**")).hasAnyAuthority(Role.ADMIN.toString(), Role.USER.toString())
//                                .requestMatchers(antMatcher("/message/**")).hasAnyAuthority(Role.ADMIN.toString(), Role.USER.toString())
                                .anyRequest()
                                .authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(ALWAYS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
//                .logout(logout ->
//                                logout.logoutUrl("/logout")
//                                        .addLogoutHandler(logoutHandler)
//                                        .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.getContext())
//                        //prebaci SecurityContextHolder.getContext() u SecurityContextHolder.clearContext()
//                )
        ;

        return http.build();
    }
}
