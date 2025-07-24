package com.kimbap.kbs.production.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.kimbap.kbs.production.service.FactoryVO;

@Mapper
public interface FactoryMapper {
    List<FactoryVO> getFactoryList(); // 공장 목록 조회


}