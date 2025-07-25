package com.kimbap.kbs.materials.mapper;

import java.util.List;

import com.kimbap.kbs.materials.service.MaterialsVO;
import com.kimbap.kbs.materials.service.SearchCriteria;

public interface MateMapper {
    void insertPurcOrd(MaterialsVO purcOrd);
    void updatePurcOrd(MaterialsVO purcOrd);
    List<MaterialsVO> getPurcOrdList();
    List<MaterialsVO> getPurcOrdList(SearchCriteria criteria);

    void insertMateInbo(MaterialsVO mateInbo);
    List<MaterialsVO> getMateInboList();
    void updateMateInbo(MaterialsVO mateInbo);
    MaterialsVO getMateInboById(String mateInboCd);

    List<MaterialsVO> getMateRelList();
    void insertMateRel(MaterialsVO mateRel);
}