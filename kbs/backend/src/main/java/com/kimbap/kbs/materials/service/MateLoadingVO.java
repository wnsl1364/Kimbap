package com.kimbap.kbs.materials.service;

import java.math.BigDecimal;
import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MateLoadingVO {
    
    // 공통 컬럼들
    private String mcode;           // 자재 코드
    private String mateVerCd;       // 자재버전코드
    private String wcode;           // 창고코드
    private String wareVerCd;       // 창고버전코드
    private String fcode;           // 공장코드
    private String facVerCd;        // 공장버전코드
    private String wareAreaCd;      // 창고구역코드
    private String unit;            // 단위
    private String note;            // 비고
    private String isUsed;          // 사용여부
    private String regi;            // 등록자
    private Timestamp inboDt;       // 입고일자
    private String startDate;       // 입고일자 시작일
    private String endDate;         // 입고일자 종료일
    
    // mate_inbo 테이블 컬럼들
    private String mateInboCd;      // 자재입고코드
    private String purcDCd;         // 구매상세코드
    private String mateInboDCd;     // 자재입고상세코드
    private String lotNo;           // 로트번호
    private String supplierLotNo;   // 공급업체로트번호
    private String inboStatus;      // 입고상태
    private BigDecimal totalQty;    // 총수량
    private String mname;           // 담당자명
    private String cpCd;            // 회사코드
    private Timestamp deliDt;            // 배송일자
    
    // material 테이블 컬럼들
    private String mateName;        // 자재명
    private String mateType;        // 자재타입
    private String stoCon;          // 보관조건
    private String std;             // 규격
    private String pieceUnit;       // 개당단위
    private BigDecimal converQty;   // 환산수량
    private BigDecimal moqty;       // 최소주문수량
    private BigDecimal safeStock;   // 안전재고
    private BigDecimal edate;       // 유효기간
    private String corigin;         // 원산지
    private String modi;            // 수정자
    private String chaRea;          // 변경사유
    private Timestamp regDt;        // 등록일시
    
    // ware_stock 테이블 컬럼들
    private String wslcode;         // 창고재고위치코드
    private String prodInboCd;      // 제품입고코드
    private String itemType;        // 품목유형
    private BigDecimal qty;         // 수량

    // ware_d 테이블 컬럼들
    private BigDecimal areaRow;     // 구역행
    private BigDecimal areaCol;     // 구역열
    private BigDecimal areaFloor;   // 구역층
    private BigDecimal vol;         // 용량
    
    // warehouse 테이블 컬럼들
    private String wareName;        // 창고명
    private String wareType;        // 창고타입
    private String address;         // 주소
    private BigDecimal maxRow;      // 최대행
    private BigDecimal maxCol;      // 최대열
    private BigDecimal maxFloor;    // 최대층
    
    // factory 테이블 컬럼들
    private String facName;         // 공장명
    private String tel;             // 연락처
    private String opStatus;        // 가동상태
}