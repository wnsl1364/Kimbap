import axios from 'axios';

// 입출금 내역 
// 입출금 내역 목록 조회 API (검색 조건 포함)
export const getCashflowList = (params = {}) => {
  return axios.get('/api/pay/list', { params }); // 💡 여기에 검색조건 포함
};
// 입출금 내역  등록 API
export const insertCashflow = (data) => {
  return axios.post('/api/pay/insert', data);
}
// 입출금 내역  단건 조회 API
export function getCashflowDetail(statementCd) {
  return axios.get(`/api/pay/detail/${statementCd}`);
}
// 입출금 내역  수정 API
export const updateCashflow = (data) => {
  return axios.put('/api/pay/update', data);
}
// 입금 내역 조회
export const getInComeList = () => {
  return axios.get('/api/pay/income');
};

// 미정산 정산 처리 API (입금 내역 상태 x2 + 미정산금액 차감)
export const settleUnpaid = (data) => {
  return axios.post('/api/unpaid/update', data);
};