// /stores/commonStore.js
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getCommonCodesByGroupCd } from '@/api/common' // API 파일에서 가져오는 함수

export const useCommonStore = defineStore('common', () => {
  // 공통코드 객체 (key: groupCd, value: 코드 리스트)
  const commonCodes = ref({})

  // 공통코드 fetch
  const fetchCommonCodes = async (groupCd) => {
    try {
      const res = await getCommonCodesByGroupCd(groupCd)
      commonCodes.value[groupCd] = res.data
    } catch (err) {
      console.error(`공통코드(${groupCd}) 조회 실패:`, err)
    }
  }

  // getter-like computed 함수
  const getCodes = (groupCd) => {
    return commonCodes.value[groupCd] || []
  }

  return {
    commonCodes,
    fetchCommonCodes,
    getCodes
  }
})