<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderDetail, registerReturn, cancelReturn, getLotList } from '@/api/return'
import LeftAlignTable from '@/components/kimbap/table/LeftAlignTable.vue'
import InputTable from '@/components/kimbap/table/InputTable.vue'
import { format, parseISO } from 'date-fns'
import { useToast } from 'primevue/usetoast';
import Toast from 'primevue/toast'

const toast = useToast();

const route = useRoute()
const router = useRouter()
const ordCd = route.params.ordCd

const formData = ref({})
const products = ref([])
const selectedRows = ref([])
const lotOptions = ref({}) // LOT 목록 저장용

const infoFormButtons = ref({
  save: { show: true, label: '반품요청', severity: 'info' },
  delete: { show: true, label: '반품취소', severity: 'danger' },
})

// 할인 계산 함수 (주문 수량에 따라 할인 적용)
const calculateDiscountedPrice = (basePrice, qty) => {

  if (qty >= 400) return basePrice - 1000
  if (qty >= 200) return basePrice - 500
  if (qty >= 100) return basePrice - 200
  return basePrice
}

const returnFormFields = [
  { label: '주문코드', field: 'ordCd', type: 'text', readonly: true },
  { label: '납기일자', field: 'deliReqDt', type: 'text', readonly: true },
  { label: '거래처', field: 'cpName', type: 'text', readonly: true },
]

const productColumns = [
  { field: 'prodName', header: '제품명', type: 'readonly' },
  { field: 'ordQty', header: '주문수량(BOX)', type: 'readonly', align: 'right' },
  { field: 'lotNo', header: 'LOT번호', type: 'select', options: row => lotOptions.value[row.ordDCd] || [], align: 'left' },
  { field: 'returnQty', header: '반품수량(BOX)', type: 'input', inputType: 'number', align: 'right' },
  { field: 'returnAmount', header: '반품금액', type: 'readonly', align: 'right', formatter: val => Number(val).toLocaleString() },
  { field: 'returnRea', header: '반품사유', type: 'input', align: 'left' },
]

// const handleSave = async () => {
//   if (selectedRows.value.length === 0) {
//     return alert('반품할 제품을 선택해주세요.');
//   }

//   const selected = selectedRows.value.filter(p => p.returnQty > 0 && p.returnRea && p.lotNo);
//   if (selected.length === 0) {
//     return alert('반품수량, 사유, LOT을 모두 입력해야 합니다.');
//   }

//   // 주문수량 초과 검증
//   for (const item of selected) {
//     if (item.returnQty > item.ordQty) {
//       return alert(`"${item.prodName}"의 반품수량은 주문수량(${item.ordQty} BOX)를 초과할 수 없습니다.`);
//     }
//   }

//   const payload = {
//     ordCd: formData.value.ordCd,
//     returnItems: selected.map(p => ({
//       ordDCd: p.ordDCd,
//       lotNo: p.lotNo,
//       returnQty: p.returnQty,
//       returnRea: p.returnRea,
//       returnAmount: p.returnAmount,
//       returnStatusCustomer: 'v1'
//     }))
//   }

//   try {
//     const res = await registerReturn(payload)
//     if (res.data.result_code === 'SUCCESS') {
//       alert('반품 요청이 등록되었습니다.')
//       router.push({ path: '/order/orderList', query: { refresh: true } });
//     } else {
//       alert('저장 실패: ' + res.data.message)
//     }
//   } catch (err) {
//     console.error('반품 저장 실패:', err)
//     alert('반품 저장 중 오류 발생')
//   }
// }

const handleSave = async () => {
  console.log('[handleSave] 현재 selectedRows:', selectedRows.value);
  console.log('[handleSave] 현재 products:', products.value);

  if (selectedRows.value.length === 0) {
    toast.add({
      severity: 'warn', // 'success', 'info', 'warn', 'error'
      summary: '입력 확인',
      detail: '반품할 제품을 선택해주세요.',
      life: 3000
    });
    return;
  }

  const selected = selectedRows.value.filter(p => p.returnQty > 0 && p.returnRea && p.lotNo);
  console.log('[handleSave] 조건 통과한 selected:', selected);

  if (selected.length === 0) {
    toast.add({
      severity: 'warn', // 'success', 'info', 'warn', 'error'
      summary: '입력 확인',
      detail: '반품수량, 사유, LOT을 모두 입력해야 합니다.',
      life: 3000
    });
    return;
  }

  for (const item of selected) {
    if (item.returnQty > item.ordQty) {
      toast.add({ 
        severity: 'warn', 
        summary: '반품 수량 초과', 
        detail: `"${item.prodName}"의 반품수량은 주문수량(${item.ordQty} BOX)를 초과할 수 없습니다.`, 
        life: 3000 
      });
      return;
    }
  }

  const payload = {
    ordCd: formData.value.ordCd,
    returnItems: selected.map(p => ({
      ordDCd: p.ordDCd,
      lotNo: p.lotNo,
      returnQty: p.returnQty,
      returnRea: p.returnRea,
      returnAmount: p.returnAmount,
      returnStatusCustomer: 'v1'
    }))
  };
  console.log('[handleSave] 전송 payload:', payload);

  try {
    const res = await registerReturn(payload);
    console.log('[handleSave] 서버 응답:', res.data);

    if (res.data.result_code === 'SUCCESS') {
      toast.add({
        severity: 'info', // 'success', 'info', 'warn', 'error'
        summary: '반품 요청 완료',
        detail: '반품 요청이 등록되었습니다.',
        life: 3000
      });
      router.push({ path: '/order/orderList', query: { refresh: true } });
    } else {
      toast.add({ 
        severity: 'warn', 
        summary: '저장 실패', 
        detail: '저장 실패: ' + res.data.message, 
        life: 3000 
      });
    }
  } catch (err) {
    console.error('반품 저장 실패:', err);
    toast.add({ 
        severity: 'warn', 
        summary: '저장 실패', 
        detail: '반품 저장 중 오류 발생', 
        life: 3000 
      });
  }
};


