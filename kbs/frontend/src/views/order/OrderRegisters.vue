<script setup>
import { ref, onMounted, onUnmounted, computed, watch, watchEffect  } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
import LeftAlignTable from '@/components/kimbap/table/LeftAlignTable.vue'
import InputTable from '@/components/kimbap/table/InputTable.vue';

// 라우터 설정
const route = useRoute()

// Pinia store
import { storeToRefs } from 'pinia'; // storeToRefs를 사용해야만 반응형이 유지됨
import { useOrderFormStore } from '@/stores/orderFormStore'
import { useOrderProductStore } from '@/stores/orderProductStore'
import { useMemberStore } from '@/stores/memberStore'

// 날짜 포맷팅을 위한 date-fns
import { format, addDays, isValid, parse, parseISO } from 'date-fns'

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
  { field: 'ordQty', header: '주문수량(box)', type: 'input', inputType: 'number', align: 'right', min: 1, },
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

// 박스당 김밥 줄 수 (예: 40개)
const KBP_PER_BOX = 40

// 할인 계산 함수 (주문 수량에 따라 할인 적용)
const calculateDiscountedPrice = (basePrice, qty) => {
  const pricePerBox = basePrice * KBP_PER_BOX

  if (qty >= 400) return pricePerBox - 1000
  if (qty >= 200) return pricePerBox - 500
  if (qty >= 100) return pricePerBox - 200
  return pricePerBox
}

// 버튼 설정
const infoFormButtons = ref({
  reset: { show: true, label: '초기화', severity: 'secondary' },
  save: { show: true, label: '저장', severity: 'success' },
  delete: { show: false, label: '삭제', severity: 'danger' }
});

// 제품 추가 영역 버튼 설정
const purchaseFormButtons = ref({
  save: { show: false, label: '저장', severity: 'success' },
  reset: { show: false, label: '초기화', severity: 'secondary' },
  delete: { show: false, label: '삭제', severity: 'danger' },
  load: { show: false, label: '불러오기', severity: 'info' }
});

