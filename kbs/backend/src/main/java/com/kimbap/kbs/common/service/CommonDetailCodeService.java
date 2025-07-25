package com.kimbap.kbs.common.service;

import java.util.List;

public interface CommonDetailCodeService {
  List<CommonDetailCodeVO> getDetailCodes(String groupCd);
}
