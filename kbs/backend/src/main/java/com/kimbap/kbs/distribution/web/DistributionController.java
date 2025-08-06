package com.kimbap.kbs.distribution.web;

import java.util.List;

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
}
