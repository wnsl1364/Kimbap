package com.kimbap.kbs.standard.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VersionSyncMapper {
    int updateVersionDynamic(@Param("table") String table,
                             @Param("codeCol") String codeCol,
                             @Param("verCol") String verCol,
                             @Param("code") String code,
                             @Param("oldVer") String oldVer,
                             @Param("newVer") String newVer);
}
