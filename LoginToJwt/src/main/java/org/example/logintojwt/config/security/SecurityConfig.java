package org.example.logintojwt.config.security;

import lombok.RequiredArgsConstructor;
import org.apache.catalina.connector.Connector;
import org.example.logintojwt.exception.CustomAuthenticationEntryPoint;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@EnableMethodSecurity
@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtFilter jwtFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // stateless 이기에 csrf 보호 비활성해놨지만
        // httponly 쿠키를 사용하기에 csrf 설정을 해줘야함
        http.csrf(csrf -> csrf.disable());

        // cors 설정
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // 세션 관리 비활성화
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 예외 처리
        http.exceptionHandling(exception -> exception.authenticationEntryPoint(customAuthenticationEntryPoint));

        // https 리다이렉트 설정
        // !!!!!!!!! 테스트 떄는 꺼놓기
        //http.requiresChannel(channel -> channel.anyRequest().requiresSecure());

        //브라우저가 항상 https를 사용하도록
        /*http.headers(headers -> headers
                .httpStrictTransportSecurity(hsts -> hsts
                        .maxAgeInSeconds(31536000) // 1년
                        .includeSubDomains(true)
                )
        );*/

        // jwt 필터
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        // 유저 디테일 서비스 설정
        http.userDetailsService(customUserDetailsService);

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/users/**", "/api/auth/**", "api/page","api/categories/**", "api/products/**").permitAll()
                .anyRequest().authenticated());

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("https://localhost:3000"); // 하용할 클라이언트 도메인
        //corsConfiguration.addAllowedOriginPattern("http://localhost:3000"); //와일드카드 지원
        corsConfiguration.addAllowedMethod("*"); // HTTP 메서드
        corsConfiguration.addAllowedHeader("*"); // 헤더
        corsConfiguration.setAllowCredentials(true); // 자격증명 허용 httpOnly 쿠키를 받을수있다

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**",corsConfiguration); // 서버의 api/ 경로에 대해서 설정 적용
        return source;

    }

    // http를 https로 리다이렉트
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> servletContainer(){
        return server -> server.addAdditionalTomcatConnectors(createHttpConnector());
    }

    private Connector createHttpConnector(){
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setScheme("http");
        connector.setPort(8080);
        connector.setSecure(false);
        connector.setRedirectPort(8443);
        return connector;

    }
}
