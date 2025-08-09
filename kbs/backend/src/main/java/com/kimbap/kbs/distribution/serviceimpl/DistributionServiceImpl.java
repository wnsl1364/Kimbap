package com.kimbap.kbs.distribution.serviceimpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kimbap.kbs.distribution.mapper.DistributionMapper;
import com.kimbap.kbs.distribution.service.DistributionService;
import com.kimbap.kbs.distribution.service.DistributionVO;
import com.kimbap.kbs.distribution.service.LotStockVO;
import com.kimbap.kbs.distribution.service.RelDetailVO;
import com.kimbap.kbs.distribution.service.RelOrdModalVO;
import com.kimbap.kbs.distribution.service.RelOrderAndResultVO;
import com.kimbap.kbs.distribution.service.ReleaseMasterOrdVO;
import com.kimbap.kbs.distribution.service.ReleaseOrdVO;
import com.kimbap.kbs.distribution.service.ReleaseRequestVO;
import com.kimbap.kbs.distribution.service.WarehouseVO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
    public List<RelOrderAndResultVO> getRelOrdListWaiting() {
        return distributionMapper.getRelOrdListWaiting();
    }

    @Override
    public List<RelDetailVO> getRelDetails(String relMasCd) {
        return distributionMapper.getRelDetails(relMasCd);
    }

    @Override
    public List<LotStockVO> getLotsByPcode(String pcode) {
        return distributionMapper.getLotsByPcode(pcode);
    }

    @Transactional
    @Override
    public String insertRelease(ReleaseRequestVO vo) {
        java.math.BigDecimal requestTotal = java.math.BigDecimal.ZERO;

        for (var item : vo.getItems()) {
            // ✅ ord_d_cd 로만 단가 조회
            java.math.BigDecimal unitPrice =
                distributionMapper.selectUnitPriceByOrdDCd(item.getOrd_d_cd());
            if (unitPrice == null) unitPrice = java.math.BigDecimal.ZERO;

            String prodVerCd = distributionMapper.selectLatestProdVerCd(item.getPcode());

            for (var lot : item.getLots()) {
                Integer curr = distributionMapper.selectLotQtyForUpdate(lot.getLotNo(), lot.getWareAreaCd());
                if (curr == null || curr < lot.getAllocQty()) throw new IllegalStateException("LOT " + lot.getLotNo() + " 재고부족");

                String prodRelCd = distributionMapper.nextProdRelCd();
                distributionMapper.insertProdRel(
                    prodRelCd,
                    lot.getLotNo(),
                    lot.getAllocQty(),
                    item.getRelOrdCd(),   // 저장용으로 계속 전달
                    item.getOrd_d_cd(),   // XML에서 단가/총액 계산 fallback에 사용
                    item.getPcode(),
                    prodVerCd
                );

                distributionMapper.decreaseLotQty(lot.getLotNo(), lot.getWareAreaCd(), lot.getAllocQty());

                // 누적합 계산
                requestTotal = requestTotal.add(unitPrice.multiply(java.math.BigDecimal.valueOf(lot.getAllocQty())));
            }
        }

        int totalOrd = distributionMapper.selectTotalRelOrdQty(vo.getRelMasCd());
        int totalDone = distributionMapper.selectTotalReleasedQty(vo.getRelMasCd());
        String status = (totalDone >= totalOrd) ? "m3" : "m2";
        distributionMapper.updateRelOrderStatus(vo.getRelMasCd(), status);

        if (requestTotal.signum() > 0) {
            int updated = distributionMapper.increaseCompanyUnsettledAmount(vo.getCpCd(), requestTotal);
            if (updated == 0) {
                log.warn("[UNSETTLED NOT UPDATED] cpCd={}, amount={}", vo.getCpCd(), requestTotal);
            }
        }

        return "OK";
    }
}
