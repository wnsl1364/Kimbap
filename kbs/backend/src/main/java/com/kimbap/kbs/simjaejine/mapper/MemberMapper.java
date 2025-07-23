package com.kimbap.kbs.simjaejine.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.kimbap.kbs.simjaejine.service.MemberVO;

@Mapper
public interface MemberMapper {
	public MemberVO getUserInfo(String id);
}
