package com.kimbap.kbs.security.service;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.kimbap.kbs.simjaejine.mapper.MemberMapper;
import com.kimbap.kbs.simjaejine.service.MemberVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberMapper memberMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        MemberVO user = memberMapper.getUserInfo(username);
        if (user == null) throw new UsernameNotFoundException("사용자 없음");

        // DB에서 권한 읽기
        List<String> roles = memberMapper.selectRolesByMemberId(username); // 예: ["ROLE_ADMIN", "ROLE_USER"]

        // DB 값이 "ADMIN"이라면 ROLE_ 접두사 붙이기
        List<SimpleGrantedAuthority> authorities = roles.stream()
            .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
            .map(SimpleGrantedAuthority::new)
            .toList();

        return User.builder()
            .username(user.getId())
            .password(user.getPw())
            .authorities(authorities) // ✅ 핵심
            .build();
    }
}

