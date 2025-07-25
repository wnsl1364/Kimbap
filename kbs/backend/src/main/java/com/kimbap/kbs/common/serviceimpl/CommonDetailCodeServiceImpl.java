package com.kimbap.kbs.common.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kimbap.kbs.common.mapper.CommonDetailCodeMapper;
import com.kimbap.kbs.common.service.CommonDetailCodeService;
import com.kimbap.kbs.common.service.CommonDetailCodeVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommonDetailCodeServiceImpl implements CommonDetailCodeService {
  
  @Autowired
  private final CommonDetailCodeMapper commonDetailCodeMapper;

    @Override
    public List<CommonDetailCodeVO> getDetailCodes(String groupCd) {
        return commonDetailCodeMapper.selectDetailCodesByGroup(groupCd);
    }
}