// 반품취소
const handleCancelReturn = async () => {
  const selected = selectedRows.value.filter(item => item.ordDStatus === 't4');

  if (selected.length === 0) {
    toast.add({ 
      severity: 'warn', 
      summary: '반품 요청 선택 오류', 
      detail: '반품요청 상태인 제품만 선택해주세요.', 
      life: 3000 
    });
    return;
  }

  const ordDCdList = selected.map(item => item.ordDCd);

  if (!confirm('선택한 반품 요청을 취소하시겠습니까?')) return;

  try {
    const res = await cancelReturn(ordDCdList);
    if (res.data.result_code === 'SUCCESS') {
      toast.add({ 
        severity: 'info', 
        summary: '반품 요청 취소', 
        detail: '반품 요청이 취소되었습니다.', 
        life: 3000 
      });
      router.push({ path: '/order/orderList', query: { refresh: true } });
    } else {
      toast.add({ 
        severity: 'warn', 
        summary: '반품 요청 취소 실패', 
        detail: '취소 실패: ' + res.data.message, 
        life: 3000 
      });
    }
  } catch (err) {
    console.error('반품 취소 실패:', err);
    toast.add({ 
      severity: 'warn', 
      summary: '반품 요청 취소 실패', 
      detail: '반품 취소 중 오류 발생', 
      life: 3000 
    });
  }
}

// 반품수량 변경 시 할인단가 적용 및 반품금액 계산
watch(products, (newVal) => {
  newVal.forEach(item => {
    if (item.returnQty > item.ordQty) {
      item.returnQty = item.ordQty;
      toast.add({ 
        severity: 'warn', 
        summary: '반품 수량 초과', 
        detail: `"${item.prodName}"의 반품수량은 주문수량(${item.ordQty} BOX)를 초과할 수 없습니다.`, 
        life: 3000 
      });
    }

    const qty = item.returnQty || 0;
    const basePrice = item.basePrice || 0;

    const discountedUnitPrice = calculateDiscountedPrice(basePrice, qty);
    item.unitPrice = discountedUnitPrice;
    item.returnAmount = qty * discountedUnitPrice;
  })
}, { deep: true });

// LOT 목록 불러오기
async function loadLotOptions(ordDCd) {
  try {
    const res = await getLotList(ordDCd)
    lotOptions.value[ordDCd] = res.data.data.map(lot => ({ label: lot, value: lot }))
  } catch (err) {
    console.error(`LOT 목록 조회 실패 (${ordDCd}):`, err)
  }
}

onMounted(async () => {
  try {
    const res = await getOrderDetail(ordCd)
    const data = res.data.data

    formData.value = {
      ordCd: data.ordCd,
      cpName: data.cpName,
      deliReqDt: format(parseISO(data.deliReqDt), 'yyyy-MM-dd')
    }

    products.value = data.orderDetails.map(item => ({
      ...item,
      basePrice: item.unitPrice,  // 주문 시의 단가를 basePrice로 저장
      returnQty: 0,
      returnRea: '',
      unitPrice: item.unitPrice,  // 초기 단가 설정 (수량 따라 변경될 예정)
      returnAmount: 0,
      lotNo: '' // LOT 선택 값
    }))

    // 모든 제품 LOT 목록 불러오기
    for (const p of products.value) {
      await loadLotOptions(p.ordDCd)
    }
  } catch (err) {
    console.error('주문 상세 조회 실패:', err)
  }
})
</script>

<template>
  <div class="space-y-4">
    <LeftAlignTable
      v-model:data="formData"
      :fields="returnFormFields"
      :title="'반품기본정보'"
      :buttons="infoFormButtons"
      button-position="top"
      @save="handleSave"
      @delete="handleCancelReturn"
      :dataKey="'ordCd'"
    />

    <InputTable
      v-model:data="products"
      v-model:selection="selectedRows"
      :columns="productColumns"
      :dataKey="'ordDCd'"
      :enableRowActions="false"
      :enableSelection="true"
      :selectionMode="'multiple'"
      scrollHeight="55vh"
      height="65vh"
      :title="'제품'"
      :buttons="{}"
    />
  </div>
</template>
