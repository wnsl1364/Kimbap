package com.kimbap.kbs.simjaejine.serviceimpl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kimbap.kbs.simjaejine.mapper.MemberMapper;
import com.kimbap.kbs.simjaejine.service.EmpCpCheckVO;
import com.kimbap.kbs.simjaejine.service.MemberAddVO;
import com.kimbap.kbs.simjaejine.service.MemberService;
import com.kimbap.kbs.simjaejine.service.MemberVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberMapper memberMapper;

    @Override
    public MemberVO getUserInfo(String id) {
        return memberMapper.getUserInfo(id);
    }

    @Transactional
    @Override
    public int addMember(MemberAddVO memberAddVO) {
        int result = 0;
        result = memberMapper.addMember(memberAddVO);
        return result;
    }

    @Override
    public List<EmpCpCheckVO> getEmpList() {
        return memberMapper.getEmpList();
    }

    @Override
    public List<EmpCpCheckVO> getCpList() {
        return memberMapper.getCpList();
    }
}
