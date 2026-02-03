package com.gateway.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;

@Component
public class JwtValidationFilter implements WebFilter {

    @Value("${rsa.public-key}")
    private org.springframework.core.io.Resource publicKeyResource;

    private PublicKey publicKey;

    @Autowired
    public void init() throws Exception {
        this.publicKey = loadPublicKey();
    }

    private PublicKey loadPublicKey() throws Exception {
        String key = new String(publicKeyResource.getInputStream().readAllBytes())
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decodedKey = java.util.Base64.getDecoder().decode(key);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decodedKey);
        return keyFactory.generatePublic(spec);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        try {
            // Skip OPTIONS (preflight) requests
            if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
                return chain.filter(exchange);
            }

            String token = getTokenFromRequest(exchange);

            if (token != null && validateToken(token)) {
                String username = extractUsername(token);
                
                // Create Spring Security Authentication object
                List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("ROLE_USER");
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        username, 
                        token, 
                        authorities
                );
                
                // Set authentication in ReactiveSecurityContextHolder
                return chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
            }

            return chain.filter(exchange);

        } catch (Exception e) {
            // log.error("JWT filter error: {}", e.getMessage());
            return chain.filter(exchange);
        }
    }

    private String getTokenFromRequest(ServerWebExchange exchange) {
        String bearerToken = exchange.getRequest()
                .getHeaders()
                .getFirst("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (Exception e) {
            // log.warn("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }

    private String extractUsername(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getSubject();

        } catch (Exception e) {
            // log.error("Failed to extract username: {}", e.getMessage());
            return null;
        }
    }
}