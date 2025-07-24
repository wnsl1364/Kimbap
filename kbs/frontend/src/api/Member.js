import axios from 'axios';


// 회원등록
export const saveMemberAdd = (MemberAddData) => {
  return axios.post('/api/login/', MemberAddData);
};