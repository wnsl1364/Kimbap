<script setup>
import { ref, onMounted, computed, watch} from 'vue'
import { getOrderList } from '@/api/order'
import axios from 'axios'
import { format, parseISO } from 'date-fns'
import { storeToRefs } from 'pinia';
import { useOrderFormStore } from '@/stores/orderFormStore'
import { useOrderProductStore } from '@/stores/orderProductStore'

import LeftAlignTable from '@/components/kimbap/table/LeftAlignTable.vue'
import InputTable from '@/components/kimbap/table/InputTable.vue'

// 스토어 인스턴스
const formStore = useOrderFormStore()
const productStore = useOrderProductStore()

// 반응형 상태
const { formData } = storeToRefs(formStore)
const { products } = storeToRefs(productStore)

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
        },
        emitEvent: 'load' 
      }
    }
  } catch (err) {
    console.error('주문 목록 로딩 실패:', err)
  }
}

const handleLoadOrder = async (selectedRow) => {
  try {
    const ordCd = selectedRow.ordCd
    
    const res = await axios.get(`/api/order/${ordCd}`)
    const order = res.data.data

    // 기본정보 세팅
    formStore.setFormData({
      ordCd: order.ordCd,
      ordDt: format(parseISO(order.ordDt), 'yyyy-MM-dd'),
      cpCd: order.cpCd,
      cpName: order.cpName,
      deliAdd: order.deliAdd,
      deliReqDt: format(parseISO(order.deliReqDt), 'yyyy-MM-dd'),
      exPayDt: format(parseISO(order.exPayDt), 'yyyy-MM-dd'),
      note: order.note,
      regi: order.regi
    })

    // 제품목록 세팅
    productStore.setProducts(
      order.products.map(item => ({
        ...item,
        deliAvailDt: item.deliAvailDt ? format(parseISO(item.deliAvailDt), 'yyyy-MM-dd') : ''
      }))
    )
  } catch (err) {
    console.error('주문 상세 불러오기 실패:', err)
  }
}


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
      @load="handleLoadOrder"
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
