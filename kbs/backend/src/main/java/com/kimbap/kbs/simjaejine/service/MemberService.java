package com.kimbap.kbs.simjaejine.service;

import org.springframework.stereotype.Service;

import com.kimbap.kbs.simjaejine.mapper.MemberMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {
      private final MemberMapper memberMapper;

    public MemberVO getUserInfo(String id) {
        return memberMapper.getUserInfo(id); // memberMapper.getUserInfo(id) 메서드 호출하여 DB에서 사용자 정보 조회
    }
}
