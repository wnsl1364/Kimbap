// api/materials.js
import axios from 'axios';

// 자재입고 관련 API 함수들
export const getMaterialInboundList = () => {
  return axios.get('/api/materials/inbound');
};

export const getMaterialInboundById = (mateInboCd) => {
  return axios.get(`/api/materials/inbound/${mateInboCd}`);
};

export const saveMaterialInbound = (inboundData) => {
  return axios.post('/api/materials/inbound', inboundData);
};

export const updateMaterialInbound = (inboundData) => {
  return axios.put('/api/materials/inbound', inboundData);
};

// 발주 목록 조회 (자재입고 시 참조용)
export const getPurchaseOrderList = () => {
  return axios.get('/api/materials/purchase-orders');
};

// 자재출고 관련 API 함수들
export const getMaterialOutboundList = () => {
  return axios.get('/api/materials/outbound');
};

export const saveMaterialOutbound = (outboundData) => {
  return axios.post('/api/materials/outbound', outboundData);
};