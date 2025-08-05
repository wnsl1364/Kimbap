package com.kimbap.kbs.distribution.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kimbap.kbs.distribution.service.DistributionService;
import com.kimbap.kbs.distribution.service.DistributionVO;
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

    @GetMapping("/relOrdList")
    public List<RelOrderAndResultVO> getRelOrdList() {
        return distributionService.getRelOrdList();
    }
}
