-- 주문 마스터 (CP-101, 출고완료 상태)
INSERT INTO order_list (
  ord_cd, cp_cd, ord_dt, note, deli_req_dt,
  deli_add, ex_pay_dt, ord_total_amount, cur_pay_amount,
  ord_status_internal, ord_status_customer,
  regi, reg_dt, is_used
) VALUES (
  'ORD-20250015', 'CP-101', SYSDATE, '출고완료 주문 테스트', SYSDATE + 3,
  '서울시 강남구 테헤란로 303', SYSDATE + 7, 100000, 0,
  'a2', 's3',
  'MEM-CP-0026', SYSDATE, 'f1'
);

-- 주문 상세 (제품: 진미채김밥)
INSERT INTO order_d (
  ord_d_cd, ord_cd, pcode, prod_ver_cd,
  ord_qty, deli_avail_dt, unit_price,
  ord_d_status, is_used
) VALUES (
  'ORDD-2025-000012', 'ORD-20250015', 'PROD-1008', 'V001',
  3, SYSDATE + 2, 180000,
  't3',
  'f1'
);

-- 주문 상세 (제품: 치즈돈까스김밥)
INSERT INTO order_d (
  ord_d_cd, ord_cd, pcode, prod_ver_cd,
  ord_qty, deli_avail_dt, unit_price,
  ord_d_status, is_used
) VALUES (
  'ORDD-2025-000013', 'ORD-20250015', 'PROD-1009', 'V001',
  2, SYSDATE + 2, 152000,
  't3',
  'f1'
);

-- 주문 마스터 (CP-101, 출고완료 상태)
INSERT INTO order_list (
  ord_cd, cp_cd, ord_dt, note, deli_req_dt,
  deli_add, ex_pay_dt, ord_total_amount, cur_pay_amount,
  ord_status_internal, ord_status_customer,
  regi, reg_dt, is_used
) VALUES (
  'ORD-20250016', 'CP-101', SYSDATE, '출고완료 주문 테스트', SYSDATE + 3,
  '서울시 강남구 테헤란로 303', SYSDATE + 7, 100000, 0,
  'a2', 's3',
  'MEM-CP-0026', SYSDATE, 'f1'
);

-- 주문 상세 (제품: 진미채김밥)
INSERT INTO order_d (
  ord_d_cd, ord_cd, pcode, prod_ver_cd,
  ord_qty, deli_avail_dt, unit_price,
  ord_d_status, is_used
) VALUES (
  'ORDD-2025-000014', 'ORD-20250016', 'PROD-1008', 'V001',
  3, SYSDATE + 2, 180000,
  't3',
  'f1'
);

-- 주문 상세 (제품: 치즈돈까스김밥)
INSERT INTO order_d (
  ord_d_cd, ord_cd, pcode, prod_ver_cd,
  ord_qty, deli_avail_dt, unit_price,
  ord_d_status, is_used
) VALUES (
  'ORDD-2025-000015', 'ORD-20250016', 'PROD-1009', 'V001',
  2, SYSDATE + 2, 152000,
  't3',
  'f1'
);


-- 주문 마스터 (CP-101, 출고완료 상태)
INSERT INTO order_list (
  ord_cd, cp_cd, ord_dt, note, deli_req_dt,
  deli_add, ex_pay_dt, ord_total_amount, cur_pay_amount,
  ord_status_internal, ord_status_customer,
  regi, reg_dt, is_used
) VALUES (
  'ORD-20250017', 'CP-101', SYSDATE, '출고완료 주문 테스트', SYSDATE + 3,
  '서울시 강남구 테헤란로 303', SYSDATE + 7, 100000, 0,
  'a2', 's3',
  'MEM-CP-0026', SYSDATE, 'f1'
);

-- 주문 상세 (제품: 진미채김밥)
INSERT INTO order_d (
  ord_d_cd, ord_cd, pcode, prod_ver_cd,
  ord_qty, deli_avail_dt, unit_price,
  ord_d_status, is_used
) VALUES (
  'ORDD-2025-000016', 'ORD-20250017', 'PROD-1008', 'V001',
  3, SYSDATE + 2, 180000,
  't3',
  'f1'
);

-- 주문 상세 (제품: 치즈돈까스김밥)
INSERT INTO order_d (
  ord_d_cd, ord_cd, pcode, prod_ver_cd,
  ord_qty, deli_avail_dt, unit_price,
  ord_d_status, is_used
) VALUES (
  'ORDD-2025-000017', 'ORD-20250017', 'PROD-1009', 'V001',
  2, SYSDATE + 2, 152000,
  't3',
  'f1'
);

INSERT INTO order_list (
  ord_cd, cp_cd, ord_dt, note, deli_req_dt,
  deli_add, ex_pay_dt, ord_total_amount, cur_pay_amount,
  ord_status_internal, ord_status_customer,
  regi, reg_dt, is_used
) VALUES (
  'ORD-20250021', 'CP-101', SYSDATE, '반품요청 주문 테스트', SYSDATE + 3,
  '서울시 강남구 테헤란로 303', SYSDATE + 7, 100000, 0,
  'a2', 'v1',
  'MEM-CP-0026', SYSDATE, 'f1'
);

INSERT INTO order_d (
  ord_d_cd, ord_cd, pcode, prod_ver_cd,
  ord_qty, deli_avail_dt, unit_price,
  ord_d_status, is_used
) VALUES (
  'ORDD-2025-000023', 'ORD-20250021', 'PROD-1009', 'V001',
  3, SYSDATE + 2, 180000,
  't4',
  'f1'
);

INSERT INTO prod_return (
  prod_return_cd, ord_d_cd, lot_no,
  return_dt, return_qty, return_amount,
  return_rea, return_status_customer
) VALUES (
  'RTN-20250026', 'ORDD-2025-000023', 'LOT-20250806-0001',
  SYSDATE, 1, 180000,
  '상품불량', 'v1'
);

commit;