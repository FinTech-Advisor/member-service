package org.advisor.global.configs;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.advisor.member.jwt.JwtProperties;
import org.advisor.member.jwt.filters.LoginFilter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
@RequiredArgsConstructor
public class SecurityConfig {

    private final LoginFilter loginFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(c -> c.disable())
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용 안 함
                .cors()  // Spring Security 기본 CORS 활성화
                .and()
                .addFilterBefore(loginFilter, UsernamePasswordAuthenticationFilter.class) // 로그인 필터 추가
                .exceptionHandling(c -> {
                    c.authenticationEntryPoint((req, res, e) -> {
                        res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    });
                    c.accessDeniedHandler((req, res, e) -> {
                        res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    });
                })
                .authorizeHttpRequests(c -> {
                    c.requestMatchers(
                                    "/join",
                                    "/login",
                                    "/apidocs.html",
                                    "/swagger-ui*/**",
                                    "/api-docs/**").permitAll()
                            .requestMatchers("/admin/**").hasRole("ADMIN")
                            .anyRequest().authenticated();
                });

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // 패스워드 암호화
    }
}
