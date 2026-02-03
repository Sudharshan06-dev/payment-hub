package com.userservice.services;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.security.PublicKey;

@Service
public class OauthService {

    @Value("${rsa.private-key}")
    private Resource privateKeyResource;

    private PrivateKey privateKey;

    @Autowired
    public void init() throws Exception {
        this.privateKey = loadPrivateKey();
    }

    private PrivateKey loadPrivateKey() throws Exception {
        String key = new String(privateKeyResource.getInputStream().readAllBytes())
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decodedKey = java.util.Base64.getDecoder().decode(key);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decodedKey);
        return keyFactory.generatePrivate(spec);
    }

    public PublicKey getPublicKey() throws Exception {
        java.security.KeyPair keyPair = java.security.KeyPairGenerator.getInstance("RSA").generateKeyPair();
        return keyPair.getPublic();
    }

    public String generateToken(String username, String email, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("email", email);
        claims.put("userId", userId);

        String subject = username + "|" + email + "|" + userId;

        return Jwts
                .builder()
                .setClaims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * (60 * 60 * 24)))
                .signWith(privateKey)
                .compact();
    }

    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        final Claims claims = extractAllClaims(token);
        return claimResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith((java.security.PublicKey) privateKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUserName(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
}
