package com.kimbap.kbs.materials.service;

import java.util.List;
public interface MateService {
    
    // 자재입고 관련 메서드
    void insertMateInbo(MaterialsVO mateInbo);
    List<MaterialsVO> getMateInboList();
    void updateMateInbo(MaterialsVO mateInbo);
    MaterialsVO getMateInboById(String mateInboCd);

    // 발주 관련 메서드
    List<MaterialsVO> getPurcOrdList();

    // 자재출고 관련 메서드
    List<MaterialsVO> getMateRelList();
    void insertMateRel(MaterialsVO mateRel);
}