package com.example.acacia.SecurityConfig;

import com.example.acacia.model.Member;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtUtils {

    private final Key secretKey = Keys.hmacShaKeyFor("this-is-my-secret-key-and-needs-to-be-pretty-long".getBytes(StandardCharsets.UTF_8));

    public String createAccessToken(Member member) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", member.getRole());
        claims.put("memberId", member.getId());
        claims.put("name", member.getFullName());

        long expiration = 1000 * 60 * 15;

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(member.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }
    public String createRefreshToken(Member member) {
        long expiration = 1000 * 60 * 60;
        return Jwts.builder()
                .setSubject(member.getEmail())
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        return (String) claims.get("role");
    }

    public String extractName(String token) {
        Claims claims = extractAllClaims(token);
        return (String) claims.get("name");
    }

    public Long extractMemberId(String token) {
        Claims claims = extractAllClaims(token);
        Number memberId = claims.get("memberId", Number.class);
        return memberId.longValue();
    }

    public Long extractExpiration(String token) {
        Date expirationDate = extractClaim(token, Claims::getExpiration);
        return expirationDate != null ? expirationDate.getTime() : null;
    }

    private boolean isTokenExpired(String token) {
        Long expirationTime = extractExpiration(token);
        return expirationTime != null && expirationTime < System.currentTimeMillis();
    }

    //validate the jwtToken data with the current user
    public boolean isTokenValid(String token, UserDetails user) {
        final String username = extractUsername(token);
        return (username.equals(user.getUsername()) && !isTokenExpired(token));
    }

    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();

            String type = claims.get("type", String.class);
            if (!"refresh".equals(type)) {
                return false;
            }

            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

}
