package com.kimbap.kbs.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.kimbap.kbs.service.MemberVO;

@Mapper
public interface MemberMapper {
	public MemberVO getUserInfo(String id);
}
