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
import { format, addDays, isValid, parse } from 'date-fns'

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
const minExPayDate = new Date();
const maxExPayDate = addDays(new Date(), 14)

// 주문 정보 필드 정의
const formFields = [
  { label: '주문코드', field: 'ordCd', type: 'text', disabled: true },
  { label: '주문일자', field: 'ordDt', type: 'text'  },
  { label: '거래처명', field: 'cpName', type: 'input', disabled: true },
  { label: '배송지주소', field: 'deliAdd', type: 'text' },
  { label: '납기요청일자', field: 'deliReqDt', type: 'calendar', readonly: true, minDate: minDeliReqDate },
  { label: '입금일자', field: 'exPayDt', type: 'calendar', minDate: minExPayDate, maxDate: maxExPayDate },
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

// 주문 상태 코드 정의
const STATUS_WAITING = 's1'      // 접수 대기
const STATUS_CONFIRMED = 's2'    // 접수 완료
const STATUS_RELEASED = 's3'     // 출고 완료
const STATUS_CANCELED = 's4'     // 주문 취소
const STATUS_RETURN_CANCELED = 's5' // 반품 취소
const STATUS_RETURN_COMPLETED = 's6' // 반품 완료

// 할인 계산 함수 (주문 수량에 따라 할인 적용)
const calculateDiscountedPrice = (basePrice, qty) => {
  if (qty >= 400) return basePrice - 100
  if (qty >= 200) return basePrice - 50
  if (qty >= 100) return basePrice - 20
  return basePrice
}

// 버튼 설정
const infoFormButtons = ref({
  reset: { show: true, label: '초기화', severity: 'secondary' },
  save: { show: true, label: '저장', severity: 'success' },
  delete: { show: true, label: '삭제', severity: 'danger' }
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

const handleReset = () => {
  // 유지해야 할 값 백업
  const baseInfo = {
    cpCd: formData.value.cpCd,
    cpName: formData.value.cpName,
    deliAdd: formData.value.deliAdd,
    regi: formData.value.regi
  }

  resetForm()
  resetProducts()

  // 유지할 값 다시 세팅
  formData.value.ordDt = format(new Date(), 'yyyy-MM-dd')
  formData.value.ordStatus = 's1'
  formData.value.ordTotalAmount = 0

  Object.assign(formData.value, baseInfo)

  console.log('초기화 후 formData:', formData.value)
}

// 날짜 변환 함수
const toDate = (value) => {
  if (value instanceof Date) return value
  if (typeof value === 'string') {
    const parsed = parse(value, 'yyyy-MM-dd', new Date())
    return isValid(parsed) ? parsed : null
  }
  return null
}

// 저장
const handleSave = async () => {
  try {
    const ordDt = toDate(formData.value.ordDt)
    const deliReqDt = toDate(formData.value.deliReqDt)
    const exPayDt = toDate(formData.value.exPayDt)

    if (!formData.value.cpCd) return alert('거래처 정보가 없습니다.')
    if (!isValid(deliReqDt)) return alert('납기 요청일자를 선택해주세요.')
    if (!isValid(exPayDt)) return alert('입금일자를 선택해주세요.')
    if (products.value.length === 0) return alert('제품을 1개 이상 등록해주세요.')

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
    formData.value.ordTotalAmount = allTotalAmount.value

    const requestBody = {
      ...raw,
      ordDt,
      deliReqDt,
      exPayDt,
      ordStatus: 's1',
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

// 상태 감지해서 버튼 조건 변경하는 watch
// 주문 상태가 '접수 대기'인 경우에만 버튼 활성화
watch(() => formData.value.ordStatus, (newStatus) => {
  if (!newStatus || newStatus === STATUS_WAITING) {
    infoFormButtons.value.save.show = true
    infoFormButtons.value.reset.show = true
    infoFormButtons.value.delete.show = true
  } else {
    infoFormButtons.value.save.show = false
    infoFormButtons.value.reset.show = false
    infoFormButtons.value.delete.show = false
  }
})

// 수량변경 시 단가 및 총 금액 자동 계산
// products 배열의 각 항목에서 totalQty와 unitPrice를 곱하여 총 금액을 계산
// 단가 계산은 주문 수량에 따라 할인 적용
watch(
  () => products.value.map(p => p.totalQty),
  () => {
    products.value.forEach((item) => {
      const qty = Number(item.totalQty)
      const base = Number(item.basePrice || item.unitPrice || 0)
      const newPrice = calculateDiscountedPrice(base, qty)
      const newAmount = qty * newPrice

      // 값이 실제로 바뀌는 경우에만 갱신
      if (item.unitPrice !== newPrice || item.totalAmount !== newAmount) {
        item.unitPrice = newPrice
        item.totalAmount = newAmount
      }
    })
  },
  { deep: true }
)



// 제품 모달 설정
const productModalConfig = ref({})

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

  // 초기 폼 데이터 설정
  setFormData({
    ordCd: '',
    ordDt: today,
    cpCd: hardcodedCompany.cpCd,
    cpName: hardcodedCompany.cpName,
    deliAdd: hardcodedCompany.deliAdd,
    deliReqDt: '',
    exPayDt: '',
    note: '',
    regi: hardcodedCompany.regi,
    ordTotalAmount: 0,
    ordStatus: 's1'
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
            basePrice: 'prodUnitPrice',
            unitPrice: (item) => calculateDiscountedPrice(item.prodUnitPrice, 1),
            totalQty: () => 1,
            totalAmount: (item) => calculateDiscountedPrice(item.prodUnitPrice, 1) * 1
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
        :modalDataSets="productModalConfig" :autoCalculation="{enabled: true, quantityField: 'totalQty', priceField: 'unitPrice', totalField: 'totalAmount' }"/>
        <!-- 하단 합계 영역 -->
        <div class="flex justify-end items-center mt-4 px-4">
          <p class="text-base font-semibold text-gray-700 mr-2 mb-0">총 주문 총액</p>
          <p class="text-xl font-bold text-orange-500">
            {{ allTotalAmount.toLocaleString() }} <em class="text-sm font-normal not-italic text-black ml-1">원</em>
          </p>
        </div>
    </div>
</template>
