package com.kimbap.kbs.standard.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.kimbap.kbs.standard.mapper.VersionSyncMapper;

@Service
@RequiredArgsConstructor
public class VersionSyncService {

    private final VersionSyncMapper versionSyncMapper;

    /**
     * 공장 기준정보 버전 동기화 처리
     */
    public void syncFactoryVersion(String fcode, String oldVer, String newVer) {
        List<String> targetTables = List.of("FACTORY_MAX", "PROD_PLAN", "PROD_INBO", "WAREHOUSE");

        for (String table : targetTables) {
            versionSyncMapper.updateVersionDynamic(
                table,
                "FCODE",        // 공장코드 컬럼명
                "FAC_VER_CD",   // 버전 컬럼명
                fcode,
                oldVer,
                newVer
            );
        }
    }

    // 자재, 제품도 마찬가지로 동일하게 확장 가능
    public void syncMaterialVersion(String mcode, String oldVer, String newVer) {
        List<String> matTables = List.of("BOM_D",
        "MATE_INBO",
        "MATE_REL",
        "MATE_SUPPLIER",
        "MOVE_REQ_D",
        "MRP_D",
        "PURC_ORD_D",
        "SUPPLY_PLAN_D");
        for (String table : matTables) {
            versionSyncMapper.updateVersionDynamic(
                table,
                "MCODE",
                "MATE_VER_CD",
                mcode,
                oldVer,
                newVer
            );
        }
    }

    // 제품 
    public void syncProductVersion(String pcode, String oldVer, String newVer) {
        List<String> productTables = List.of(
            "BOM",
            "FACTORY_MAX",
            "MOVE_REQ_D",
            "PROD_INBO",
            "PROD_PLAN_D",
            "PROD_REL",
            "PROD_REQ"
            // "ORDER_D"는 애초에 제외됨
        );

        for (String table : productTables) {
            versionSyncMapper.updateVersionDynamic(
                table,
                "PCODE",         // 기준 코드 컬럼명
                "PROD_VER_CD",   // 버전 컬럼명
                pcode,
                oldVer,
                newVer
            );
        }
    }

    // 창고
    public void syncWarehouseVersion(String wcode, String oldVer, String newVer) {
        List<String> warehouseTables = List.of(
            "RELEASE_ORD",
            "WARE_D"
        );

        for (String table : warehouseTables) {
            versionSyncMapper.updateVersionDynamic(
                table,
                "WCODE",         // 창고 코드 컬럼
                "WARE_VER_CD",   // 창고 버전 컬럼
                wcode,
                oldVer,
                newVer
            );
        }
    }
}