package com.kimbap.kbs.distribution.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.kimbap.kbs.distribution.service.DistributionVO;
import com.kimbap.kbs.distribution.service.LotStockVO;
import com.kimbap.kbs.distribution.service.RelDetailVO;
import com.kimbap.kbs.distribution.service.RelOrdModalVO;
import com.kimbap.kbs.distribution.service.RelOrderAndResultVO;
import com.kimbap.kbs.distribution.service.ReleaseMasterOrdVO;
import com.kimbap.kbs.distribution.service.ReleaseOrdVO;
import com.kimbap.kbs.distribution.service.WarehouseVO;

@Mapper
public interface DistributionMapper {
  // 입출고조회
  List<DistributionVO> getInOutCheck(DistributionVO filter);

  // 출고 지시서 조회
  List<RelOrderAndResultVO> getRelOrdList(RelOrderAndResultVO filter);

  // 출고 지시서 등록 모달관련
  List<RelOrdModalVO> getRelOrdModal(RelOrdModalVO vo);

  // 모달 선택 후 주문 상세 출력
  List<RelOrdModalVO> getRelOrdSelect(String ordCd);

  // 출고 지시서 상세 조회
   RelOrderAndResultVO getRelOrdDetail(String relMasCd);
   List<ReleaseOrdVO> getRelOrdProductList(String relMasCd); 

  // 창고 목록 조회
  List<WarehouseVO> getWarehouseListByOrdCd(String ordCd);

  // 출고 지시서 등록
  void insertReleaseOrdMaster(ReleaseMasterOrdVO masterVO);

  void insertReleaseOrdList(List<ReleaseOrdVO> releaseList);

  int updateOrdStatusCustomer(@Param("ordCd") String ordCd);

  // 출고마스터코드 자동 생성
  String selectNewRelMasCd();

  int selectMaxRelOrdSeqToday();

  // 출고지시서 불러오기 모달
  List<RelOrderAndResultVO> getRelOrdListWaiting();

  // 출고처리 상세
  List<RelDetailVO> getRelDetails(@Param("relMasCd") String relMasCd);

  // lot
  List<LotStockVO> getLotsByPcode(@Param("pcode") String pcode);

  // 출고처리 코드
  String nextProdRelCd();

  // 재고 잠금 & 차감
List<Map<String, Object>> selectLotQtyRowsForUpdate(@Param("lotNo") String lotNo,
                                                    @Param("wareAreaCd") String wareAreaCd);

int decreaseLotQtyByRowId(@Param("rid") String rid,
                          @Param("qty") Integer qty);
  // 지시 상태 갱신 및 합계
  int updateRelOrderStatus(@Param("relMasCd") String relMasCd,
      @Param("status") String status);

  // (선택) 주문상세 상태 갱신
  int updateOrderDetailStatus(@Param("ord_d_cd") String ordDCd,
      @Param("status") String status);

  // 최신 제품버전 조회 (없으면 null)
  String selectLatestProdVerCd(@Param("pcode") String pcode);

  // prod_rel 한 행 INSERT (LOT 1건 = 행 1건)
  int insertProdRel(@Param("prodRelCd") String prodRelCd,
                    @Param("lotNo") String lotNo,
                    @Param("relQty") Integer relQty,
                    @Param("relOrdCd") String relOrdCd,
                    @Param("ordDCd") String ordDCd,
                    @Param("pcode") String pcode,
                    @Param("prodVerCd") String prodVerCd);

  java.math.BigDecimal selectUnitPriceByOrdDCd(@Param("ord_d_cd") String ordDCd);     
  
  int increaseCompanyUnsettledAmount(@Param("cpCd") String cpCd,
                                   @Param("amount") java.math.BigDecimal amount);

  int updateCustomerOrderStatus(@Param("ordCd") String ordCd,
                              @Param("status") String status);      
                              
  String selectOrdCdByOrdDCd(@Param("ord_d_cd") String ord_d_cd);

  int countRemainingQtyByOrdCd(@Param("ordCd") String ordCd);

  void updateOrderDetailStatusToT3(String ordDCd);

  Integer selectOrderQtyByOrdDCd(String ordDCd);
  
  Integer sumReleasedQtyByOrdDCd(String ordDCd);

  Long selectTotalRequestQtyByRelMasCd(String relMasCd);

  Long selectTotalReleasedQtyAllByRelMasCd(String relMasCd);

}
