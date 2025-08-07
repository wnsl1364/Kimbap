package com.kimbap.kbs.distribution.serviceimpl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kimbap.kbs.distribution.mapper.DistributionMapper;
import com.kimbap.kbs.distribution.service.DistributionService;
import com.kimbap.kbs.distribution.service.DistributionVO;
import com.kimbap.kbs.distribution.service.RelOrdModalVO;
import com.kimbap.kbs.distribution.service.RelOrderAndResultVO;
import com.kimbap.kbs.distribution.service.ReleaseOrdVO;
import com.kimbap.kbs.distribution.service.WarehouseVO;

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

    // 출고지시서 등록 모달관련
    @Override
    public List<RelOrdModalVO> getRelOrdModal(RelOrdModalVO vo) {
        return distributionMapper.getRelOrdModal(vo);
    }

    // 모달 선택시 상세출력
    @Override
    public List<RelOrdModalVO> getRelOrdSelect(String ordCd) {
        return distributionMapper.getRelOrdSelect(ordCd);
    }

    // 창고 목록 조회
        @Override
    public List<WarehouseVO> getWarehouseListByOrdCd(String ordCd) {
        return distributionMapper.getWarehouseListByOrdCd(ordCd);
    }

    // 출고 지시서 등록
    @Override
    public int insertReleaseOrders(List<ReleaseOrdVO> list) {
        return distributionMapper.insertReleaseOrders(list);
    }
}
