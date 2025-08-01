package com.kimbap.kbs.security.util;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Component;

import com.kimbap.kbs.simjaejine.service.MemberVO;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private final Key key = Keys.hmacShaKeyFor("KimbapJwtSecretKey1234567890KimbapJwt!".getBytes(StandardCharsets.UTF_8));
    private final long EXPIRATION = 1000 * 60 * 60; // 1시간

    public String generateToken(MemberVO user) {
        return Jwts.builder()
                .setSubject(user.getId())
                .claim("authorities", user.getAuthorities()) // 토큰에서 권한 꺼내기
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
