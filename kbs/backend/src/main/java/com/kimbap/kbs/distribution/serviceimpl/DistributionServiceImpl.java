package com.kimbap.kbs.distribution.serviceimpl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kimbap.kbs.distribution.mapper.DistributionMapper;
import com.kimbap.kbs.distribution.service.DistributionService;
import com.kimbap.kbs.distribution.service.DistributionVO;
import com.kimbap.kbs.distribution.service.RelOrderAndResultVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DistributionServiceImpl implements DistributionService {

    private final DistributionMapper distributionMapper;

    // 입출고 조회
    @Override
    public List<DistributionVO> getInOutCheck(DistributionVO filter) {
        return distributionMapper.getInOutCheck(filter);
    };

    // 출고 지시서 조회
    @Override
    public List<RelOrderAndResultVO> getRelOrdList(RelOrderAndResultVO filter) {
        return distributionMapper.getRelOrdList(filter);
    }
}
