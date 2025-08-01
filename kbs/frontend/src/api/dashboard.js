import axios from 'axios';

// 대쉬보드 건수 조회
export const dashboardTopData = () => {
  return axios.get('/api/dashboard/chartData');
};

// 파이차트 데이터
export const dashboardPieData = () => {
  return axios.get('/api/dashboard/pieData');
};

// 바 차트 데이터
export const dashboardBarData = () => {
  return axios.get('/api/dashboard/barData');
};

// 금일 주문요청 데이터
export const dashboardOrderData = () => {
  return axios.get('/api/dashboard/orderData');
};