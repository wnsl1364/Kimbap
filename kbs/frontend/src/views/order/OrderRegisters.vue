<script setup>
import { ref, onMounted, onUnmounted, computed, watch} from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import LeftAlignTable from '@/components/kimbap/table/LeftAlignTable.vue'
import InputTable from '@/components/kimbap/table/InputTable.vue';
// Pinia store
import { storeToRefs } from 'pinia'; // storeToRefs를 사용해야만 반응형이 유지됨
import { useOrderFormStore } from '@/stores/orderFormStore'
import { useOrderProductStore } from '@/stores/orderProductStore'
import { useMemberStore } from '@/stores/memberStore'
import { getOrderList } from '@/api/order'

// 날짜 포맷팅을 위한 date-fns
import { format, addDays, isValid, parse, parseISO } from 'date-fns'

// 라우터 설정
const route = useRoute()
const router = useRouter()
const ordCd = route.query.ordCd

// 스토어 인스턴스
const formStore = useOrderFormStore()
const productStore = useOrderProductStore()
const memberStore = useMemberStore()

// 반응형 상태
const { formData } = storeToRefs(formStore)
const { products } = storeToRefs(productStore)
const { user } = storeToRefs(memberStore)

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
  { label: '주문일자', field: 'ordDt', type: 'text', readonly: true },
  { label: '거래처명', field: 'cpName', type: 'input', disabled: true },
  { label: '배송지주소', field: 'deliAdd', type: 'text' },
  { label: '납기요청일자', field: 'deliReqDt', type: 'calendar', readonly: true, minDate: minDeliReqDate },
  { label: '입금일자', field: 'exPayDt', type: 'calendar', minDate: minExPayDate, maxDate: maxExPayDate },
  { label: '비고', field: 'note', type: 'text' }
]

// 제품 정보 테이블 컬럼 정의
const columns = [
  { field: 'pcode', header: '제품코드', type: 'input', readonly: true },
  { field: 'prodName', header: '제품명', type: 'inputsearch', suffixIcon: 'pi pi-search', suffixEvent: 'openQtyModal', },
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
  save: { show: true, label: '저장', severity: 'info' },
  delete: { show: false, label: '삭제', severity: 'danger' },
  load: { show: true, label: '주문정보 불러오기', severity: 'success' },
});

// 제품 추가 영역 버튼 설정
const purchaseFormButtons = ref({
  save: { show: false, label: '저장', severity: 'success' },
  reset: { show: false, label: '초기화', severity: 'secondary' },
  delete: { show: false, label: '삭제', severity: 'danger' },
  load: { show: false, label: '불러오기', severity: 'info' }
});

// 행 추가 버튼 비활성화
const isProductAddDisabled = computed(() => {
  const isNewOrder = !formData.value.ordCd
  const isWaiting = formData.value.ordStatusCustomer === STATUS_WAITING
  return !(isNewOrder || isWaiting)
})


// 총 금액 계산
// products 배열의 각 항목에서 ordQty와 unitPrice를 곱하여 총 금액을 계산
const allTotalAmount = computed(() => {
  return products.value.reduce((sum, item) => {
    const qty = Number(item.ordQty) || 0
    const price = Number(item.unitPrice) || 0
    return sum + qty * price
  }, 0)
});

