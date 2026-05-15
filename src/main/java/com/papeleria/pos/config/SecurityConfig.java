package com.papeleria.pos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Apagamos CSRF temporalmente para facilitar las pruebas locales
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // 1. RECURSOS PÚBLICOS
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/login").permitAll()

                        // 2. BLINDAJE DE PANTALLAS (FRONTEND) - Usamos hasRole
                        .requestMatchers("/catalogo/**", "/personal/**", "/reportes/**").hasRole("ADMIN")
                        .requestMatchers("/dashboard", "/punto-venta", "/corte-caja").hasAnyRole("ADMIN", "CAJERO")

                        // 3. BLINDAJE DE API (BACKEND) - Usamos hasRole
                        .requestMatchers(HttpMethod.POST, "/api/productos/**", "/api/servicios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/productos/**", "/api/servicios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/productos/**", "/api/servicios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/productos/**", "/api/servicios/**").hasRole("ADMIN")

                        // Cualquier otra petición a la API y al sistema requiere estar autenticado
                        .anyRequest().authenticated()
                )
                // Configuramos nuestro Login Visual
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login-post")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                // Configuramos el cierre de sesión
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                );

        return http.build();
    }
}