package com.kimbap.kbs.security.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import com.kimbap.kbs.security.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // ✅ 로그인 요청은 JWT 검사 없이 통과시킴
        String requestURI = request.getRequestURI();
        System.out.println("JWT 필터 동작 중: " + request.getRequestURI());
        if (requestURI.startsWith("/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            System.out.println("🔍 JWT 토큰 발견, 길이: " + token.length());
            
            String username = jwtUtil.getUsernameFromToken(token);
            System.out.println("🔍 JWT에서 추출한 사용자명: " + username);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                System.out.println("🔍 UserDetails 로드 완료: " + userDetails.getUsername());

                if (jwtUtil.validateToken(token)) {
                    System.out.println("🔍 JWT 토큰 검증 성공!");
                    
                    // JWT 토큰에서 cpCd 추출
                    String cpCd = jwtUtil.getCpCdFromToken(token);
                    System.out.println("🔍 JWT에서 추출한 cpCd: " + cpCd);

                    // 토큰 내용 직접 파싱해서 확인 (항상 실행)
                    try {
                        String[] parts = token.split("\\.");
                        if (parts.length >= 2) {
                            String payload = new String(java.util.Base64.getDecoder().decode(parts[1]));
                            System.out.println("🔍 JWT 페이로드 원본: " + payload);
                        }
                    } catch (Exception e) {
                        System.out.println("❌ JWT 페이로드 파싱 실패: " + e.getMessage());
                    }

                    // 요청 속성에 cpCd 저장
                    if (cpCd != null && !cpCd.isEmpty()) {
                        request.setAttribute("cpCd", cpCd);
                        System.out.println("✅ 요청 속성에 cpCd 저장 완료: " + cpCd);
                    } else {
                        System.out.println("❌ JWT에서 cpCd를 추출할 수 없습니다");
                    }

                    UsernamePasswordAuthenticationToken authToken
                            = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    // Authentication Details에도 cpCd 저장
                    if (cpCd != null && !cpCd.isEmpty()) {
                        Map<String, Object> details = new HashMap<>();
                        details.put("cpCd", cpCd);
                        authToken.setDetails(details);
                        System.out.println("✅ Authentication Details에 cpCd 저장 완료: " + cpCd);
                    }

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    System.out.println("❌ JWT 토큰 검증 실패!");
                }
            } else {
                if (username == null) {
                    System.out.println("❌ JWT에서 사용자명 추출 실패");
                }
                if (SecurityContextHolder.getContext().getAuthentication() != null) {
                    System.out.println("🔍 이미 인증된 사용자가 있음");
                }
            }
        } else {
            if (authHeader == null) {
                System.out.println("❌ Authorization 헤더가 없습니다");
            } else {
                System.out.println("❌ Bearer 토큰이 아닙니다: " + authHeader);
            }
        }

        filterChain.doFilter(request, response);
    }
}