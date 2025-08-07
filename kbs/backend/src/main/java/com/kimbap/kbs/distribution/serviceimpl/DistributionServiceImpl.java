package com.kimbap.kbs.distribution.serviceimpl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kimbap.kbs.distribution.mapper.DistributionMapper;
import com.kimbap.kbs.distribution.service.DistributionService;
import com.kimbap.kbs.distribution.service.DistributionVO;
import com.kimbap.kbs.distribution.service.RelOrdModalVO;
import com.kimbap.kbs.distribution.service.RelOrderAndResultVO;
import com.kimbap.kbs.distribution.service.ReleaseMasterOrdVO;
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
    @Transactional
    @Override
    public void saveReleaseOrder(ReleaseMasterOrdVO master, List<ReleaseOrdVO> detailList) {
    // 1. 출고마스터코드 생성 (쿼리 호출)
    String relMasCd = distributionMapper.selectNewRelMasCd();

    // 2. 생성한 relMasCd 를 master VO 에 세팅
    master.setRelMasCd(relMasCd);

    // 3. 마스터 insert
    distributionMapper.insertReleaseOrdMaster(master);

    // 4. detailList에 모두 relMasCd 세팅
    for (ReleaseOrdVO item : detailList) {
        item.setRelMasCd(relMasCd);
    }

    // 5. 출고지시서 리스트 insert
    distributionMapper.insertReleaseOrdList(detailList);
}
}
