package com.kimbap.kbs.order.serviceimpl;

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

        // 주문 마스터 상태 체크 및 업데이트 (부분반품(s5) / 반품완료(s6))
        int totalDetails = returnMapper.getOrderDetailCount(ordCd);
        int returnCompletedCount = returnMapper.getOrderDetailStatusCount(ordCd, "t5");

        Map<String, String> params = new HashMap<>();
        params.put("ordCd", ordCd);

        if (returnCompletedCount == totalDetails) {
            // 전체 반품완료
            params.put("status", "s6");
            returnMapper.updateOrderStatusCustomer(params);
            log.info("주문 마스터 상태 변경 → s6 (반품완료)");
        } else {
            // 부분반품
            params.put("status", "s5");
            returnMapper.updateOrderStatusCustomer(params);
            log.info("주문 마스터 상태 변경 → s5 (부분반품)");
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
    public void approveReturn(List<String> prodReturnCds) {
        for (String prodReturnCd : prodReturnCds) {
            Map<String, Object> params = new HashMap<>();
            params.put("returnStatusInternal", "w1");  // SQL에서 쓰는 이름과 맞춰야 함
            params.put("prodReturnCd", prodReturnCd);

            returnMapper.updateReturnStatus(params);
            returnMapper.updateOrderDetailStatusToT5(prodReturnCd); // 이건 그대로 두면 됨
        }
    }


    @Override
    @Transactional
    public void rejectReturn(List<String> prodReturnCds) {
        for (String prodReturnCd : prodReturnCds) {
            Map<String, Object> params = new HashMap<>();
            params.put("returnStatusInternal", "w2");  // 상태: 반려
            params.put("prodReturnCd", prodReturnCd);

            returnMapper.updateReturnStatus(params);
        }
    }
}
