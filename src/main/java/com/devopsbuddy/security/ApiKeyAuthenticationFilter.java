package com.devopsbuddy.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {
    private static final String HEADER_NAME = "X-API-Key";
    private final byte[] configuredKey;

    public ApiKeyAuthenticationFilter(@Value("${devopsbuddy.api.key:}") String configuredKey) {
        if (configuredKey != null && !configuredKey.isBlank() && configuredKey.length() < 32) {
            throw new IllegalStateException("DEVOPSBUDDY_API_KEY must be at least 32 characters long");
        }
        this.configuredKey = configuredKey == null
                ? new byte[0]
                : configuredKey.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String suppliedKey = request.getHeader(HEADER_NAME);
        if (configuredKey.length > 0 && suppliedKey != null
                && MessageDigest.isEqual(configuredKey, suppliedKey.getBytes(StandardCharsets.UTF_8))) {
            var authentication = new UsernamePasswordAuthenticationToken(
                    "api-client",
                    null,
                    AuthorityUtils.createAuthorityList("ROLE_API")
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }
}