// 총 금액 계산
// products 배열의 각 항목에서 ordQty와 unitPrice를 곱하여 총 금액을 계산
const allTotalAmount = computed(() => {
  return products.value.reduce((sum, item) => {
    const qty = Number(item.ordQty) || 0
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

const formatDateFields = (obj, fields) => {
  fields.forEach(field => {
    if (obj[field]) {
      obj[field] = format(parseISO(obj[field]), 'yyyy-MM-dd')
    }
  })
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
      if (!item.ordQty || Number(item.ordQty) <= 0) {
        alert(`${i + 1}번 제품의 수량을 1 이상 입력해주세요.`)
        return
      }
    }

    const raw = formData.value
    formData.value.ordTotalAmount = allTotalAmount.value

    const requestBody = {
      ...raw,
      ordDt: format(ordDt, 'yyyy-MM-dd'),
      deliReqDt: format(deliReqDt, 'yyyy-MM-dd'),
      exPayDt: format(exPayDt, 'yyyy-MM-dd'),
      ordStatus: 's1',
      orderDetails: products.value.map(p => ({
        ordDCd: '', // 서버에서 생성하거나 프론트에서 getGeneratedOrderDetailCode로 요청
        ordCd: '',  // 마스터 등록 후 백에서 채움
        pcode: p.pcode,
        prodVerCd: p.prodVerCd || 'ver-250724-01', // 프론트에서 기본값 보완
        ordQty: p.ordQty,
        unitPrice: p.unitPrice,
        deliAvailDt: format(deliReqDt, 'yyyy-MM-dd'),
        ordDStatus: 't1',
        isUsed: 'f1'
      }))
    }

    console.log('서버에 보낼 데이터:', requestBody)
    console.log("orderDetails", requestBody.orderDetails)

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

const handleDelete = async () => {
  const confirmed = confirm('정말 삭제하시겠습니까?')
  if (!confirmed) return

  try {
    const res = await axios.put(`/api/order/${formData.value.ordCd}/deactivate`)  // 예: PUT 요청

    if (res.data.result_code === 'SUCCESS') {
      alert('주문이 정상적으로 삭제(비활성)되었습니다.')
      handleReset()
    } else {
      alert(`삭제 실패: ${res.data.message}`)
    }
  } catch (err) {
    console.error('삭제 오류:', err)
    alert('주문 삭제 중 오류가 발생했습니다.')
  }
}

const handleProductDeleteList = async (ordDCdList) => {
  for (const ordDCd of ordDCdList) {
    await axios.put(`/api/order-detail/${ordDCd}/deactivate`)
    // 로컬에서 products 배열에서 제거
    const idx = products.value.findIndex(p => p.ordDCd === ordDCd)
    if (idx !== -1) products.value.splice(idx, 1)
  }
}


// 상태 감지해서 버튼 조건 변경하는 watch
// 주문 상태가 신규(ordCd 없음) 또는 접수 대기(s1) 상태일 때 저장 및 초기화 버튼을 표시하고, 삭제 버튼은 접수 대기 상태일 때만 표시
// 주문 상태가 접수 완료(s2) 이상이면 저장 및 초기화 버튼을 숨기고, 삭제 버튼은 숨김
watch(() => formData.value.ordStatus, (newStatus) => {
  const isNewOrder = !formData.value.ordCd           // 주문코드가 없으면 신규
  const isWaiting = newStatus === STATUS_WAITING     // 접수 대기 상태인지 확인

  infoFormButtons.value.save.show = isNewOrder || isWaiting
  infoFormButtons.value.reset.show = isNewOrder || isWaiting
  infoFormButtons.value.delete.show = !isNewOrder && isWaiting
})


// 수량변경 시 단가 및 총 금액 자동 계산
// products 배열의 각 항목에서 ordQty와 unitPrice를 곱하여 총 금액을 계산
// 단가 계산은 주문 수량에 따라 할인 적용
watch(
  () => products.value.map(p => p.ordQty),
  () => {
    products.value.forEach((item) => {
      const qty = Number(item.ordQty)
      const base = Number(item.basePrice || item.unitPrice || 0)
      const newPrice = calculateDiscountedPrice(base, qty)
      const newAmount = qty > 0 ? qty * newPrice : 0  

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

  // 하드코딩 테스트 데이터
  const hardcodedCompany = {
    cpCd: 'CP-101',
    cpName: 'GS25강남점',
    deliAdd: '서울시 강남구 테헤란로 303',
    loanTerm: 15,
    regi: 'EMP-10005' //'MEM-001'
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
          displayField: 'prodName',
          items: productList,
          columns: [
            { field: 'pcode', header: '제품코드' },
            { field: 'prodName', header: '제품명' },
            { field: 'prodUnitPrice', header: '단가' }
          ],
          mappingFields: {
            pcode: 'pcode',
            pName: 'prodName',
            prodVerCd: 'prodVerCd',
            basePrice: 'prodUnitPrice',
            unitPrice: (item) => calculateDiscountedPrice(item.prodUnitPrice, 1),
            ordQty: () => 1,
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

onMounted(async () => {
  const ordCdFromQuery = route.query.ordCd

  console.log('ordCdFromQuery:', ordCdFromQuery)

  if (ordCdFromQuery) {
    try {
      const res = await axios.get(`/api/order/${ordCdFromQuery}`)

      if (res.data.result_code === 'SUCCESS') {
        const order = res.data.data

        // 날짜 포맷을 적용해야 하는 부분 추가!
        formatDateFields(order, ['ordDt', 'deliReqDt', 'exPayDt', 'regDt', 'actPayDt'])

        setFormData(order)           // 주문 기본 정보
        setProducts(order.orderDetails) // 주문 상세 목록
      } else {
        alert(`주문 정보를 불러오는 데 실패했습니다: ${res.data.message}`)
      }
    } catch (err) {
      console.error('주문 조회 오류:', err)
      alert('주문 정보를 불러오는 중 오류가 발생했습니다.')
    }
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
        <LeftAlignTable v-model:data="formData" :fields="formFields" :title="'기본정보'" :buttons="infoFormButtons" button-position="top" @reset="handleReset" @save="handleSave"  @delete="handleDelete"/>
    </div>
    <div class="space-y-4 mt-8">
        <!-- 제품추가 영역 -->
        <InputTable :data="products" :columns="columns" :title="'제품'" :buttons="purchaseFormButtons" button-position="top"
        scrollHeight="360px" height="460px" :dataKey="'pcode'" :deleteKey="'ordDCd'" :deleteEventName="'handleProductDeleteList'"
        @handleProductDeleteList="handleProductDeleteList"
        :modalDataSets="productModalConfig"
        :autoCalculation="{enabled: true, quantityField: 'ordQty', priceField: 'unitPrice', totalField: 'totalAmount' }"/>
        <!-- 하단 합계 영역 -->
        <div class="flex justify-end items-center mt-4 px-4">
          <p class="text-base font-semibold text-gray-700 mr-2 mb-0">총 주문 총액</p>
          <p class="text-xl font-bold text-orange-500">
            {{ allTotalAmount.toLocaleString() }} <em class="text-sm font-normal not-italic text-black ml-1">원</em>
          </p>
        </div>
    </div>
</template>
