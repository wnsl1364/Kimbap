package com.kimbap.kbs.standard.serviceimpl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kimbap.kbs.standard.mapper.MatMapper;
import com.kimbap.kbs.standard.service.MatService;
import com.kimbap.kbs.standard.service.MatSupplierVO;
import com.kimbap.kbs.standard.service.MatVO;

@Service
public class MatServiceImpl implements MatService {

    @Autowired
    private MatMapper matMapper;

    @Override
    public List<MatVO> getMatList() {
        return matMapper.getMatList();
    }

    @Transactional
@Override
public void insertMatWithSuppliers(MatVO mat) {
    String mcode;

    if ("H1".equalsIgnoreCase(mat.getMateType())) {
        int next = matMapper.getNextRawMaterialCodeBySeq();
        if (next > 1999)
            throw new RuntimeException("원자재 코드 범위 초과");
        mcode = "MAT-" + next;

    } else if ("H2".equalsIgnoreCase(mat.getMateType())) {
        int next = matMapper.getNextSubMaterialCodeBySeq();
        if (next > 2999)
            throw new RuntimeException("부자재 코드 범위 초과");
        mcode = "MAT-" + next;

    } else {
        throw new RuntimeException("지원하지 않는 자재유형: " + mat.getMateType());
    }

    System.out.println("생성된 mcode: " + mcode); // ✅ 디버깅용 로그

    // ✅ 중복 확인
    if (matMapper.existsMcode(mcode)) {
        throw new RuntimeException("이미 존재하는 자재코드: " + mcode);
    }

    mat.setMcode(mcode);

    // ✅ 버전 기본값
    if (mat.getMateVerCd() == null || mat.getMateVerCd().isEmpty()) {
        mat.setMateVerCd("V001");
    }

    // ✅ 자재 등록
    matMapper.insertMat(mat);
    System.out.println("등록되는 VO: " + mat);

    // ✅ 공급사 등록
    if (mat.getSuppliers() != null) {
        for (MatSupplierVO supplier : mat.getSuppliers()) {
            supplier.setMcode(mcode);
            supplier.setMateVerCd(mat.getMateVerCd());
            supplier.setMateCpCd(UUID.randomUUID().toString());
            matMapper.insertMatSupplier(supplier);
        }
    }
}
}