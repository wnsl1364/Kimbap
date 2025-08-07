import axios from 'axios';

// ✅ axios 인터셉터 설정 - 모든 요청에 자동으로 토큰 추가
axios.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    } else {
      console.log('❌ 토큰이 없습니다');
    }
    return config;
  },
  (error) => {
    console.log('❌ 요청 인터셉터 오류:', error);
    return Promise.reject(error);
  }
);

// ✅ 응답 인터셉터 설정 - 토큰 만료 시 자동 처리
axios.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      console.log('❌ 토큰 만료 또는 인증 실패');
      localStorage.removeItem('token');
      delete axios.defaults.headers.common['Authorization'];
      // 필요시 로그인 페이지로 리다이렉트
      // window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

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
  // 로그아웃 시 토큰 삭제
  localStorage.removeItem('token');
  delete axios.defaults.headers.common['Authorization'];
  return axios.post('/api/logout', null, {
    withCredentials: true
  });
};

// 로그인 성공 후 응답 처리

export const login = async(loginData) => {
  const response = await axios.post('/api/login', loginData);

  if (response.data && response.data.token) {
    // ✅ 로컬스토리지에 토큰 저장
    localStorage.setItem('token', response.data.token);
    console.log('✅ JWT 토큰 저장 완료');
    
    // 대시보드로 이동
    navigate('/dashboard');
  }
}
// export const login = async (loginData) => {
//   try {
//     const response = await axios.post('/api/login', loginData);
    
//     if (response.data && response.data.token) {
//       localStorage.setItem('token', response.data.token);
//       console.log('✅ JWT 토큰 저장 완료');
//       return response.data;
//     }
    
//   } catch (error) {
//     console.error('❌ 로그인 실패:', error);
//     throw error;
//   }
// };