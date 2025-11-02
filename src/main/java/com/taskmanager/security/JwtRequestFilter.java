package com.taskmanager.security;

import com.taskmanager.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private Environment env;

    // 不需要 JWT 的公开路径
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/",
            "/health",
            "/error",
            "/auth/signup",
            "/auth/login",
            "/auth/test",
            "/h2-console"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getServletPath();
        String[] profiles = env.getActiveProfiles();
        String activeProfiles = (profiles.length > 0) ? String.join(",", profiles) : "default";

        logger.debug("🔍 JwtRequestFilter: path=" + path + ", activeProfiles=[" + activeProfiles + "]");

        // 检查是否是公开路径
        boolean isPublic = isPublicPath(path);
        logger.debug("🧩 Checking if path is public: " + path + " -> " + isPublic);

        if (isPublic) {
            chain.doFilter(request, response);
            return;
        }

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
                logger.info("✅ JWT token detected for user: " + username);

                // 打印调试信息
                logger.debug("🔑 Token valid. Expires at: " + jwtUtil.extractExpiration(jwt));
                logger.debug("🧱 Using secret prefix: " + maskSecret(jwtUtil.getSecret()));

            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                logger.error("❌ JWT expired at: " + e.getClaims().getExpiration());
            } catch (io.jsonwebtoken.SignatureException e) {
                logger.error("❌ JWT signature invalid: " + e.getMessage());
            } catch (Exception e) {
                logger.error("❌ JWT extraction failed: " + e.getMessage());
            }
        } else {
            logger.debug("⚠️ No Authorization header found or not Bearer token");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                logger.info("✅ User authenticated: " + username);
            } else {
                logger.warn("❌ Token validation failed for user: " + username);
            }
        }

        chain.doFilter(request, response);
    }

    // 检查是否是公开路径（严格匹配，避免误判）
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream()
                .anyMatch(publicPath ->
                        path.equals(publicPath) || path.startsWith(publicPath + "/"));
    }

    // Secret 仅显示前 8 个字符
    private String maskSecret(String secret) {
        if (secret == null || secret.isEmpty()) return "(null)";
        return secret.substring(0, Math.min(8, secret.length())) + "******";
    }
}
