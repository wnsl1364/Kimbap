package com.kimbap.kbs.security.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.kimbap.kbs.security.filter.JwtAuthenticationFilter;
import com.kimbap.kbs.security.util.JwtUtil;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    // 1. 비밀번호 인코더
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. 인증 관리자
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // 3. JWT 필터 등록 및 시큐리티 체인 설정
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
        .cors(Customizer.withDefaults())
        .csrf(csrf -> csrf.disable())
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            // 조회 허용 목록 (GET만)
            .requestMatchers(
                HttpMethod.GET,
                // 거래처
                "/api/std/cp/list",
                "/api/std/cp/sup/list",
                "/api/std/cp/sal/list",
                "/api/std/cp/detail/**",
                "/api/std/cp/change-history/**",

                // 설비
                "/api/std/fac/list",
                "/api/std/fac/detail/**",
                "/api/std/fac/change-history/**",

                // 자재
                "/api/std/mat/list",
                "/api/std/mat/detail/**",
                "/api/std/mat/history/**",
                "/api/std/mat/change-history/**",

                // 제품
                "/api/std/prod/list",
                "/api/std/prod/detail/**",
                "/api/std/prod/change-history/**",

                // 창고
                "/api/std/wh/list",
                "/api/std/wh/detail/**",
                "/api/std/wh/change-history/**"
            ).permitAll()

            // 그 외 /api/std/**는 ADMIN만
            .requestMatchers("/api/std/**").hasRole("ADMIN")

            // 기존 규칙 유지
            .requestMatchers("/api/memberAdd").hasRole("ADMIN")

            // 나머지는 전부 허용 (정책에 따라 authenticated()로 변경 가능)
            .anyRequest().permitAll()
        )
        .logout(logout -> logout
            .logoutUrl("/api/logout")
            .logoutSuccessHandler((request, response, authn) -> {
                response.setStatus(HttpServletResponse.SC_OK);
            })
        )
        .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, userDetailsService),
            UsernamePasswordAuthenticationFilter.class)
        .build();
}


    // 4. CORS 설정 (Vue 등 프론트 허용)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173")); // Vue Dev Server 주소
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
