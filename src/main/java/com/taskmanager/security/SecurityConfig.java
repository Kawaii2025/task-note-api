package com.taskmanager.security;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taskmanager.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * ✅ SecurityConfig
 * 统一管理开发与生产安全策略：
 * - dev 模式：放宽所有权限，方便调试
 * - prod 模式：启用 JWT 验证
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Autowired
    private Environment env;  // 当前激活的 Profile

    // ✅ 从 application.yml 中读取 jwt.secret
    @Value("${jwt.secret:}")
    private String jwtSecret;

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @PostConstruct
    public void logEnvironmentInfo() {
        String[] profiles = env.getActiveProfiles();
        log.info("🌱 Active profiles: {}", String.join(", ", profiles));
        if (jwtSecret != null && !jwtSecret.isEmpty()) {
            log.info("🔑 JWT Secret loaded (first 8 chars): {}******", jwtSecret.substring(0, Math.min(8, jwtSecret.length())));
        } else {
            log.warn("⚠️ JWT Secret is missing or empty!");
        }
    }

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private String allowedOrigins;

    // 加密器
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 用户认证提供者
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // AuthenticationManager（用于登录认证）
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * 🔐 Security Filter Chain
     * 根据当前 profile 自动切换开发/生产安全模式。
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // 检查当前环境是否为开发模式
        boolean isDev = Arrays.asList(env.getActiveProfiles()).contains("dev");

        http
                .csrf(csrf -> csrf.disable()) // 关闭 CSRF（JWT 无状态）
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin())); // 支持 H2 控制台

        if (isDev) {
            // 🧩 开发环境：放宽权限（不需要 token）
            http.authorizeHttpRequests(auth -> auth
                    .requestMatchers("/", "/error", "/health", "/h2-console/**").permitAll()
                    .requestMatchers("/auth/**").permitAll()
                    .requestMatchers("/tasks/**").permitAll()
                    .anyRequest().permitAll()
            );
            System.out.println("⚙️ [SECURITY] Running in DEV mode → All endpoints are open.");
        } else {
            // 🔒 生产环境：严格启用 JWT 验证
            http.authorizeHttpRequests(auth -> auth
                    .requestMatchers("/", "/error", "/health", "/auth/**", "/h2-console/**").permitAll()
                    .anyRequest().authenticated()
            );
            http.authenticationProvider(authenticationProvider());
            http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
            System.out.println("🔐 [SECURITY] Running in PROD mode → JWT security enabled.");
        }

        return http.build();
    }

    /**
     * 🌐 CORS 配置：支持多域名（可在 application.yml 或环境变量中定义）
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 从配置读取多个域名
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
