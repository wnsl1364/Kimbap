package com.kimbap.kbs.security.util;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Component;

import com.kimbap.kbs.simjaejine.service.MemberVO;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private final Key key = Keys.hmacShaKeyFor("KimbapJwtSecretKey1234567890KimbapJwt!".getBytes(StandardCharsets.UTF_8));
    private final long EXPIRATION = 1000 * 60 * 60; // 1시간

    public String generateToken(MemberVO user) {
        System.out.println("🔍 JWT 토큰 생성 시작 - 사용자: " + user.getId() + ", cpCd: " + user.getCpCd());
        
        String token = Jwts.builder()
                .setSubject(user.getId())
                .claim("authorities", user.getAuthorities()) // 토큰에서 권한 꺼내기
                .claim("cpCd", user.getCpCd())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        
        System.out.println("✅ JWT 토큰 생성 완료, 길이: " + token.length());
        
        // 생성된 토큰 검증
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token).getBody();
            System.out.println("🔍 생성된 토큰 검증 - subject: " + claims.getSubject());
            System.out.println("🔍 생성된 토큰 검증 - cpCd: " + claims.get("cpCd", String.class));
        } catch (Exception e) {
            System.out.println("❌ 생성된 토큰 검증 실패: " + e.getMessage());
        }
        
        return token;
    }

    public String getUsernameFromToken(String token) {
        try {
            System.out.println("🔍 getUsernameFromToken 시작");
            String username = Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token).getBody().getSubject();
            System.out.println("🔍 getUsernameFromToken 성공: " + username);
            return username;
        } catch (JwtException e) {
            System.out.println("❌ getUsernameFromToken 실패: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public String getCpCdFromToken(String token) {
        try {
            System.out.println("🔍 getCpCdFromToken 시작");
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token).getBody();
            
            System.out.println("🔍 JWT 클레임 파싱 성공");
            System.out.println("🔍 모든 클레임: " + claims);
            
            String cpCd = claims.get("cpCd", String.class);
            System.out.println("🔍 추출된 cpCd: " + cpCd);
            
            return cpCd;
        } catch (JwtException e) {
            System.out.println("❌ JWT에서 cpCd 추출 실패: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            System.out.println("🔍 validateToken 시작");
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            System.out.println("✅ validateToken 성공");
            return true;
        } catch (JwtException e) {
            System.out.println("❌ validateToken 실패: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}