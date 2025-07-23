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

        // ✅ 자재코드 자동 생성: 자재유형에 따라 분기
        if ("H1".equalsIgnoreCase(mat.getMateType())) {
            int next = matMapper.getMaxRawMaterialCode() + 1;
            if (next > 1999) throw new RuntimeException("원자재 코드 범위 초과");
            mcode = "MAT-" + next;

        } else if ("H2".equalsIgnoreCase(mat.getMateType())) {
            int next = matMapper.getMaxSubMaterialCode() + 1;
            if (next > 2999) throw new RuntimeException("부자재 코드 범위 초과");
            mcode = "MAT-" + next;

        } else {
            throw new RuntimeException("지원하지 않는 자재유형: " + mat.getMateType());
        }

        mat.setMcode(mcode);

        // ✅ 자재버전 기본값
        if (mat.getMateVerCd() == null || mat.getMateVerCd().isEmpty()) {
            mat.setMateVerCd("V001");
        }

        // ✅ 자재 등록
        matMapper.insertMat(mat);

        // ✅ 공급사 등록
        if (mat.getSuppliers() != null) {
            for (MatSupplierVO supplier : mat.getSuppliers()) {
                supplier.setMcode(mcode);
                supplier.setMateVerCd(mat.getMateVerCd());

                // 공급사 연결코드 UUID 생성
                supplier.setMateCpCd(UUID.randomUUID().toString());

                matMapper.insertMatSupplier(supplier);
            }
        }
    }
}
