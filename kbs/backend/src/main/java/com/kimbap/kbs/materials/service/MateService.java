package com.kimbap.kbs.materials.service;

import java.util.List;

public interface MateService {
    
    // 자재입고 관련 메서드
    void insertMateInbo(MateInboVO mateInbo);
    List<MateInboVO> getMateInboList();
    void updateMateInbo(MateInboVO mateInbo);
    MateInboVO getMateInboById(String mateInboCd);
    
    // 발주 관련 메서드
    List<PurcOrdVO> getPurcOrdList();
    
    // 자재출고 관련 메서드
    List<MateRelVO> getMateRelList();
    void insertMateRel(MateRelVO mateRel);
}