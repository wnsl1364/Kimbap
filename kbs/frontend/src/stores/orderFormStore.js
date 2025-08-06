// stores/orderFormStore.js
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useOrderFormStore = defineStore('orderForm', () => {
  const formData = ref({
    ordCd: '',
    ordDt: '',
    cpCd: '',
    cpName: '',
    deliAdd: '',
    deliReqDt: '',
    exPayDt: '',
    note: '',
    regi: '',
    unsettledAmount: ''
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
      note: '',
      unsettledAmount: ''
    }
  }

  return {
    formData,
    setFormData,
    resetForm
  }
})
