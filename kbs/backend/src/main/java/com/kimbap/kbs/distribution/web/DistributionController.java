package com.kimbap.kbs.distribution.web;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kimbap.kbs.distribution.service.DistributionService;
import com.kimbap.kbs.distribution.service.DistributionVO;
import com.kimbap.kbs.distribution.service.LotStockVO;
import com.kimbap.kbs.distribution.service.RelDetailVO;
import com.kimbap.kbs.distribution.service.RelOrdModalVO;
import com.kimbap.kbs.distribution.service.RelOrderAndResultVO;
import com.kimbap.kbs.distribution.service.ReleaseMasterOrdVO;
import com.kimbap.kbs.distribution.service.ReleaseOrdVO;
import com.kimbap.kbs.distribution.service.ReleaseRequestVO;
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

    @GetMapping("/waiting")
    public List<RelOrderAndResultVO> getRelOrdListWaiting() {
        return distributionService.getRelOrdListWaiting();
    }

    @GetMapping("/details/{relMasCd}")
    public List<RelDetailVO> getRelDetails(@PathVariable String relMasCd) {
        return distributionService.getRelDetails(relMasCd);
    }

    @GetMapping("/lots")
    public List<LotStockVO> getLotsByPcode(@RequestParam String pcode) {
        return distributionService.getLotsByPcode(pcode);
    }

    @PostMapping("/release")
    public ResponseEntity<String> saveRelease(@RequestBody ReleaseRequestVO vo) {
        System.out.println("[DEBUG] relMasCd=" + vo.getRelMasCd());
        if (vo.getItems() != null && !vo.getItems().isEmpty()) {
            var it0 = vo.getItems().get(0);
            System.out.println("[DEBUG] item0.relOrdCd=" + it0.getRelOrdCd()
                + ", ord_d_cd=" + it0.getOrd_d_cd()
                + ", pcode=" + it0.getPcode());
        }
        String prodRelCd = distributionService.insertRelease(vo);
        return ResponseEntity.ok(prodRelCd);
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

    // 출고 지시서 단건 조회
    @GetMapping("/relOrdDetail")
    public Map<String, Object> getRelOrderDetailFull(@RequestParam("relMasCd") String relMasCd) {
        RelOrderAndResultVO master = distributionService.getRelOrdDetail(relMasCd);
        List<ReleaseOrdVO> products = distributionService.getRelOrdProductList(relMasCd);
        return Map.of("master", master, "products", products);
    }
}
