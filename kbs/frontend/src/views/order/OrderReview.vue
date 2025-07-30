<script setup>
import { ref, onMounted, computed, watch} from 'vue'
import { useRoute } from 'vue-router'
import { getOrderList } from '@/api/order'
import axios from 'axios'
import { format, parseISO } from 'date-fns'

import LeftAlignTable from '@/components/kimbap/table/LeftAlignTable.vue'
import InputTable from '@/components/kimbap/table/InputTable.vue'

const route = useRoute()

// 상태
const formData = ref({})
const products = ref([])

// 미수금
const arrears = ref(0)
const showArrearsModal = ref(false)

// 반려 사유
const rejectReason = ref('')

// form 필드
const formFields = [
  { label: '주문코드', field: 'ordCd', type: 'text', disabled: true },
  { label: '주문일자', field: 'ordDt', type: 'text', readonly: true },
  { label: '거래처명', field: 'cpName', type: 'input', disabled: true },
  { label: '배송지주소', field: 'deliAdd', type: 'text', readonly: true },
  { label: '납기요청일자', field: 'deliReqDt', type: 'text', readonly: true },
  { label: '입금예정일자', field: 'exPayDt', type: 'text', readonly: true },
  { label: '비고', field: 'note', type: 'text', readonly: true },
  { label: '미수금', field: 'arrears', type: 'text', readonly: true, suffixButton: '미수 상세보기',
  suffixEvent: 'showArrearsModal' }
]

// 제품 테이블
const columns = [
  { field: 'pcode', header: '제품코드', type: 'input', readonly: true },
  { field: 'prodName', header: '제품명', type: 'input', readonly: true },
  { field: 'ordQty', header: '주문수량(box)', type: 'input', inputType: 'number', align: 'right', readonly: true },
  { field: 'unitPrice', header: '단가(원)', type: 'input', align: 'right', readonly: true },
  { field: 'totalAmount', header: '총 금액(원)', type: 'input', align: 'right', readonly: true },
  { field: 'deliAvailDt', header: '납기가능일자', type: 'calendar' }
]

// 버튼 설정
const infoFormButtons = ref({
  reset: { show: true, label: '승인', severity: 'info' },
  load: { show: true, label: '주문정보 불러오기', severity: 'success' },
  delete: { show: true, label: '반려', severity: 'danger' }
});

// 제품 추가 영역 버튼 설정
const purchaseFormButtons = ref({
  save: { show: false, label: '저장', severity: 'success' },
  reset: { show: false, label: '초기화', severity: 'secondary' },
  delete: { show: false, label: '삭제', severity: 'danger' },
  load: { show: false, label: '불러오기', severity: 'info' }
});

// 모달 데이터셋
const modalDataSets = ref({})

const loadOrderListForModal = async () => {
  try {
    const res = await getOrderList()

    const items = res.data.data.map(order => ({
      ordCd: order.ordCd,
      cpName: order.cpName,
      ordDt: format(parseISO(order.ordDt), 'yyyy-MM-dd'),
      prodName: order.prodName  // 백에서 가공된 데이터 그대로 사용!
    }))

    modalDataSets.value = {
      load: {
        items,
        columns: [
          { field: 'ordCd', header: '주문코드' },
          { field: 'prodName', header: '제품명' },
          { field: 'cpName', header: '거래처명' },
          { field: 'ordDt', header: '주문일자' }
        ],
        mappingFields: {
          ordCd: 'ordCd',
          prodName: 'prodName',
          cpName: 'cpName',
          ordDt: 'ordDt'
        }
      }
    }
  } catch (err) {
    console.error('주문 목록 로딩 실패:', err)
  }
}


// 모달로 선택된 주문코드 감지 → 상세 조회
watch(
  () => formData.value.ordCd,
  async (newOrdCd) => {
    if (!newOrdCd || products.value.length > 0) return
    try {
      const res = await axios.get(`/api/order/${newOrdCd}`)
      if (res.data.result_code === 'SUCCESS') {
        const order = res.data.data
        formatDateFields(order, ['ordDt', 'deliReqDt', 'exPayDt', 'regDt', 'actPayDt'])
        order.orderDetails.forEach(p => {
          p.basePrice = p.unitPrice / 40
        })
        Object.assign(formData.value, order)
        products.value = order.orderDetails
      }
    } catch (err) {
      console.error('주문 조회 실패:', err)
    }
  }
)

// 날짜 포맷
const formatDateFields = (obj, fields) => {
  fields.forEach(field => {
    if (obj[field]) {
      obj[field] = format(parseISO(obj[field]), 'yyyy-MM-dd')
    }
  })
}

// 주문 불러오기
onMounted(async () => {
  await loadOrderListForModal()

  const ordCdFromQuery = route.query.ordCd
  if (!ordCdFromQuery) return
  try {
    const res = await axios.get(`/api/order/${ordCdFromQuery}`)
    if (res.data.result_code === 'SUCCESS') {
      const order = res.data.data
      formatDateFields(order, ['ordDt', 'deliReqDt', 'exPayDt', 'regDt', 'actPayDt'])
      order.orderDetails.forEach(p => {
        p.basePrice = p.unitPrice / 40
      })
      formData.value = order
      products.value = order.orderDetails
    } else {
      alert(`주문 정보를 불러오는 데 실패했습니다: ${res.data.message}`)
    }
  } catch (err) {
    console.error('주문 조회 오류:', err)
    alert('주문 정보를 불러오는 중 오류가 발생했습니다.')
  }
})
</script>

<template>
  <div class="space-y-4">
    <LeftAlignTable
      v-model:data="formData"
      :fields="formFields"
      :title="'기본정보'"
      :buttons="infoFormButtons"
      button-position="top"
      :modalDataSets="modalDataSets"
      :dataKey="'ordCd'"
      @showArrearsModal="showArrearsModal = true"
    />
  </div>

  <div class="space-y-4 mt-8">
    <InputTable
      :data="products"
      :columns="columns"
      :title="'제품'"
      scrollHeight="300px"
      height="400px"
      :dataKey="'pcode'"
      :buttons="purchaseFormButtons"
      :enableRowActions="false"
      :enableSelection="false"
    />
  </div>
  <div class="mt-4">
    <h2 class="text-lg mb-0 font-semibold">반려사유</h2>
    <input v-model="rejectReason" type="text" class="border rounded px-3 py-2 w-full " placeholder="반려 사유를 입력하세요" />
  </div>
</template>
