<script setup>
import { ref, onMounted, onUnmounted, computed, watch} from 'vue'
import { getRelOrdModal, getRelOrdSelect } from '@/api/distribution'
import axios from 'axios'
import LeftAlignTable from '@/components/kimbap/table/LeftAlignTable.vue'
import InputTable from '@/components/kimbap/table/InputTable.vue'
import { format, parseISO } from 'date-fns'
import { storeToRefs } from 'pinia';
import { useOrderFormStore } from '@/stores/orderFormStore'
import { useOrderProductStore } from '@/stores/orderProductStore'
import { useRoute } from 'vue-router';

const today = format(new Date(), 'yyyy-MM-dd')

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
  { label: '출고지시번호', field: 'newRelOrdCd', type: 'text', disabled: true },
  { label: '작성자', field: '', type: 'text', disabled: true },
  { label: '지시일자', field: 'relDt', type: 'input', disabled: true },
  { label: '창고', field: '', type: 'text', readonly: true },
];
const formFields2 = [
  { label: '거래처명', field: 'cpName', type: 'input', disabled: true },
  { label: '거래처 담당자', field: 'mName', type: 'text', disabled: true },
  { label: '납품지 주소', field: 'deliAdd', type: 'text', disabled: true },
  { label: '납기요청일', field: 'deliReqDt', type: 'text', disabled: true },
]

// 제품 테이블
const columns = [
  { field: 'prodName', header: '제품명', type: 'input', readonly: true },
  { field: 'ordQty', header: '주문수량(개)', type: 'input', inputType: 'number', align: 'right', readonly: true },
  { field: 'noRelQty', header: '잔여수량(개)', type: 'input', inputType: 'number', align: 'right', readonly: true },
  { field: 'relQty', header: '출고지시수량(개)', type: 'input', inputType: 'number', align: 'right', },
  { field: 'relOrdStatus', header: '출고상태', type: 'input', readonly: true }
]

// 버튼 설정
const infoFormButtons = ref({
  save: { show: true, label: '저장', severity: 'info' },
  load: { show: true, label: '주문정보 불러오기', severity: 'success' },
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
    const res = await getRelOrdModal({}) // ✅ 파라미터가 있으면 추가

    const items = res.data.map(order => ({
      ordCd: order.ordCd,
      cpName: order.cpName,
      ordDt: format(parseISO(order.ordDt), 'yyyy-MM-dd'),
      prodName: order.prodNameSummary  // ✅ 필드명 주의!
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
    console.error('출고지시 모달 주문 목록 로딩 실패:', err)
  }
}

const handleLoadOrder = async (selectedRow) => {
  console.log('🟢 모달에서 선택된 row:', selectedRow);
  try {
    const ordCd = selectedRow.ordCd;

    // 1. 주문 상세 정보 (기존대로 불러오기)
    const orderRes = await axios.get(`/api/order/${ordCd}`);
    const order = orderRes.data.data;

    // 2. 출고지시용 제품 리스트
    const prodRes = await getRelOrdSelect(ordCd);
    const productList = prodRes.data;

    // 담당자명, 거래처명은 productList[0]에서 바로 꺼내기
    const mName = productList[0]?.mname || '';
    const cpName = productList[0]?.mcpName || '';
    const newRelOrdCd = productList[0]?.newRelOrdCd || '';

    formStore.setFormData({
      ordCd: order.ordCd,
      ordDt: format(parseISO(order.ordDt), 'yyyy-MM-dd'),
      cpCd: order.cpCd,
      cpName: cpName,         
      deliAdd: order.deliAdd,
      deliReqDt: format(parseISO(order.deliReqDt), 'yyyy-MM-dd'),
      exPayDt: format(parseISO(order.exPayDt), 'yyyy-MM-dd'),
      note: order.note,
      mName: mName,           
      regi: order.regi,
      relDt: today,
      newRelOrdCd: newRelOrdCd,
    });

    productStore.setProducts(productList);
    console.log('✅ 출고지시 제품 리스트:', productList)
  } catch (err) {
    console.error('출고지시 주문 데이터 로딩 실패:', err);
  }
};




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
      :title="''"
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
