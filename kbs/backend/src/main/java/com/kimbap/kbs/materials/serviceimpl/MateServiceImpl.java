package com.kimbap.kbs.materials.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kimbap.kbs.materials.mapper.MateMapper;
import com.kimbap.kbs.materials.service.MateService;
import com.kimbap.kbs.materials.service.MaterialsVO;
import com.kimbap.kbs.materials.service.SearchCriteria;

@Service
@Transactional
public class MateServiceImpl implements MateService {

    @Autowired
    private MateMapper mateMapper;

    @Override
    public void insertMateInbo(MaterialsVO mateInbo) {
        mateMapper.insertMateInbo(mateInbo);
    }

    @Override
    public List<MaterialsVO> getMateInboList() {
        return mateMapper.getMateInboList();
    }

    @Override
    public void updateMateInbo(MaterialsVO mateInbo) {
        mateMapper.updateMateInbo(mateInbo);
    }

    @Override
    public MaterialsVO getMateInboById(String mateInboCd) {
        return mateMapper.getMateInboById(mateInboCd);
    }

    @Override
    public List<MaterialsVO> getPurcOrdList() {
        return mateMapper.getPurcOrdList();
    }

    @Override
    public List<MaterialsVO> getMateRelList() {
        return mateMapper.getMateRelList();
    }

    @Override
    public void insertMateRel(MaterialsVO mateRel) {
        mateMapper.insertMateRel(mateRel);
    }

    @Override
    public List<MaterialsVO> getPurchaseOrders(SearchCriteria criteria) {
        return mateMapper.getPurcOrdList(criteria);
    }
}