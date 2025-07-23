package com.kimbap.kbs.materials.service;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MateVO {
    private String purcCd;
    private Timestamp ordDt;
    private String regi;
    private String purcStatus;
    private Double ordTotalAmount;
}
