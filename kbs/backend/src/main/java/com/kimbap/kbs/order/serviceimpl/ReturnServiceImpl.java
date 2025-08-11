package com.kimbap.kbs.order.serviceimpl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kimbap.kbs.order.mapper.ReturnMapper;
import com.kimbap.kbs.order.service.ReturnItemVO;
import com.kimbap.kbs.order.service.ReturnRequestVO;
import com.kimbap.kbs.order.service.ReturnService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReturnServiceImpl implements ReturnService {

    private final ReturnMapper returnMapper;

    @Override
    @Transactional
    public void registerReturn(ReturnRequestVO request) {
        String ordCd = request.getOrdCd();

        for (ReturnItemVO item : request.getReturnItems()) {
            // 프론트에서 넘어온 lotNo를 이용해 유효성 체크
            Map<String, Object> lotParams = new HashMap<>();
            lotParams.put("ordDCd", item.getOrdDCd());
            lotParams.put("lotNo", item.getLotNo());

            String lot = returnMapper.checkLotExists(lotParams);

            if (lot == null) {
                throw new RuntimeException("선택한 LOT 번호가 유효하지 않습니다. (ord_d_cd: " + item.getOrdDCd() + ")");
            }

            // 시퀀스 번호 조회 (정수형)
            int seq = returnMapper.getNextReturnCodeSeq();
            // 현재 연도
            String year = new SimpleDateFormat("yyyy").format(new Date());
            // 반품 코드 생성
            String prodReturnCd = "RTN-" + year + String.format("%04d", seq);  // ex) RTN-20250001
            item.setProdReturnCd(prodReturnCd);

            // 데이터 세팅
            item.setReturnDt(new Date());

            // 제품반품 등록
            returnMapper.insertReturnItem(item);
            log.info("반품 등록 완료: {}");

            // 주문 상세 상태 → 반품요청(t4)
            returnMapper.updateOrderDetailStatus(item.getOrdDCd());
            log.info("주문상세 상태 변경 → t4 (ord_d_cd: {})", item.getOrdDCd());
        }

        Map<String, String> params = new HashMap<>();
        params.put("ordCd", ordCd);
        params.put("status", "v1");
        int updatedRows = returnMapper.updateOrderStatusCustomer(params);

        // 업데이트된 Row 수를 로그로 출력 (0이면 update 실패)
        log.info("주문마스터 상태 v1 업데이트 결과 → 업데이트된 건수: {}", updatedRows);

        if (updatedRows == 0) {
            log.error("주문마스터 상태 업데이트 실패! (ordCd: {})", ordCd);
        } else {
            log.info("주문 마스터 상태 변경 성공 → v1 (반품요청)");
        }
    }

    @Override
    public List<ReturnItemVO> getReturnHistoryByOrdCd(String ordCd) {
        return returnMapper.getReturnHistoryByOrdCd(ordCd);
    }

    @Override
    public List<ReturnItemVO> getReturnList(Map<String, Object> params) {
        return returnMapper.getReturnList(params);
    }

    @Override
    @Transactional
    public void approveReturn(ReturnItemVO request) {
        log.info("=== [반품 승인 요청 시작] ===");
        log.info("요청 데이터: prodReturnCd={}, ordDCd={}, returnQty={}, manager={}", 
                request.getProdReturnCd(), request.getOrdDCd(), request.getReturnQty(), request.getManager());

        // 1. 단가 DB 조회
        BigDecimal unitPrice = returnMapper.getUnitPriceByOrdDCd(request.getOrdDCd());
        log.info("DB 조회 단가: {}", unitPrice);

        // 2. 반품금액 계산
        BigDecimal returnAmount = unitPrice.multiply(new BigDecimal(request.getReturnQty()));
        log.info("계산된 반품 금액: {}", returnAmount);

        // 3. prod_return 테이블에 금액 업데이트
        Map<String, Object> amountParams = new HashMap<>();
        amountParams.put("prodReturnCd", request.getProdReturnCd());
        amountParams.put("returnAmount", returnAmount);
        returnMapper.updateReturnAmount(amountParams);
        log.info("prod_return 테이블 return_amount 업데이트 완료");

        // 4. 거래처 미수금 차감
        String ordCd = returnMapper.getOrdCdByReturnCd(request.getProdReturnCd());
        String cpCd = returnMapper.getCompanyCodeByOrdCd(ordCd);
        log.info("연결된 주문코드: {}, 거래처코드: {}", ordCd, cpCd);

        Map<String, Object> decreaseParams = new HashMap<>();
        decreaseParams.put("cpCd", cpCd);
        decreaseParams.put("amount", returnAmount);
        returnMapper.decreaseCompanyUnsettledAmount(decreaseParams);
        log.info("거래처({}) 미수금 {} 차감 완료", cpCd, returnAmount);

        // 5. 반품 상태 승인(w1) 처리
        Map<String, Object> statusParams = new HashMap<>();
        statusParams.put("returnStatusInternal", "w1");
        statusParams.put("prodReturnCd", request.getProdReturnCd());
        statusParams.put("manager", request.getManager());
        returnMapper.updateReturnStatus(statusParams);
        log.info("반품 상태 승인(w1) 처리 완료");

        // 6. 주문 상세 상태 t5(반품완료)로 변경
        returnMapper.updateOrderDetailStatusToT5(request.getProdReturnCd());
        log.info("주문상세 상태 변경 → t5(반품완료)");

        // 7. 주문 마스터 상태 갱신
        updateOrderMasterStatus(ordCd);
        log.info("주문마스터 상태 갱신 완료");

        log.info("=== [반품 승인 요청 종료] ===");
    }

    @Override
    @Transactional
    public void approveReturns(List<ReturnItemVO> requests) {
        for (ReturnItemVO request : requests) {
            approveReturn(request); // 기존 단건 승인 로직 재사용
        }
    }


    @Override
    @Transactional
    public void rejectReturn(ReturnItemVO request) {
        log.info("=== [반품 거절 요청 시작] ===");
        log.info("요청 데이터: prodReturnCd={}, manager={}, rejectRea={}", 
                request.getProdReturnCd(), request.getManager(), request.getRejectRea());

        // 1. 반품 상태 거절(w2) 처리
        Map<String, Object> params = new HashMap<>();
        params.put("returnStatusInternal", "w2");  // 거절
        params.put("prodReturnCd", request.getProdReturnCd());
        params.put("manager", request.getManager());
        params.put("rejectRea", request.getRejectRea());
        returnMapper.updateReturnStatus(params);
        log.info("반품 상태 거절(w2) 처리 완료");

        // 2. 주문 상세 상태 t1(주문접수)로 복구
        returnMapper.updateOrderDetailStatusToT1(request.getProdReturnCd());
        log.info("주문상세 상태 변경 → t1(주문접수)");

        // 3. 주문 마스터 상태 갱신
        String ordCd = returnMapper.getOrdCdByReturnCd(request.getProdReturnCd());
        updateOrderMasterStatus(ordCd);
        log.info("주문마스터 상태 갱신 완료");

        log.info("=== [반품 거절 요청 종료] ===");
    }


    @Transactional
    public void updateOrderMasterStatus(String ordCd) {
        int totalDetails = returnMapper.getOrderDetailCount(ordCd);
        int returnCompletedCount = returnMapper.getOrderDetailStatusCount(ordCd, "t5");
        int pendingReturnCount = returnMapper.getOrderDetailStatusCount(ordCd, "t4");

        log.info("=== 상태 점검 ===");
        log.info("ordCd: {}", ordCd);
        log.info("주문상세 총 건수: {}", totalDetails);
        log.info("반품완료(t5) 건수: {}", returnCompletedCount);
        log.info("반품요청(t4) 건수: {}", pendingReturnCount);

        Map<String, String> updateParams = new HashMap<>();
        updateParams.put("ordCd", ordCd);

        if (returnCompletedCount == totalDetails) {
            // 전체 반품 완료
            updateParams.put("status", "s6");  // 반품완료
            log.info("주문 마스터 상태 변경 → s6 (반품완료)");
        } else if (pendingReturnCount > 0) {
            // 남아있는 반품요청 있음
            updateParams.put("status", "v1");  // 반품요청 유지
            log.info("주문 마스터 상태 유지 → v1 (반품요청)");
        } else if (returnCompletedCount > 0) {
            // 일부 반품 완료
            updateParams.put("status", "s5");  // 부분반품
            log.info("주문 마스터 상태 변경 → s5 (부분반품)");
        } else {
            // 반품이 모두 거절된 경우
            updateParams.put("status", "s3");  // 출고완료 복귀
            log.info("모든 반품 거절 → 주문 마스터 상태 s3(출고완료)로 복구");
        }

        returnMapper.updateOrderStatusCustomer(updateParams);
    }

    @Override
    @Transactional
    public void cancelReturnItems(List<String> ordDCdList) {
        for (String ordDCd : ordDCdList) {
            // 1. 주문상세 상태를 출고완료(t3)로 변경
            returnMapper.updateOrderDetailStatusToT3(ordDCd);
            log.info("주문상세 상태 복구 → t3 (ord_d_cd: {})", ordDCd);

            // 2. prod_return 상태를 반품요청취소(v2)로 변경
            returnMapper.updateProdReturnStatusToV2(ordDCd);
            log.info("prod_return 상태 변경 → v2 (ord_d_cd: {})", ordDCd);
        }

        // 3. 주문 마스터 상태 갱신 (주문상세 기준)
        for (String ordDCd : ordDCdList) {
            String ordCd = returnMapper.getOrdCdByOrdDCd(ordDCd);
            updateOrderMasterStatus(ordCd);
        }
    }

    @Override
    public List<String> getLotList(String ordDCd) {
        return returnMapper.getLotNosByOrdDCd(ordDCd);
    }
}
