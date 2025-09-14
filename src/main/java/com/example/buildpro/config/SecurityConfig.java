package com.example.buildpro.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for simplicity
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/login", "/index", "/homepage", "/register", "/registration-verification",
                                "/categories/view", "/categories", "/cart/add/**",
                                "/otp-verification", "/forgot-password", "/reset-password", "/products",
                                "/products/view", "/cart/view", "/cart", "/checkout/**", "/payment/**",
                                "/user/**", "/user/dashboard", "/user/orders", "/user/profile",
                                "/css/**", "/js/**", "/products/add/**", "/images/**",
                                "/admin/**", "/admin/dashboard", "/admin/products/**", "/admin/orders/**",
                                "/admin/users/**",
                                "/admin/logout", "/cart/checkout", "/error", "/favicon.ico")
                        .permitAll()
                        .anyRequest().permitAll()) // Temporarily allow all requests
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/admin/dashboard")); // Redirect to dashboard on access denied

        return http.build();
    }

    /*
     * @Bean
     * public SecurityFilterChain securityFilterChain(HttpSecurity http) throws
     * Exception {
     * http
     * .cors(cors -> cors.configurationSource(corsConfigurationSource()))
     * .csrf(csrf -> csrf.disable())
     * .authorizeHttpRequests(auth -> auth
     * .requestMatchers("/api/auth/**").permitAll()
     * .requestMatchers("/api/products/**").permitAll()
     * .requestMatchers("/api/categories/**").permitAll()
     * .requestMatchers("/api/admin/**").hasRole("ADMIN")
     * .anyRequest().authenticated()
     * )
     * .formLogin(form -> form.disable())
     * .httpBasic(httpBasic -> httpBasic.disable());
     * 
     * return http.build();
     * }
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://127.0.0.1:5500"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
