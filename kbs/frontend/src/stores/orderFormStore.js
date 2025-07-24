// stores/orderFormStore.js
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useOrderFormStore = defineStore('orderForm', () => {
  const formData = ref({
    ordCd: '',
    ordDt: '',
    cpCd: '',      // 추가
    cpName: '',    // 거래처명
    deliAdd: '',   // 배송지 주소
    deliReqDt: '',
    exPayDt: '',
    note: '',
    regi: ''       // 추가
  })


  const setFormData = (data) => {
    formData.value = data
  }

  const resetForm = () => {
    formData.value = {
      ordCd: '',
      ordDt: '',
      cpName: '',
      deliAdd: '',
      deliReqDt: '',
      exPayDt: '',
      note: ''
    }
  }

  return {
    formData,
    setFormData,
    resetForm
  }
})
