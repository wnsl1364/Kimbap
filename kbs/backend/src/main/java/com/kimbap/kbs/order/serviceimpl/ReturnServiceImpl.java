package com.kimbap.kbs.order.serviceimpl;

import java.text.SimpleDateFormat;
import java.util.Date;

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
            // 1. 반품코드 시퀀스로 생성
            String seq = returnMapper.getNextReturnCodeSeq(); // 예: 000001
            String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String prodReturnCd = "RTN-" + dateStr + "-" + seq;

            // 2. 데이터 세팅
            item.setProdReturnCd(prodReturnCd);
            item.setOrdCd(ordCd);
            item.setReturnDt(new Date());

            // 3. insert into prod_return
            returnMapper.insertReturnItem(item);
            log.info("반품 등록 완료: {}", prodReturnCd);

            // 4. 주문 상세 상태 → 반품요청(t4)
            returnMapper.updateOrderDetailStatus(item.getOrdDCd());
            log.info("주문상세 상태 변경 → t4 (ord_d_cd: {})", item.getOrdDCd());
        }

        // 5. 주문 마스터 상태 → 반품요청(s4)
        returnMapper.updateOrderStatusCustomer(ordCd);
        log.info("주문 마스터 상태 변경 → s4 (ord_cd: {})", ordCd);
    }
}
