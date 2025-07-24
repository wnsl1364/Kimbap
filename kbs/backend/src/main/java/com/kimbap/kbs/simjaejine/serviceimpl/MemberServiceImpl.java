package com.kimbap.kbs.simjaejine.serviceimpl;

import org.springframework.stereotype.Service;

import com.kimbap.kbs.simjaejine.mapper.MemberMapper;
import com.kimbap.kbs.simjaejine.service.EmployeeAddVO;
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

    @Override
    public MemberAddVO addMember(MemberAddVO memberAddVO) {
      throw new UnsupportedOperationException("Unimplemented method 'addMember'");
    }

    @Override
    public EmployeeAddVO addEmployee(EmployeeAddVO employeeAddVO) {
      throw new UnsupportedOperationException("Unimplemented method 'addEmployee'");
    }
}