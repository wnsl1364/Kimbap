// /api/common.js
import axios from 'axios'

// 그룹코드 기반 공통코드 조회
export const getCommonCodesByGroupCd = (groupCd) => {
  return axios.get(`/api/common/${groupCd}`)
}