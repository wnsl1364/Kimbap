<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import axios from 'axios'
import LeftAlignTable from '@/components/kimbap/table/LeftAlignTable.vue'
import InputTable from '@/components/kimbap/table/InputTable.vue';

// Pinia store
import { storeToRefs } from 'pinia'; // storeToRefs를 사용해야만 반응형이 유지됨
import { useOrderFormStore } from '@/stores/orderFormStore'
import { useOrderProductStore } from '@/stores/orderProductStore' //피니아 스토어 가져오기

// 날짜 포맷팅을 위한 date-fns
import { format } from 'date-fns'
import { addDays } from 'date-fns'
import { parse } from 'date-fns'

// 스토어 인스턴스
const formStore = useOrderFormStore()
const productStore = useOrderProductStore()

// 반응형 상태
const { formData } = storeToRefs(formStore)
const { products } = storeToRefs(productStore)

// store 메서드
const { setFormData, resetForm } = formStore
const { setProducts, resetProducts } = productStore

// 최소 납기 요청일은 오늘 날짜 + 1일(내일)
const minDeliReqDate = addDays(new Date(), 1)

// 최대 입금일자는 오늘 날짜 + 14일(여신기간 임시 설정: 거래처마다 여신기간이 다름 수정해야함)
const maxExPayDate = addDays(new Date(), 14)

// 주문 정보 필드 정의
const formFields = [
    { label: '주문코드', field: 'ordCd', type: 'text', disabled: true },
    { label: '주문일자', field: 'ordDt', type: 'text'  },
    { label: '거래처명', field: 'cpName', type: 'input', disabled: true },
    { label: '배송지주소', field: 'deliAdd', type: 'text' },
    { label: '납기요청일자', field: 'deliReqDt', type: 'calendar', readonly: true, minDate: minDeliReqDate },
    { label: '입금일자', field: 'exPayDt', type: 'calendar', maxDate: maxExPayDate },
    { label: '비고', field: 'note', type: 'text' }
]

// 제품 정보 테이블 컬럼 정의
const columns = [
  { field: 'pcode', header: '제품코드', type: 'input', readonly: true },
  { field: 'pName', header: '제품명', type: 'inputsearch', suffixIcon: 'pi pi-search', suffixEvent: 'openQtyModal', },
  { field: 'totalQty', header: '주문수량(box)', type: 'input', inputType: 'number', align: 'right' },
  { field: 'unitPrice', header: '단가(원)', type: 'input', align: 'right', readonly: true },
  { field: 'dueDate', header: '총 금액(원)', type: 'input', align: 'right', readonly: true }
]

// 버튼 설정
const infoFormButtons = ref({
  reset: { show: true, label: '초기화', severity: 'secondary' },
  save: { show: true, label: '저장', severity: 'success' }
});

// 제품 추가 영역 버튼 설정
const purchaseFormButtons = ref({
  save: { show: false, label: '저장', severity: 'success' },
  reset: { show: false, label: '초기화', severity: 'secondary' },
  delete: { show: false, label: '삭제', severity: 'danger' },
  load: { show: false, label: '불러오기', severity: 'info' }
});

// 총 금액 계산
// products 배열의 각 항목에서 totalQty와 unitPrice를 곱하여 총 금액을 계산
const totalAmount = computed(() => {
  return products.value.reduce((sum, item) => {
    const qty = Number(item.totalQty) || 0
    const price = Number(item.unitPrice) || 0
    return sum + qty * price
  }, 0)
});

// 초기화
const handleReset = () => {
  resetForm()
  formData.value.ordDt = format(new Date(), 'yyyy-MM-dd')
  console.log('초기화 후 주문일자:', formData.value.ordDt)
}

// 저장
const handleSave = async () => {
  try {
    const raw = formData.value

    const requestBody = {
      ...raw,
      ordDt: typeof raw.ordDt === 'string' ? parse(raw.ordDt, 'yyyy-MM-dd', new Date()) : raw.ordDt,
      deliReqDt: typeof raw.deliReqDt === 'string' ? parse(raw.deliReqDt, 'yyyy-MM-dd', new Date()) : raw.deliReqDt,
      exPayDt: typeof raw.exPayDt === 'string' ? parse(raw.exPayDt, 'yyyy-MM-dd', new Date()) : raw.exPayDt,
      orderDetails: products.value
    }

    console.log('서버에 보낼 데이터:', requestBody)

    const res = await axios.post('/api/order/register', requestBody)

    if (res.data.result_code === 'SUCCESS') {
      const createdOrder = res.data.data
      alert(`주문이 성공적으로 등록되었습니다! \n주문번호: ${createdOrder.ordCd}`)
      handleReset()
    } else {
      alert(`등록 실패: ${res.data.message}`)
    }

  } catch (err) {
    console.error('주문 등록 오류:', err)
    alert('주문 등록 중 오류가 발생했습니다.')
  }
}


onMounted(async () => {
  // 오늘 날짜를 yyyy-MM-dd 형식으로 설정
  const today = format(new Date(), 'yyyy-MM-dd')
    // 임시 데이터 예시
  formStore.setFormData({
    ordCd: '',
    ordDt: today,
    cpName: '',
    deliAdd: '',
    deliReqDt: '',
    exPayDt: '',
    note: ''
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
        <LeftAlignTable :data="formData" :fields="formFields" :title="'기본정보'" :buttons="infoFormButtons" button-position="top" @reset="handleReset" @save="handleSave"/>
    </div>
    <div class="space-y-4 mt-8">
        <!-- 제품추가 영역 -->
        <InputTable :data="products" :columns="columns" :title="'제품'" :buttons="purchaseFormButtons" button-position="top" scrollHeight="360px" height="460px" :dataKey="'pcode'"/>
        <!-- 하단 합계 영역 -->
        <div class="flex justify-end items-center mt-4 px-4">
          <p class="text-base font-semibold text-gray-700 mr-2 mb-0">총 주문 총액</p>
          <p class="text-xl font-bold text-orange-500">
            {{ totalAmount.toLocaleString() }} <em class="text-sm font-normal not-italic text-black ml-1">원</em>
          </p>
        </div>
    </div>
</template>
