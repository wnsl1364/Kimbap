import axios from 'axios';

// 반품 요청 등록
export const registerReturn = (data) => {
  return axios.post('/api/return/register', data);
};

// 반품 이력 조회
export const getReturnHistory = (ordCd) => {
  return axios.get(`/api/return/history/${ordCd}`);
};

// 주문 상세 조회
export const getOrderDetail = (ordCd) => {
  return axios.get(`/api/order/${ordCd}`);
};