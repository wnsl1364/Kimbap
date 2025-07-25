package com.kimbap.kbs.common.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.kimbap.kbs.common.service.CommonDetailCodeVO;

@Mapper
public interface CommonDetailCodeMapper {
  List<CommonDetailCodeVO> selectDetailCodesByGroup(String groupCd);
}
