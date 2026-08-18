package org.proyecto.logistica.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain SecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/login", "/error").permitAll()
                        // API REST - públicamente accesible (sin autenticación por ahora)
                        .requestMatchers("/api/v1/**").permitAll()
                        // WebSocket - públicamente accesible
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/ws/location/**").permitAll()
                        // Endpoints web - requieren rol
                        .requestMatchers("/chofer/**", "/api/rutas/**").hasRole("CHOFER")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/despachador/**").hasRole("DESPACHADOR")
                        .requestMatchers("/mapa").hasAnyRole("ADMIN", "DESPACHADOR", "CHOFER")
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}
