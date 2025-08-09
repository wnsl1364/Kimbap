package com.kimbap.kbs.distribution.service;

import java.sql.Timestamp;
import lombok.Data;

@Data
public class LotStockVO {
    private String lotNo;        // LOT 번호 (prod_inbo.lot_no)
    private String pcode;        // 제품코드
    private Integer qty;         // 현재 재고수량 (ware_stock.qty)
    private String wcode;        // 창고코드 (ware_stock.wcode)
    private String wareAreaCd;   // 창고 구역코드 (ware_stock.ware_area_cd)
    private Timestamp inboDt;    // 입고일시 (prod_inbo.inbo_dt) - FIFO 정렬용
}