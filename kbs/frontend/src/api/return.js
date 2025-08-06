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

// 반품 목록 조회
export const getReturnList = (params) => {
  return axios.get('/api/return/list', { params });
};

// 반품 승인 처리
export const approveReturn = (returnCdList) => {
  return axios.put('/api/return/approve', returnCdList);
};

// 반품 거절 처리
export const rejectReturn = (payload) => {
  return axios.put('/api/return/reject', payload);
};

// 반품 취소 요청
// export const cancelReturn = (ordDCdList) => {
//   return axios.put('/api/return/cancel', { ordDCdList });
// };
export const cancelReturn = (ordDCdList) => {
  return axios({
    method: 'put',
    url: '/api/return/cancel',
    data: ordDCdList  // 배열 자체를 그대로 전달
  });
};