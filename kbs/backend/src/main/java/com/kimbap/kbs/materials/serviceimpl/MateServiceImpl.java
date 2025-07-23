package com.kimbap.kbs.materials.serviceimpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kimbap.kbs.materials.mapper.MateMapper;
import com.kimbap.kbs.materials.service.MateInboVO;
import com.kimbap.kbs.materials.service.MateRelVO;
import com.kimbap.kbs.materials.service.MateService;
import com.kimbap.kbs.materials.service.PurcOrdVO;

@Service
@Transactional
public class MateServiceImpl implements MateService {

    @Autowired
    private MateMapper mateMapper;

    @Override
    public void insertMateInbo(MateInboVO mateInbo) {
        mateMapper.insertMateInbo(mateInbo);
    }

    @Override
    public List<MateInboVO> getMateInboList() {
        return mateMapper.getMateInboList();
    }

    @Override
    public void updateMateInbo(MateInboVO mateInbo) {
        mateMapper.updateMateInbo(mateInbo);
    }

    @Override
    public MateInboVO getMateInboById(String mateInboCd) {
        return mateMapper.getMateInboById(mateInboCd);
    }

    @Override
    public List<PurcOrdVO> getPurcOrdList() {
        return mateMapper.getPurcOrdList();
    }

    @Override
    public List<MateRelVO> getMateRelList() {
        return mateMapper.getMateRelList();
    }

    @Override
    public void insertMateRel(MateRelVO mateRel) {
        mateMapper.insertMateRel(mateRel);
    }
}