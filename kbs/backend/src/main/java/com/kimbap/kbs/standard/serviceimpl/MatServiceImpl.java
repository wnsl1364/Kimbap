package com.kimbap.kbs.standard.serviceimpl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kimbap.kbs.standard.mapper.MatMapper;
import com.kimbap.kbs.standard.service.ChangeItemVO;
import com.kimbap.kbs.standard.service.MatService;
import com.kimbap.kbs.standard.service.MatSupplierVO;
import com.kimbap.kbs.standard.service.MatVO;
import com.kimbap.kbs.standard.service.VersionSyncService;

@Service
public class MatServiceImpl implements MatService {

    @Autowired
    private MatMapper matMapper;

    @Autowired
    private VersionSyncService versionSyncService;

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

        // 4. 등록자 정보
        if (mat.getRegi() == null || mat.getRegi().isEmpty()) {
            mat.setRegi("admin");
        }

        // ✅ 자재 등록
        matMapper.insertMat(mat);
        System.out.println("등록되는 VO: " + mat);

        // ✅ 공급사 등록
        // ✅ 공급사 등록
        if (mat.getSuppliers() != null) {
            int index = matMapper.getSupplierCountByMcode(mcode); // 기존 등록 수
            for (MatSupplierVO supplier : mat.getSuppliers()) {
                supplier.setMcode(mcode);
                supplier.setMateVerCd(mat.getMateVerCd());

                // 공급사 코드 생성: MAT-2001-SUP-01
                String mateCpCd = String.format("%s-SUP-%02d", mcode, index + 1);
                supplier.setMateCpCd(mateCpCd);

                matMapper.insertMatSupplier(supplier);
                index++; // 다음 공급사 번호 증가
            }
        }
    }

    @Override
    public Map<String, Object> getMaterialDetail(String mcode) {
        Map<String, Object> result = new HashMap<>();

        MatVO material = matMapper.getMatDetail(mcode); // 여기서 mateVerCd도 나옴
        List<MatSupplierVO> suppliers = matMapper.selectMatSuppliersByMaterial(
                mcode,
                material.getMateVerCd() // 버전 전달
        );

        result.put("material", material);
        result.put("suppliers", suppliers);

        return result;
    }

    @Override
    public List<MatVO> selectMatHistory(String mcode) {
        return matMapper.selectMatHistory(mcode);
    }

    @Transactional
    @Override
    public void updateMaterial(MatVO newMat) {
        // 1. 기존 최신 버전 조회
        MatVO oldMat = matMapper.selectLatestVersion(newMat.getMcode());
        if (oldMat == null) {
            throw new RuntimeException("존재하지 않는 자재코드: " + newMat.getMcode());
        }

        // 2. 기존 공급사 목록 조회
        List<MatSupplierVO> oldSuppliers = matMapper.selectMatSuppliersByMaterial(
                oldMat.getMcode(),
                oldMat.getMateVerCd());

        // 3. 변경 여부 판단
        boolean isChanged = !Objects.equals(oldMat.getMateName(), newMat.getMateName()) ||
                !Objects.equals(oldMat.getUnit(), newMat.getUnit()) ||
                !Objects.equals(oldMat.getMoqty(), newMat.getMoqty()) ||
                !Objects.equals(oldMat.getSafeStock(), newMat.getSafeStock()) ||
                !Objects.equals(oldMat.getStd(), newMat.getStd()) ||
                !Objects.equals(oldMat.getPieceUnit(), newMat.getPieceUnit()) ||
                !Objects.equals(oldMat.getEdate(), newMat.getEdate());

        boolean suppliersChanged = !isSameSuppliers(oldSuppliers, newMat.getSuppliers());

        if (isChanged) {
            // ✅ 기존 MATERIAL 비활성화
            matMapper.updateIsUsedOnly(oldMat.getMcode(), oldMat.getMateVerCd(), "f2", oldMat.getModi());

            // ✅ 기존 공급사 비활성화
            matMapper.updateSupplierIsUsed(oldMat.getMcode(), oldMat.getMateVerCd(), "f2");

            // ✅ 새 버전 생성
            String nextVer = getNextVersion(oldMat.getMateVerCd());
            newMat.setMateVerCd(nextVer);
            newMat.setIsUsed("f1");
            newMat.setRegDt(Timestamp.valueOf(LocalDateTime.now()));
            newMat.setModi(newMat.getModi());

            // ✅ 부모 MATERIAL INSERT
            matMapper.insertMat(newMat);

            // ✅ 자식 테이블 버전 동기화
            versionSyncService.syncMaterialVersion(newMat.getMcode(), oldMat.getMateVerCd(), nextVer);

            // ✅ 공급사 등록
            if (newMat.getSuppliers() != null && !newMat.getSuppliers().isEmpty()) {
                insertSuppliers(newMat, nextVer); // insertSuppliers 안에서 is_used = f1 세팅
            }

        } else if (suppliersChanged) {
            // ✅ 공급사만 변경 → 구버전은 그대로, 신규/변경 공급사 f1로 등록
            matMapper.updateSupplierIsUsed(oldMat.getMcode(), oldMat.getMateVerCd(), "f2");
            insertSuppliers(newMat, oldMat.getMateVerCd());
        } else if (!Objects.equals(oldMat.getIsUsed(), newMat.getIsUsed())) {
            // ✅ 사용여부만 변경
            matMapper.updateIsUsedOnly(oldMat.getMcode(), oldMat.getMateVerCd(), newMat.getIsUsed(), newMat.getModi());
        } else {
            System.out.println("⚠️ 자재 정보 변경 없음, 처리 생략");
        }
    }

    // 공급사 동일 여부 비교 메서드
    private boolean isSameSuppliers(List<MatSupplierVO> oldList, List<MatSupplierVO> newList) {
        if (oldList == null)
            oldList = Collections.emptyList();
        if (newList == null)
            newList = Collections.emptyList();

        if (oldList.size() != newList.size())
            return false;

        // 공급사 식별키(예: cpCode) 기준으로 비교
        for (int i = 0; i < oldList.size(); i++) {
            MatSupplierVO oldSup = oldList.get(i);
            MatSupplierVO newSup = newList.get(i);

            if (!Objects.equals(oldSup.getCpCd(), newSup.getCpCd()))
                return false;
            if (!Objects.equals(oldSup.getLtime(), newSup.getLtime()))
                return false;
            // 필요시 다른 컬럼 비교 추가
        }
        return true;
    }

    // 공급사 INSERT 공통 처리
    private void insertSuppliers(MatVO mat, String ver) {
        int index = 1;
        for (MatSupplierVO supplier : mat.getSuppliers()) {
            supplier.setMcode(mat.getMcode());
            supplier.setMateVerCd(ver);
            String mateCpCd = String.format("%s-%s-SUP-%02d", mat.getMcode(), ver, index);
            supplier.setMateCpCd(mateCpCd);
            matMapper.insertMatSupplier(supplier);
            index++;
        }
    }

    @Override
    public List<ChangeItemVO> getChangeHistory(String mcode) {
        List<MatVO> histories = matMapper.selectMatHistory(mcode); // 정렬: 최신 → 과거
        List<ChangeItemVO> changeItems = new ArrayList<>();

        List<MatSupplierVO> allSuppliers = matMapper.selectAllSuppliersByMcode(mcode);
        Map<String, List<MatSupplierVO>> supplierMap = new HashMap<>();
        for (MatSupplierVO s : allSuppliers) {
            supplierMap.computeIfAbsent(s.getMateVerCd(), k -> new ArrayList<>()).add(s);
        }

        for (int i = 0; i < histories.size() - 1; i++) {
            MatVO current = histories.get(i);
            MatVO prev = histories.get(i + 1);

            // 공급처 비교용 리스트 꺼내기
            List<MatSupplierVO> currSuppliers = supplierMap.getOrDefault(current.getMateVerCd(), new ArrayList<>());
            List<MatSupplierVO> prevSuppliers = supplierMap.getOrDefault(prev.getMateVerCd(), new ArrayList<>());
            // 🔻 공급처 변경 비교
            for (MatSupplierVO curr : currSuppliers) {
                MatSupplierVO matchedPrev = prevSuppliers.stream()
                        .filter(p -> p.getCpCd().equals(curr.getCpCd()))
                        .findFirst()
                        .orElse(null);

                if (matchedPrev == null) {
                    // 👉 신규 공급처 추가됨
                    changeItems.add(new ChangeItemVO(
                            "공급처 추가",
                            "-",
                            curr.getCpName() + " (단가: " + curr.getUnitPrice() + ", 리드타임: " + curr.getLtime() + ")",
                            current.getChaRea(), current.getMateVerCd(), current.getRegDt(), current.getModi()));
                } else {
                    // 👉 기존 공급처지만 단가 변경됨
                    if (!Objects.equals(curr.getUnitPrice(), matchedPrev.getUnitPrice())) {
                        changeItems.add(new ChangeItemVO(
                                "공급처 단가 변경",
                                matchedPrev.getCpName() + ": " + matchedPrev.getUnitPrice(),
                                curr.getCpName() + ": " + curr.getUnitPrice(),
                                current.getChaRea(), current.getMateVerCd(), current.getRegDt(), current.getModi()));
                    }

                    // 👉 리드타임 변경됨
                    if (!Objects.equals(curr.getLtime(), matchedPrev.getLtime())) {
                        changeItems.add(new ChangeItemVO(
                                "공급처 리드타임 변경",
                                matchedPrev.getCpName() + ": " + matchedPrev.getLtime(),
                                curr.getCpName() + ": " + curr.getLtime(),
                                current.getChaRea(), current.getMateVerCd(), current.getRegDt(), current.getModi()));
                    }
                }
            }

            // 🔻 이전에는 있었는데 지금은 사라진 공급처
            for (MatSupplierVO oldSup : prevSuppliers) {
                boolean removed = currSuppliers.stream()
                        .noneMatch(c -> c.getCpCd().equals(oldSup.getCpCd()));

                if (removed) {
                    changeItems.add(new ChangeItemVO(
                            "공급처 삭제",
                            oldSup.getCpName(),
                            "-",
                            current.getChaRea(), current.getMateVerCd(), current.getRegDt(), current.getModi()));
                }
            }

            if (!Objects.equals(current.getMateName(), prev.getMateName())) {
                changeItems.add(new ChangeItemVO("자재명", prev.getMateName(), current.getMateName(),
                        current.getChaRea(), current.getMateVerCd(), current.getRegDt(), current.getModi()));
            }
            if (!Objects.equals(current.getStd(), prev.getStd())) {
                changeItems.add(new ChangeItemVO("규격", prev.getStd(), current.getStd(),
                        current.getChaRea(), current.getMateVerCd(), current.getRegDt(), current.getModi()));
            }
            if (!Objects.equals(current.getPieceUnit(), prev.getPieceUnit())) {
                changeItems.add(new ChangeItemVO("낱개단위", prev.getPieceUnit(), current.getPieceUnit(),
                        current.getChaRea(), current.getMateVerCd(), current.getRegDt(), current.getModi()));
            }
            if (!Objects.equals(current.getConverQty(), prev.getConverQty())) {
                changeItems.add(new ChangeItemVO("환산수량",
                        prev.getConverQty() == null ? null : prev.getConverQty().toString(),
                        current.getConverQty() == null ? null : current.getConverQty().toString(),
                        current.getChaRea(), current.getMateVerCd(), current.getRegDt(), current.getModi()));
            }
            if (!Objects.equals(current.getMoqty(), prev.getMoqty())) {
                changeItems.add(new ChangeItemVO("최소발주단위",
                        prev.getMoqty() == null ? null : prev.getMoqty().toString(),
                        current.getMoqty() == null ? null : current.getMoqty().toString(),
                        current.getChaRea(), current.getMateVerCd(), current.getRegDt(), current.getModi()));
            }
            if (!Objects.equals(current.getSafeStock(), prev.getSafeStock())) {
                changeItems.add(new ChangeItemVO("안전재고",
                        prev.getSafeStock() == null ? null : prev.getSafeStock().toString(),
                        current.getSafeStock() == null ? null : current.getSafeStock().toString(),
                        current.getChaRea(), current.getMateVerCd(), current.getRegDt(), current.getModi()));
            }
            if (!Objects.equals(current.getEdate(), prev.getEdate())) {
                changeItems.add(new ChangeItemVO("소비기한",
                        prev.getEdate() == null ? null : prev.getEdate().toString(),
                        current.getEdate() == null ? null : current.getEdate().toString(),
                        current.getChaRea(), current.getMateVerCd(), current.getRegDt(), current.getModi()));
            }
        }

        // 마지막 하나는 최초 등록 버전 → 항상 넣어줌
        if (!histories.isEmpty()) {
            MatVO first = histories.get(histories.size() - 1); // 마지막이 V001
            changeItems.add(new ChangeItemVO(
                    "-", "-", "-", "-", // 변경항목 없음
                    first.getMateVerCd(),
                    first.getRegDt(),
                    first.getModi()));
        }

        return changeItems;
    }

    // 버전 코드 생성 함수 (V001 -> V002)
    private String getNextVersion(String currentVer) {
        int verNum = Integer.parseInt(currentVer.replace("V", ""));
        return String.format("V%03d", verNum + 1);
    }

}
