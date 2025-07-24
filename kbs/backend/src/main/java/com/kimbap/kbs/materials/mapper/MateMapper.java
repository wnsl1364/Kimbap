package com.kimbap.kbs.materials.mapper;

import java.util.List;

import com.kimbap.kbs.materials.service.MaterialsVO;

public interface MateMapper {
    void insertPurcOrd(MaterialsVO purcOrd); // 발주 등록
    void updatePurcOrd(MaterialsVO purcOrd); // 발주 수정
    List<MaterialsVO> getPurcOrdList(); // 발주 목록 조회

    void insertMateInbo(MaterialsVO mateInbo); // 자재입고 처리
    List<MaterialsVO> getMateInboList(); // 자재입고 목록 조회
    void updateMateInbo(MaterialsVO mateInbo); // 자재 적재 대기, 처리
    MaterialsVO getMateInboById(String mateInboCd); // 자재입고 단건 조회 추가

    List<MaterialsVO> getMateRelList(); // 자재출고 목록 조회
    void insertMateRel(MaterialsVO mateRel); // 자재출고 등록
}