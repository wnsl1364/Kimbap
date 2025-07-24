<script setup>
import { ref, onMounted, onUnmounted, computed, watch, watchEffect  } from 'vue'
import axios from 'axios'
import LeftAlignTable from '@/components/kimbap/table/LeftAlignTable.vue'
import InputTable from '@/components/kimbap/table/InputTable.vue';

// Pinia store
import { storeToRefs } from 'pinia'; // storeToRefs를 사용해야만 반응형이 유지됨
import { useOrderFormStore } from '@/stores/orderFormStore'
import { useOrderProductStore } from '@/stores/orderProductStore'
import { useMemberStore } from '@/stores/memberStore'

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
  { field: 'totalAmount', header: '총 금액(원)', type: 'input', align: 'right', readonly: true }
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
const allTotalAmount = computed(() => {
  return products.value.reduce((sum, item) => {
    const qty = Number(item.totalQty) || 0
    const price = Number(item.unitPrice) || 0
    return sum + qty * price
  }, 0)
});

// 제품 모달 설정
const productModalConfig = ref({})

// 초기화
const handleReset = () => {
  resetForm()
  formData.value.ordDt = format(new Date(), 'yyyy-MM-dd')
  console.log('초기화 후 주문일자:', formData.value.ordDt)
}

// 저장
const handleSave = async () => {
  try {
    if (!formData.value.cpCd) {
      alert('거래처 정보가 없습니다.')
      return
    }
    if (!formData.value.deliReqDt) {
      alert('납기 요청일자를 선택해주세요.')
      return
    }
    if (!formData.value.exPayDt) {
      alert('입금일자를 선택해주세요.')
      return
    }
    if (products.value.length === 0) {
      alert('제품을 1개 이상 등록해주세요.')
      return
    }

     for (let i = 0; i < products.value.length; i++) {
      const item = products.value[i]
      if (!item.pcode || !item.pName) {
        alert(`${i + 1}번 제품의 정보를 선택해주세요.`)
        return
      }
      if (!item.totalQty || Number(item.totalQty) <= 0) {
        alert(`${i + 1}번 제품의 수량을 1 이상 입력해주세요.`)
        return
      }
    }

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

watchEffect(() => {
  products.value.forEach(item => {
    const qty = Number(item.totalQty) || 0
    const price = Number(item.unitPrice) || 0
    item.totalAmount = qty * price
  })
})

onMounted(async () => {
  const today = format(new Date(), 'yyyy-MM-dd')

  // 하드코딩 테스트 데이터 (예: 드림마트 로그인 사용자)
  const hardcodedCompany = {
    cpCd: 'CP-010',
    cpName: '드림마트',
    deliAdd: '부산시 해운대구 10번지',
    loanTerm: 14,
    regi: 'MEM-001'
  }

  setFormData({
    ordCd: '',
    ordDt: today,
    cpCd: hardcodedCompany.cpCd,
    cpName: hardcodedCompany.cpName,
    deliAdd: hardcodedCompany.deliAdd,
    deliReqDt: '',
    exPayDt: '',
    note: '',
    regi: hardcodedCompany.regi
  })

  // 입금일자 최대값 세팅
  maxExPayDate.value = addDays(new Date(), hardcodedCompany.loanTerm)

  // 제품 목록(DB)
  try {
    const res = await axios.get('/api/product/list')
    if (res.data.result_code === 'SUCCESS') {
      const productList = res.data.data

      productModalConfig.value = {
        pName: {
          displayField: 'prod_name',
          items: productList,
          columns: [
            { field: 'pcode', header: '제품코드' },
            { field: 'prodName', header: '제품명' },
            { field: 'prodUnitPrice', header: '단가' }
          ],
          mappingFields: {
            pcode: 'pcode',
            pName: 'prodName',
            unitPrice: 'prodUnitPrice',
            totalQty: () => 1 // 선택 시 기본값으로 1 넣기 (선택사항)
          }
        }
      }
    } else {
      console.error('제품 목록 불러오기 실패:', res.data.message)
    }
  } catch (err) {
    console.error('제품 목록 요청 실패:', err)
  }
})

onUnmounted(() => {
  formStore.$reset();
  productStore.$reset();
});
</script>

<template>
    <div class="space-y-4">
        <!-- 기본정보 영역 -->
        <LeftAlignTable :data="formData" :fields="formFields" :title="'기본정보'" :buttons="infoFormButtons" button-position="top" @reset="handleReset" @save="handleSave"/>
    </div>
    <div class="space-y-4 mt-8">
        <!-- 제품추가 영역 -->
        <InputTable :data="products" :columns="columns" :title="'제품'" :buttons="purchaseFormButtons" button-position="top" scrollHeight="360px" height="460px" :dataKey="'pcode'"
        :modalDataSets="productModalConfig"/>
        <!-- 하단 합계 영역 -->
        <div class="flex justify-end items-center mt-4 px-4">
          <p class="text-base font-semibold text-gray-700 mr-2 mb-0">총 주문 총액</p>
          <p class="text-xl font-bold text-orange-500">
            {{ allTotalAmount.toLocaleString() }} <em class="text-sm font-normal not-italic text-black ml-1">원</em>
          </p>
        </div>
    </div>
</template>