const deletedOrdDCdList = ref([])

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
  deletedOrdDCdList.value = []

  // 유지할 값 다시 세팅
  formData.value.ordDt = format(new Date(), 'yyyy-MM-dd')
  formData.value.ordStatusCustomer = 's1'
  formData.value.ordStatusInternal = 'a1'
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
      if (!item.pcode || !item.prodName) {
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
      ordStatusCustomer: 's1',     // 매출업체 상태: 접수대기
      ordStatusInternal: 'a1',     // 내부 상태: 요청
      orderDetails: products.value.map(p => ({
        ordDCd: p.ordDCd || '', // 신규 등록 시 빈 문자열로 설정
        ordCd: formData.value.ordCd || '',  // 마스터 등록 후 백에서 채움
        pcode: p.pcode,
        prodVerCd: p.prodVerCd || 'ver-250724-01',
        ordQty: p.ordQty,
        unitPrice: p.unitPrice,
        deliAvailDt: format(deliReqDt, 'yyyy-MM-dd'),
        ordDStatus: 't1',
        isUsed: 'f1'
      })),
      deletedOrdDCdList: deletedOrdDCdList.value
    }

    console.log('서버에 보낼 데이터:', requestBody)
    console.log("orderDetails", requestBody.orderDetails)

    const isUpdate = !!formData.value.ordCd
    const url = isUpdate
      ? `/api/order/${formData.value.ordCd}/update`
      : '/api/order/register'

    const res = isUpdate
      ? await axios.put(url, requestBody)
      : await axios.post(url, requestBody)

    if (res.data.result_code === 'SUCCESS') {
      const createdOrder = res.data.data
      alert(`주문이 ${isUpdate ? '수정' : '등록'}되었습니다! \n주문번호: ${createdOrder.ordCd}`)
      handleReset()
      router.push('/order/orderList')
    } else {
      alert(`${isUpdate ? '수정' : '등록'} 실패: ${res.data.message}`)
    }
  } catch (err) {
    console.error('주문 저장 오류:', err)
    alert('주문 저장 중 오류가 발생했습니다.')
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
      router.push('/order/orderList')
    } else {
      alert(`삭제 실패: ${res.data.message}`)
    }
  } catch (err) {
    console.error('삭제 오류:', err)
    alert('주문 삭제 중 오류가 발생했습니다.')
  }
}


// 제품 삭제 이벤트 핸들러
const handleProductDeleteList = async (ordDCdList) => {
  for (const ordDCd of ordDCdList) {
    await axios.put(`/api/order-detail/${ordDCd}/deactivate`)

    const idx = products.value.findIndex(p => p.ordDCd === ordDCd)
    if (idx !== -1) products.value.splice(idx, 1)

    // 삭제된 코드 따로 저장
    deletedOrdDCdList.value.push(ordDCd)
  }
}

// ordCd가 없으면 등록 모드 → 저장/초기화 표시, 삭제 숨김
// ordCd 있고 ordStatusCustomer === 's1'이면 → 수정 모드에서 삭제 버튼 활성화
// 다른 상태(s2 이상)이면 → 삭제/초기화 숨김
const updateInfoFormButtons = () => {
  const ordCd = formData.value.ordCd
  const ordStatus = formData.value.ordStatusCustomer
  const isNewOrder = !ordCd
  const isWaiting = ordStatus === STATUS_WAITING

  // 핵심 포인트! 새 객체로 완전히 재할당해야 LeftAlignTable에서 감지함
  infoFormButtons.value = {
    reset: { show: isNewOrder, label: '초기화', severity: 'secondary' },
    save: { show: isNewOrder || isWaiting, label: '저장', severity: 'info' },
    delete: { show: !isNewOrder && isWaiting, label: '삭제', severity: 'danger' },
    load: { show: isNewOrder, label: '주문정보 불러오기', severity: 'success' }
  }
}

watch(
  () => [formData.value.ordCd, formData.value.ordStatusCustomer],
  updateInfoFormButtons,
  { immediate: true }
)


// 수량변경 시 단가 및 총 금액 자동 계산
// products 배열의 각 항목에서 ordQty와 unitPrice를 곱하여 총 금액을 계산
// 단가 계산은 주문 수량에 따라 할인 적용
watch(
  () => products.value.map(p => p.ordQty),
  () => {
    products.value.forEach((item) => {
      const qty = Number(item.ordQty)
      const base = Number(item.basePrice || 0)
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

// 주문정보 불러오기
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
      regi: order.regi,
      unsettledAmount: order.unsettledAmount,
      ordStatusCustomer: order.ordStatusCustomer,
      ordStatusInternal: order.ordStatusInternal 
    })

    // 제품목록 세팅
    productStore.setProducts(
      order.orderDetails.map(item => {
        const qty = item.ordQty || 0
        const price = item.unitPrice || 0
        const total = qty * price

        console.log('제품별 ordDCd:', item.ordDCd, 'ordDStatus:', item.ordDStatus)

        return {
          ...item,
          totalAmount: total,
          deliAvailDt: item.deliAvailDt ? format(parseISO(item.deliAvailDt), 'yyyy-MM-dd') : '',
          ordDStatus: item.ordDStatus || 't1',
          ordDCd: item.ordDCd
        }
      })
    )

    // 버튼 상태 갱신 강제 호출 (여기 추가!)
    formData.value = { ...formData.value }
    updateInfoFormButtons()
  } catch (err) {
    console.error('주문 상세 불러오기 실패:', err)
  }
}

