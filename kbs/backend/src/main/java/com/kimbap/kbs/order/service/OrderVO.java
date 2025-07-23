package com.kimbap.kbs.order.service;

import java.math.BigDecimal;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderVO {
    private String ordCd;
    private String cpCd;
    private Date ordDt;
    private String note;
    private Date deliReqDt;
    private Date deliAvailDt;
    private String ordStatus;
    private String deliAdd;
    private String regi;
    private Date regDt;
    private BigDecimal ordTotalAmount;
    private BigDecimal curPayAmount;
    private Date exPayDt;
    private Date actPayDt;
}
