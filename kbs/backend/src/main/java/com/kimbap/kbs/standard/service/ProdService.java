package com.kimbap.kbs.standard.service;

import java.util.List;
import java.util.Map;

public interface ProdService {
    List<ProdVO> getProdList();                         // 제품 목록 조회
    void insertProd(ProdVO prod);                       // 제품 등록
    void updateProduct(ProdVO prod);                    // 제품 수정
    Map<String, Object> getProductDetail(String pcode); // 제품 단건 조회
    List<ProdVO> selectProdHistory(String pcode);       // 제품 이력 조회 (버전 전체)
    List<ChangeItemVO> getChangeHistory(String pcode);  // 변경 항목 단위 이력
}