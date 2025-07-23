<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
/*import axios from 'axios'*/
import LeftAlignTable from '@/components/kimbap/table/LeftAlignTable.vue'
import InputTable from '@/components/kimbap/table/InputTable.vue';

// Pinia store
import { storeToRefs } from 'pinia'; // storeToRefs를 사용해야만 반응형이 유지됨
import { useOrderFormStore } from '@/stores/orderFormStore'
import { useOrderProductStore } from '@/stores/orderProductStore' //피니아 스토어 가져오기

// 스토어 인스턴스
const formStore = useOrderFormStore()
const productStore = useOrderProductStore()

// 반응형 상태
const { formData } = storeToRefs(formStore)
const { products } = storeToRefs(productStore)

// store 메서드
const { setFormData, resetForm } = formStore
const { setProducts, resetProducts } = productStore

const formFields = [
    { label: '주문코드', field: 'ordCd', type: 'text', disabled: true },
    { label: '주문일자', field: 'ordDt', type: 'text'  },
    { label: '거래처명', field: 'cpName', type: 'input', disabled: true },
    { label: '배송지주소', field: 'deliAdd', type: 'text' },
    { label: '납기요청일자', field: 'deliReqDt', type: 'calendar', readonly: true },
    { label: '입금일자', field: 'exPayDt', type: 'calendar' },
    { label: '비고', field: 'note', type: 'text' }
]

const columns = [
  { field: 'pcode', header: '제품코드', type: 'input', readonly: true },
  { field: 'pName', header: '제품명', type: 'inputsearch', suffixIcon: 'pi pi-search', suffixEvent: 'openQtyModal', },
  { field: 'totalQty', header: '주문수량(box)', type: 'input', inputType: 'number', align: 'right' },
  { field: 'unitPrice', header: '단가(원)', type: 'input', align: 'right', readonly: true },
  { field: 'dueDate', header: '총 금액(원)', type: 'input', align: 'right', readonly: true }
]

const infoFormButtons = ref({
  reset: { show: true, label: '초기화', severity: 'secondary' },
  save: { show: true, label: '저장', severity: 'success' }
});

const purchaseFormButtons = ref({
  save: { show: false, label: '저장', severity: 'success' },
  reset: { show: false, label: '초기화', severity: 'secondary' },
  delete: { show: false, label: '삭제', severity: 'danger' },
  load: { show: false, label: '불러오기', severity: 'info' }
});

const totalAmount = computed(() => {
  return products.value.reduce((sum, item) => {
    const qty = Number(item.totalQty) || 0
    const price = Number(item.unitPrice) || 0
    return sum + qty * price
  }, 0)
});

onMounted(async () => {
    // 임시 데이터 예시
  formStore.setFormData({
    orderNo: '',
    orderDate: '',
    customerName: '',
    address: '',
    dueDate: '',
    paymentDate: '',
    memo: ''
  })
})

onUnmounted(() => {
  formStore.$reset();
  productStore.$reset();
});

/** 
  * db로 가져올때
onMounted(async () => {
    const res = await axios.get('/api/orders/1')   // 예시 URL
    formData.value = res.data // 받아온 데이터를 formData에 바로 넣음
})
*/
</script>

<template>
    <div class="space-y-4">
        <!-- 기본정보 영역 -->
        <LeftAlignTable :data="formData" :fields="formFields" :title="'기본정보'" :buttons="infoFormButtons" button-position="top"/>
    </div>
    <div class="space-y-4 mt-8">
        <!-- 제품추가 영역 -->
        <InputTable :data="products" :columns="columns" :title="'제품'" :buttons="purchaseFormButtons" 
      button-position="top" scrollHeight="360px" height="460px"/>
        <!-- 하단 합계 영역 -->
        <div class="flex justify-end items-center mt-4 px-4">
          <p class="text-base font-semibold text-gray-700 mr-2 mb-0">총 주문 총액</p>
          <p class="text-xl font-bold text-orange-500">
            {{ totalAmount.toLocaleString() }} <em class="text-sm font-normal not-italic text-black ml-1">원</em>
          </p>
        </div>
    </div>
</template>
