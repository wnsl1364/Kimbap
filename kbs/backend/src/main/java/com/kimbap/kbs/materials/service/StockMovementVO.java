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
public class StockMovementVO {
    
    // ========== 공통 필드들 ==========
    private String mcode;            // 자재마스터코드
    private String mateVerCd;        // 자재버전코드
    private String pcode;            // 제품마스터코드
    private String prodVerCd;        // 제품버전코드
    private String itemType;         // 품목유형 (h1:원자재, h2:부자재, h3:완제품)
    private String lotNo;            // LOT번호
    private String unit;             // 단위
    private String fcode;            // 공장코드
    private String facVerCd;         // 공장버전코드
    private String wcode;            // 창고코드
    private String wareVerCd;        // 창고버전코드
    private String wareAreaCd;       // 창고구역코드
    private String regi;             // 등록자
    private String modi;             // 수정자
    private Timestamp regDt;         // 등록일자
    private String note;             // 비고
    private String isUsed;           // 사용여부
    
    // ========== MOVE_REQ 테이블 전용 컬럼들 ==========
    private String moveReqCd;        // 이동요청코드
    private String moveType;         // 이동유형 (z1:내부, z2:외부)
    private String moveRea;          // 이동사유
    private String moveStatus;       // 이동상태 (d1:요청, d2:승인, d3:거절)
    private String requ;             // 요청자
    private Timestamp reqDt;         // 요청일자
    private String appr;             // 승인자
    private Timestamp appDt;         // 승인일자
    private String retuRea;          // 거절사유
    
    // ========== MOVE_REQ_D 테이블 전용 컬럼들 ==========
    private String mrdcode;          // 이동요청상세코드
    private BigDecimal moveQty;      // 이동수량
    private String depaWareCd;       // 출발창고코드
    private String depaAreaCd;       // 출발구역코드
    private String arrWareCd;        // 도착창고코드
    private String arrAreaCd;        // 도착구역코드
    
    // ========== 조인용 추가 필드들 ==========
    // 자재 정보
    private String mateName;         // 자재명
    private String mateType;         // 자재타입
    private String stoCon;           // 보관조건
    
    // 제품 정보  
    private String prodName;         // 제품명
    private String prodType;         // 제품타입
    
    // 요청자 정보
    private String requName;         // 요청자명
    private String requDept;         // 요청자부서
    
    // 승인자 정보
    private String apprName;         // 승인자명
    private String apprDept;         // 승인자부서
    
    // 창고/구역 정보
    private String depaWareName;     // 출발창고명
    private String depaAreaName;     // 출발구역명
    private String arrWareName;      // 도착창고명
    private String arrAreaName;      // 도착구역명
    
    // 공장 정보
    private String depaFcode;        // 출발공장코드
    private String depaFacName;      // 출발공장명
    private String arrFcode;         // 도착공장코드
    private String arrFacName;       // 도착공장명
    
    // ========== 화면 표시용 필드들 ==========
    private String moveTypeText;     // 이동유형 텍스트
    private String moveStatusText;   // 이동상태 텍스트
    private String itemTypeText;     // 품목유형 텍스트
    private String unitText;         // 단위 텍스트
    private String depaLocation;     // 출발위치 (공장-창고-구역)
    private String arrLocation;      // 도착위치 (공장-창고-구역)
    
    // ========== 검색/페이징용 필드들 ==========
    private String startDate;        // 검색 시작일
    private String endDate;          // 검색 종료일
    private String searchKeyword;    // 검색 키워드
    private String searchType;       // 검색 타입
    
    // ========== 재고 정보 확인용 ==========
    private BigDecimal currentStock; // 현재 재고량
    private BigDecimal availableQty; // 이동 가능 수량
    private String stockStatus;      // 재고 상태
    
    // ========== 승인 처리용 ==========
    private String approvalComment;  // 승인 의견
    private String rejectionReason;  // 거절 사유
    
    // ========== 편의 메서드들 ==========
    
    /**
     * 이동요청코드 자동 생성
     * MR-yyMMdd-001 형식
     */
    public void generateMoveReqCode() {
        if (this.moveReqCd == null || this.moveReqCd.trim().isEmpty()) {
            // 실제 구현시에는 DB에서 시퀀스를 조회해야 함
            this.moveReqCd = "MR-" + java.time.LocalDate.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyMMdd")) + "-001";
        }
    }
    
    /**
     * 이동요청상세코드 자동 생성  
     * MRD-yyMMdd-001 형식
     */
    public void generateMrdCode() {
        if (this.mrdcode == null || this.mrdcode.trim().isEmpty()) {
            // 실제 구현시에는 DB에서 시퀀스를 조회해야 함
            this.mrdcode = "MRD-" + java.time.LocalDate.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyMMdd")) + "-001";
        }
    }
    
    /**
     * 출발위치 표시 문자열 생성
     */
    public void generateDepaLocation() {
        if (this.depaFacName != null && this.depaWareName != null && this.depaAreaName != null) {
            this.depaLocation = String.format("%s - %s - %s", 
                this.depaFacName, this.depaWareName, this.depaAreaName);
        }
    }
    
    /**
     * 도착위치 표시 문자열 생성
     */
    public void generateArrLocation() {
        if (this.arrFacName != null && this.arrWareName != null && this.arrAreaName != null) {
            this.arrLocation = String.format("%s - %s - %s", 
                this.arrFacName, this.arrWareName, this.arrAreaName);
        }
    }
    
    /**
     * 공통코드 텍스트 변환
     */
    public void convertCommonCodes() {
        // 이동유형
        if ("z1".equals(this.moveType)) {
            this.moveTypeText = "내부";
        } else if ("z2".equals(this.moveType)) {
            this.moveTypeText = "외부";
        }
        
        // 이동상태
        if ("d1".equals(this.moveStatus)) {
            this.moveStatusText = "요청";
        } else if ("d2".equals(this.moveStatus)) {
            this.moveStatusText = "승인";
        } else if ("d3".equals(this.moveStatus)) {
            this.moveStatusText = "거절";
        }
        
        // 품목유형
        if ("h1".equals(this.itemType)) {
            this.itemTypeText = "원자재";
        } else if ("h2".equals(this.itemType)) {
            this.itemTypeText = "부자재";
        } else if ("h3".equals(this.itemType)) {
            this.itemTypeText = "완제품";
        }
        
        // 단위 (필요시 추가)
        this.unitText = this.unit; // 기본값
    }
    
    /**
     * 모든 계산 필드 업데이트
     */
    public void updateCalculatedFields() {
        generateDepaLocation();
        generateArrLocation();
        convertCommonCodes();
    }
    
    /**
     * 이동 가능 여부 체크
     */
    public boolean isMovable() {
        return this.currentStock != null && 
               this.moveQty != null && 
               this.currentStock.compareTo(this.moveQty) >= 0;
    }
    
    /**
     * 승인 가능 여부 체크
     */
    public boolean isApprovable() {
        return "d1".equals(this.moveStatus) && // 요청 상태
               this.moveQty != null && 
               this.moveQty.compareTo(BigDecimal.ZERO) > 0; // 수량이 0보다 큼
    }
}