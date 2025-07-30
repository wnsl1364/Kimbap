package com.kimbap.kbs.simjaejine.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginSecurityVO {
    private String secuCd;     // 보안코드
    private int lcount;        // 로그인 실패 횟수
    private String idUsed;     // 잠금 유무
    private String memCd;      // 회원 코드
}