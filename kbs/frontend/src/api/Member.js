import axios from 'axios';

// 회원등록
export const saveMemberAdd = (Data) => {
  return axios.post('/api/memberAdd', Data)
};

// 사원조회
export const cpListCheck= () => {
  return axios.get('/api/cpList')
};

// 거래처조회
export const empListCheck = () => {
  return axios.get('/api/empList')
};

// 로그아웃
export const logout = () => {
  return axios.post('/api/logout', null, {
    withCredentials: true
  });
};