package com.kimbap.kbs.materials.mapper;

import java.util.List;

import com.kimbap.kbs.materials.service.MateInboVO;
import com.kimbap.kbs.materials.service.MateRelVO;
import com.kimbap.kbs.materials.service.PurcOrdVO;

public interface MateMapper {
    void insertPurcOrd(PurcOrdVO purcOrd); // 발주 등록
    void updatePurcOrd(PurcOrdVO purcOrd); // 발주 수정
    List<PurcOrdVO> getPurcOrdList(); // 발주 목록 조회
    
    void insertMateInbo(MateInboVO mateInbo); // 자재입고 처리
    List<MateInboVO> getMateInboList(); // 자재입고 목록 조회
    void updateMateInbo(MateInboVO mateInbo); // 자재 적재 대기, 처리
    MateInboVO getMateInboById(String mateInboCd); // 자재입고 단건 조회 추가

    List<MateRelVO> getMateRelList(); // 자재출고 목록 조회
    void insertMateRel(MateRelVO mateRel); // 자재출고 등록
}