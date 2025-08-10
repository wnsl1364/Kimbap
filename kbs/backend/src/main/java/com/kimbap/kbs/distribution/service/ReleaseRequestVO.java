package com.kimbap.kbs.distribution.service;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // 예상치 못한 필드 들어와도 무시
public class ReleaseRequestVO {
    private String relMasCd; // 출고 지시서 마스터 코드
    private String cpCd;     // 거래처 코드
    private String memo;     // 비고/메모
    private List<ItemVO> items; // 출고 상세 목록

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ItemVO {
        private String relOrdCd;
        private String ord_d_cd;
        private String pcode;      // 제품 코드
        private Integer qty;       // 출고수량 합계 (lots 합계)
        private List<LotVO> lots;  // LOT별 배분 목록
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LotVO {
        private String lotNo;      // LOT 번호
        private String wareAreaCd; // 창고 구역 코드
        private Integer allocQty;  // 배분 수량
    }
}
