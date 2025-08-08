package com.kimbap.kbs.distribution.serviceimpl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
        // 1. 출고마스터코드 생성
        System.out.println("DEBUG: master = " + master);
        System.out.println("DEBUG: master.getMName() = " + master.getMname());
        String relMasCd = distributionMapper.selectNewRelMasCd();
        master.setRelMasCd(relMasCd);

        // 2. 마스터 insert
        distributionMapper.insertReleaseOrdMaster(master);

        // 3. 출고지시서코드 생성 준비
        int maxSeq = distributionMapper.selectMaxRelOrdSeqToday(); // 오늘 최대 시퀀스
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int nextSeq = maxSeq + 1;

        // 4. detailList에 코드 할당
        for (ReleaseOrdVO item : detailList) {
            String relOrdCd = "REL-" + today + "-" + String.format("%04d", nextSeq++);
            item.setNewRelOrdCd(relOrdCd); // 출고지시서 코드
            item.setRelMasCd(relMasCd); // 출고마스터 코드
        }

        // 5. 출고지시서 리스트 insert
        distributionMapper.insertReleaseOrdList(detailList);
    }

    @Override
    public RelOrderAndResultVO getRelOrdDetail(String relMasCd) {
        return distributionMapper.getRelOrdDetail(relMasCd);
    }

    @Override
    public List<ReleaseOrdVO> getRelOrdProductList(String relMasCd) {
        return distributionMapper.getRelOrdProductList(relMasCd);
    }

}
