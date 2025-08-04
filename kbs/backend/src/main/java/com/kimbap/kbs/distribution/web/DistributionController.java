package com.kimbap.kbs.distribution.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kimbap.kbs.distribution.service.DistributionService;
import com.kimbap.kbs.distribution.service.DistributionVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/distribution")
@RequiredArgsConstructor
public class DistributionController {
    private final DistributionService distributionService;

    @GetMapping("/distributionInOut")
    public List<DistributionVO> getInOutCheck() {
        return distributionService.getInOutCheck();
    }
}