onMounted(async () => {
  // 제품 목록(DB)
  try {
    const res = await axios.get('/api/product/list')
    if (res.data.result_code === 'SUCCESS') {
      const productList = res.data.data

      productModalConfig.value = {
        prodName: {
          displayField: 'prodName',
          items: productList,
          columns: [
            { field: 'pcode', header: '제품코드' },
            { field: 'prodName', header: '제품명' },
            { field: 'prodUnitPrice', header: '단가' }
          ],
          mappingFields: {
            pcode: 'pcode',
            prodName: 'prodName',
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

onMounted(() => {
  watch(
    () => user?.value,
    (val) => {
      if (!val) return

      const today = format(new Date(), 'yyyy-MM-dd')

      // 로그인한 사용자 정보에서 cpCd, cpName, deliAdd, loanTerm 가져오기
      const companyInfo = {
        cpCd: val.cpCd || '',
        cpName: val.cpName || '',
        deliAdd: val.address || '',
        loanTerm: val.loanTerm || 14,
        regi: val.empCd || val.memCd || ''
      }

      // 초기 폼 데이터 설정
      setFormData({
        ordCd: '',
        ordDt: today,
        cpCd: companyInfo.cpCd,
        cpName: companyInfo.cpName,
        deliAdd: companyInfo.deliAdd,
        deliReqDt: '',
        exPayDt: '',
        note: '',
        regi: companyInfo.regi,
        ordTotalAmount: 0,
        ordStatusCustomer: 's1',      // 매출업체 상태 초기값
        ordStatusInternal: 'a1'       // 내부 상태 초기값
      })

      // 입금일자 최대값 세팅
      maxExPayDate.value = addDays(new Date(), companyInfo.loanTerm)
    },
    { immediate: true }
  )
})

onMounted(async () => {
  const ordCdFromQuery = route.query.ordCd

  if (ordCdFromQuery) {
    try {
      console.log('ordCdFromQuery:', ordCdFromQuery)
      const res = await axios.get(`/api/order/${ordCdFromQuery}`)

      if (res.data.result_code === 'SUCCESS') {
        const order = res.data.data

        // 날짜 포맷을 적용해야 하는 부분 추가!
        formatDateFields(order, ['ordDt', 'deliReqDt', 'exPayDt', 'regDt', 'actPayDt'])

        order.orderDetails.forEach(p => {
          p.basePrice = p.unitPrice / 40
        })

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
        <LeftAlignTable v-if="infoFormButtons" v-model:data="formData" :fields="formFields" :title="'기본정보'" :buttons="infoFormButtons" button-position="top" @reset="handleReset" @save="handleSave"  @delete="handleDelete" @load="handleLoadOrder" :modalDataSets="modalDataSets" :dataKey="'ordCd'"/>
    </div>
    <div class="space-y-4 mt-8">
        <!-- 제품추가 영역 -->
        <InputTable :data="products" :columns="columns" :title="'제품'" :buttons="purchaseFormButtons" button-position="top"
        scrollHeight="360px" height="460px" :dataKey="'pcode'" :deleteKey="'ordDCd'" :deleteEventName="'handleProductDeleteList'"
        @handleProductDeleteList="handleProductDeleteList"
        :modalDataSets="productModalConfig"
        :autoCalculation="{enabled: true, quantityField: 'ordQty', priceField: 'unitPrice', totalField: 'totalAmount' }"
        :enableRowActions="!isProductAddDisabled" :enableSelection="!isProductAddDisabled"/>
        <!-- 하단 합계 영역 -->
        <div class="flex justify-end items-center mt-4 px-4">
          <p class="text-base font-semibold text-gray-700 mr-2 mb-0">총 주문 총액</p>
          <p class="text-xl font-bold text-orange-500">
            {{ allTotalAmount.toLocaleString() }} <em class="text-sm font-normal not-italic text-black ml-1">원</em>
          </p>
        </div>
    </div>
</template>
