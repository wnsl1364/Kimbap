<script setup>
import { ref, onMounted, onUnmounted, computed, watch} from 'vue'
import { getOrderList } from '@/api/order'
import axios from 'axios'
import LeftAlignTable from '@/components/kimbap/table/LeftAlignTable.vue'
import InputTable from '@/components/kimbap/table/InputTable.vue'
import { format, parseISO } from 'date-fns'
import { storeToRefs } from 'pinia';
import { useOrderFormStore } from '@/stores/orderFormStore'
import { useOrderProductStore } from '@/stores/orderProductStore'
import { useRoute } from 'vue-router';

// 라우터 설정
const route = useRoute()
const ordCd = route.query.ordCd

// 스토어 인스턴스
const formStore = useOrderFormStore()
const productStore = useOrderProductStore()

// 반응형 상태
const { formData } = storeToRefs(formStore)
const { products } = storeToRefs(productStore)

const showArrearsModal = ref(false)

// form 필드
const formFields1 = [
  { label: '출고지시번호', field: 'ordCd', type: 'text', disabled: true },
  { label: '작성자', field: 'ordDt', type: 'text', disabled: true },
  { label: '지시일자', field: 'cpName', type: 'input', disabled: true },
  { label: '창고', field: 'note', type: 'text', readonly: true },
];
const formFields2 = [
  { label: '거래처명', field: 'cpName', type: 'input', disabled: true },
  { label: '거래처 담당자', field: 'deliAdd', type: 'text', disabled: true },
  { label: '납품지 주소', field: 'deliReqDt', type: 'text', disabled: true },
  { label: '비고', field: 'note', type: 'text', readonly: true },
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
  reset: { show: true, label: '저장', severity: 'info' },
  load: { show: true, label: '주문정보 불러오기', severity: 'success' },
  delete: { show: true, label: '이전', severity: 'danger' }
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
      order.orderDetails.map(item => {
        const qty = item.ordQty || 0
        const price = item.unitPrice || 0
        const total = qty * price

        console.log('✅ 제품별 ordDCd:', item.ordDCd, 'ordDStatus:', item.ordDStatus)

        return {
          ...item,
          totalAmount: total,
          deliAvailDt: item.deliAvailDt ? format(parseISO(item.deliAvailDt), 'yyyy-MM-dd') : '',
          ordDStatus: item.ordDStatus || 't1',
          ordDCd: item.ordDCd
        }
      })
    )
  } catch (err) {
    console.error('주문 상세 불러오기 실패:', err)
  }
}



// 주문 불러오기
onMounted(async () => {
  if (!ordCd) {
    await loadOrderListForModal();
  }

  // 자동 주문 불러오기
  if (ordCd) {
    await handleLoadOrder({ ordCd })
  }
})

// 피니아 리셋
onUnmounted(() => {
  formStore.$reset();
  productStore.$reset();
});
</script>

<template>
  <div class="space-y-4 mb-3">
    <LeftAlignTable
      v-model:data="formData"
      :fields="formFields1"
      :title="'출고 지시서'"
      :buttons="infoFormButtons"
      button-position="top"
      :modalDataSets="modalDataSets"
      :dataKey="'ordCd'"
      @showArrearsModal="showArrearsModal = true"
      @load="handleLoadOrder"
      @reset="handleApprove"
      @delete="handleReject"
    />
  </div>
    <div class="space-y-4">
    <LeftAlignTable
      v-model:data="formData"
      :fields="formFields2"
      :title="'출고처'"
      :buttons="false"
      button-position="top"
      :modalDataSets="modalDataSets"
      :dataKey="'ordCd'"
      @showArrearsModal="showArrearsModal = true"
      @load="handleLoadOrder"
      @reset="handleApprove"
      @delete="handleReject"
    />
  </div>

  <div class="space-y-4 mt-3">
    <InputTable
      :data="products"
      :columns="columns"
      :title="'제품'"
      scrollHeight="360px"
      height="460px"
      :dataKey="'pcode'"
      :buttons="purchaseFormButtons"
      :enableRowActions="false"
      :enableSelection="false"
    />
  </div>
  <!-- <div class="mt-4">
    <h2 class="text-lg mb-0 font-semibold">거절사유</h2>
    <input v-model="rejectReason" type="text" class="border rounded px-3 py-2 w-full " placeholder="거절 사유를 입력하세요" />
  </div> -->
</template>
