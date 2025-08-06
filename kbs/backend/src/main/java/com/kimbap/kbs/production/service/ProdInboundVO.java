package com.kimbap.kbs.production.service;

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
public class ProdInboundVO {
  // 제품 입고 테이블
  private String prodInboCd;    // 제품입고코드
  private String pcode;         // 제품마스터코드
  private String prodVerCd;     // 제품버전코드
  private String lotNo;         // Lot 번호
  private String inboStatus;    // 입고상태
  private Integer inboQty;      // 입고수량
  private String produProdCd;   // 생산제품코드
  private Timestamp inboDt;     // 입고일시

  // 제품 기준정보
  private BigDecimal prodUnitPrice;   // 제품단가
  private String wei;                 // 중량
  private Integer edate;              // 소비기한
  private Integer safeStock;          // 안전재고
  private String pacUnit;             // 포장단위
  private String chaRea;              // 수정사유
  private String isUsed;              // 사용여부
  private Timestamp regDt;            // 등록일자
  private BigDecimal primeCost;       // 원가
  private String modi;                // 수정자

  // 제품 입고 등록 시 필요
  private String fcode;         // 공장마스터코드
  private String facVerCd;      // 공장버전코드

  // 제품 적재 대기 조회 시 필요
  private String prodName;
  private String prodType;
  private String stoTemp;
  private String unit;
  private String facName;        
  private Integer totalQty;      // 적재 대기 수량
  private BigDecimal qty;        // 실제 적재 수량 (입력값)
  private String wslcode;        // 적재제품목록코드
  private String wareAreaCd;     // 구역코드 (선택된 구역)
  private String regi;           // 등록자
  private String itemType;       // 품목타입
  private String note;           // 비고

  // ========== 기존 ware_d 테이블 컬럼들 ==========
  private String areaRow;         // 구역행 (A, B, C...)
  private BigDecimal areaCol;     // 구역열 (1, 2, 3...)
  private BigDecimal areaFloor;   // 구역층 (1, 2, 3...)
  private BigDecimal vol;         // 용량
  
  private Integer lastSeq;        // 창고재고목록코드 마지막 순번

  /**
   * 현재 적재량 (계산 필드)
   * 해당 구역에 현재 적재된 총 수량
   */
  private Integer currentVolume;
  /**
   * 잔여 용량 (계산 필드)
   * vol - currentVolume으로 계산되는 값
   */
  private Integer availableVolume;
  
  /**
   * 현재 적재된 제품코드 (조회 필드)
   * 해당 구역에 적재된 제품의 pcode (동일 제품 체크용)
   */
  private String currentProduct;
  
  /**
   * 현재 적재된 제품명 (조회 필드)
   * 현재 적재된 제품의 이름
   */
  private String currentProductName;
  
  /**
   * 적재 가능 여부 (계산 필드)
   * 동일 자재이고 용량이 충분하면 true
   */
  private Boolean isAvailable;
  
  /**
   * 동일 제품 여부 (계산 필드)
   * 현재 제품과 적재하려는 제품이 같으면 true
   */
  private Boolean isSameProduct;
  
  /**
   * 분할 적재 수량 (입력 필드)
   * 용량 초과 시 분할하여 적재할 수량
   */
  private Integer allocateQty;
  
  /**
   * 구역 표시명 (계산 필드)
   * 행+열 조합 (예: A1, B2, C3...)
   */
  private String displayName;
}
