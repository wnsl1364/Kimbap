package com.kimbap.kbs.distribution.serviceimpl;

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
    public void saveReleaseOrder(ReleaseMasterOrdVO master, List<ReleaseOrdVO> detailList) {
        // 1) 마스터 코드
        String relMasCd = distributionMapper.selectNewRelMasCd();
        master.setRelMasCd(relMasCd);

        // 2) 마스터 INSERT
        distributionMapper.insertReleaseOrdMaster(master);

        // 3) 시퀀스 준비
        int maxSeq = distributionMapper.selectMaxRelOrdSeqToday();
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int nextSeq = maxSeq + 1;

        // 4) 디테일 코드/마스터 세팅
        for (ReleaseOrdVO item : detailList) {
            String relOrdCd = "REL-" + today + "-" + String.format("%04d", nextSeq++);
            item.setNewRelOrdCd(relOrdCd);
            item.setRelMasCd(relMasCd);
        }

        // 5) 디테일 INSERT
        distributionMapper.insertReleaseOrdList(detailList);

        // 6) 주문 상태 업데이트 (고객용 상태 s7)
        if (master.getOrdCd() == null || master.getOrdCd().isBlank()) {
            throw new IllegalArgumentException("ordCd가 없어 주문 상태를 갱신할 수 없습니다.");
        }
        distributionMapper.updateOrdStatusCustomer(master.getOrdCd());
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
        java.util.Set<String> touchedOrdCds = new java.util.HashSet<>();

        for (var item : vo.getItems()) {
            java.math.BigDecimal unitPrice = distributionMapper.selectUnitPriceByOrdDCd(item.getRelOrdCd());
            if (unitPrice == null)
                unitPrice = distributionMapper.selectUnitPriceByOrdDCd(item.getOrd_d_cd());
            if (unitPrice == null)
                unitPrice = java.math.BigDecimal.ZERO;

            String prodVerCd = distributionMapper.selectLatestProdVerCd(item.getPcode());

            for (var lot : item.getLots()) {
    final String lotNo = lot.getLotNo();
    final String area  = lot.getWareAreaCd();
    int need = lot.getAllocQty();

    // 1) 관련 재고행 전부 잠금(ROWID, QTY) — FIFO/LIFO는 XML의 ORDER BY로 제어
    java.util.List<java.util.Map<String, Object>> rows =
        distributionMapper.selectLotQtyRowsForUpdate(lotNo, area);
    if (rows == null || rows.isEmpty()) {
        throw new IllegalStateException("LOT " + lotNo + " / " + area + " 재고가 없습니다.");
    }

    // 2) 합계 수량 검증
    int totalQty = 0;
    for (var r : rows) {
        Number q = (Number)(r.get("QTY") != null ? r.get("QTY") : r.get("qty")); // 컬럼명 대/소문자 모두 대비
        totalQty += (q == null ? 0 : q.intValue());
    }
    if (totalQty < need) {
        throw new IllegalStateException("LOT " + lotNo + " / " + area + " 재고부족(필요 " + need + ", 보유 " + totalQty + ")");
    }

    // 3) prod_rel 한 행 INSERT (이번 lot에 대한 출고수량 전체)
    String prodRelCd = distributionMapper.nextProdRelCd();
    distributionMapper.insertProdRel(
            prodRelCd,
            lotNo,
            need,
            item.getRelOrdCd(),
            item.getOrd_d_cd(),
            item.getPcode(),
            prodVerCd);

    // 4) ROWID 기준 분배 차감
    int remain = need;
    for (var r : rows) {
        if (remain <= 0) break;

        String rid = String.valueOf(r.get("RID") != null ? r.get("RID") : r.get("rid"));
        Number qn = (Number)(r.get("QTY") != null ? r.get("QTY") : r.get("qty"));
        int rowQty = (qn == null ? 0 : qn.intValue());
        if (rowQty <= 0) continue;

        int dec = Math.min(rowQty, remain);
        int updated = distributionMapper.decreaseLotQtyByRowId(rid, dec);
        if (updated != 1) {
            throw new IllegalStateException("재고 차감 실패(동시성 가능). RID=" + rid);
        }
        remain -= dec;
    }
    if (remain != 0) {
        throw new IllegalStateException("분배 차감 잔여가 0이 아닙니다. remain=" + remain);
    }

    // 5) 금액 누적
    requestTotal = requestTotal.add(
            unitPrice.multiply(java.math.BigDecimal.valueOf(need)));
}

            // ✅ 이 라인(주문상세)이 전량 출고됐는지 확인 후, 전량이면 t3로 상태 변경
            int ordered = distributionMapper.selectOrderQtyByOrdDCd(item.getOrd_d_cd()); // 주문수량
            int released = distributionMapper.sumReleasedQtyByOrdDCd(item.getOrd_d_cd()); // 누적 실제 출고
            if (released >= ordered) {
                distributionMapper.updateOrderDetailStatusToT3(item.getOrd_d_cd());
            }

            // 이 아이템이 속한 주문코드 수집
            String ordCd = distributionMapper.selectOrdCdByOrdDCd(item.getOrd_d_cd());
            if (ordCd != null)
                touchedOrdCds.add(ordCd);
        }

        // 마스터 상태 갱신 (기존 그대로)
        // 주문 전체 요청 수량
        long totalRequestQty = distributionMapper.selectTotalRequestQtyByRelMasCd(vo.getRelMasCd());
        // 주문 전체에서 누적 출고된 수량
        long totalReleasedQtyAll = distributionMapper.selectTotalReleasedQtyAllByRelMasCd(vo.getRelMasCd());

        String relStatus;
        if (totalReleasedQtyAll == 0) {
            relStatus = "m1"; // 미출고
        } else if (totalReleasedQtyAll >= totalRequestQty) {
            relStatus = "m2"; // 전부 출고
        } else {
            relStatus = "m3"; // 부분 출고
        }
        distributionMapper.updateRelOrderStatus(vo.getRelMasCd(), relStatus);

        // 주문 고객상태 갱신: 남은 수량 있으면 s8, 없으면 s3
        for (String ordCd : touchedOrdCds) {
            int remainCnt = distributionMapper.countRemainingQtyByOrdCd(ordCd);
            String custStatus = (remainCnt > 0) ? "s8" : "s3";
            distributionMapper.updateCustomerOrderStatus(ordCd, custStatus);
        }

        // 미정산금액 증가
        if (requestTotal.signum() > 0) {
            int updated = distributionMapper.increaseCompanyUnsettledAmount(vo.getCpCd(), requestTotal);
            if (updated == 0) {
                log.warn("[UNSETTLED NOT UPDATED] cpCd={}, amount={}", vo.getCpCd(), requestTotal);
            }
        }

        return "OK";
    }

    public RelOrderAndResultVO getRelOrdDetail(String relMasCd) {
        return distributionMapper.getRelOrdDetail(relMasCd);
    }

    @Override
    public List<ReleaseOrdVO> getRelOrdProductList(String relMasCd) {
        return distributionMapper.getRelOrdProductList(relMasCd);
    }

}
