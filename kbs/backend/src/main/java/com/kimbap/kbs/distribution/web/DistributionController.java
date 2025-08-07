package com.kimbap.kbs.distribution.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kimbap.kbs.distribution.service.DistributionService;
import com.kimbap.kbs.distribution.service.DistributionVO;
import com.kimbap.kbs.distribution.service.RelOrdModalVO;
import com.kimbap.kbs.distribution.service.RelOrderAndResultVO;
import com.kimbap.kbs.distribution.service.ReleaseMasterOrdVO;
import com.kimbap.kbs.distribution.service.ReleaseOrdVO;
import com.kimbap.kbs.distribution.service.WarehouseVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/distribution")
@RequiredArgsConstructor
public class DistributionController {
    private final DistributionService distributionService;

    @PostMapping("/distributionInOut")
    public List<DistributionVO> getInOutCheck(@RequestBody DistributionVO filter) {
        return distributionService.getInOutCheck(filter);
    }

    @PostMapping("/relOrdList")
    public List<RelOrderAndResultVO> getRelOrdList(@RequestBody RelOrderAndResultVO filter) {
        return distributionService.getRelOrdList(filter);
    }

    @PostMapping("/relOrderModal")
    public List<RelOrdModalVO> getRelOrderModalList(@RequestBody RelOrdModalVO vo) {
        return distributionService.getRelOrdModal(vo);
    }

    // 출고지시서 모달 선택 후 상세 제품 조회
    @GetMapping("/relOrderSelect")
    public List<RelOrdModalVO> getRelOrderSelect(@RequestParam("ordCd") String ordCd) {
        return distributionService.getRelOrdSelect(ordCd);
    }

    // 창고 목록 조회
    @GetMapping("/warehouseList")
    public List<WarehouseVO> getWarehousesByOrdCd(@RequestParam String ordCd) {
        return distributionService.getWarehouseListByOrdCd(ordCd);
    }

    // 출고 지시서 등록
    @PostMapping("/insertReleaseOrders")
    public ResponseEntity<String> insertReleaseOrders(
        @RequestBody ReleaseOrderRequest request
    ) {
        distributionService.saveReleaseOrder(request.getMaster(), request.getDetailList());
        return ResponseEntity.ok("출고지시서 저장 완료");
    }

    // 요청 DTO
    public static class ReleaseOrderRequest {
        private ReleaseMasterOrdVO master;
        private List<ReleaseOrdVO> detailList;

        public ReleaseMasterOrdVO getMaster() {
            return master;
        }

        public void setMaster(ReleaseMasterOrdVO master) {
            this.master = master;
        }

        public List<ReleaseOrdVO> getDetailList() {
            return detailList;
        }

        public void setDetailList(List<ReleaseOrdVO> detailList) {
            this.detailList = detailList;
        }
    }
}
