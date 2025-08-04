package com.kimbap.kbs.distribution.serviceimpl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kimbap.kbs.distribution.mapper.DistributionMapper;
import com.kimbap.kbs.distribution.service.DistributionService;
import com.kimbap.kbs.distribution.service.DistributionVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DistributionServiceImpl implements DistributionService{

    private final DistributionMapper distributionMapper;

    @Override
    public List<DistributionVO> getInOutCheck() {
        return distributionMapper.getInOutCheck(); // Mapper 호출
    }
}
