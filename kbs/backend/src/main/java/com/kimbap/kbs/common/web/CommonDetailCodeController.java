package com.kimbap.kbs.common.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kimbap.kbs.common.service.CommonDetailCodeService;
import com.kimbap.kbs.common.service.CommonDetailCodeVO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
public class CommonDetailCodeController {
  private final CommonDetailCodeService commonDetailCodeService;

  @GetMapping("/{groupCd}")
  public List<CommonDetailCodeVO> getDetailCodes(@PathVariable String groupCd) {
      return commonDetailCodeService.getDetailCodes(groupCd);
  }
}
