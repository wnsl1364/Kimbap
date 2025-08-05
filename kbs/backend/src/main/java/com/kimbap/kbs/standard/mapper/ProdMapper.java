package com.kimbap.kbs.standard.mapper;

import java.util.List;

import com.kimbap.kbs.standard.service.ProdVO;

public interface ProdMapper {
    List<ProdVO> getProdList(); // 제품 목록 조회\
    void insertProd(ProdVO prod); // 제품 등록
    ProdVO getProdDetail(String pcode); // 제품단건조회
    int existsPcode(String pcode);  // 제품코드 존재 여부 확인
    List<ProdVO> selectProdHistory(String pcode); // 자재기준정보 이력조회
    ProdVO selectLatestVersion(String pcode); // 최신 버전 조회
    int disableOldVersion(String pcode); // 기존 버전 비활성화
    int getNextRawProductCodeBySeq(); // 시퀀스 
    void updateIsUsedOnly(String pcode, String prodVerCd, String isUsed, String modi); // 사용여부만 업데이트
}
