<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderDetail, registerReturn } from '@/api/return'
import LeftAlignTable from '@/components/kimbap/table/LeftAlignTable.vue'
import InputTable from '@/components/kimbap/table/InputTable.vue'
import { format, parseISO } from 'date-fns'

const route = useRoute()
const router = useRouter()
const ordCd = route.params.ordCd

const formData = ref({})
const products = ref([])
const selectedRows = ref([])

const infoFormButtons = ref({
  save: { show: true, label: '요청', severity: 'info' },
  delete: { show: false, label: '삭제', severity: 'danger' },
})

const returnFormFields = [
  { label: '반품요청코드', field: 'returnCd', type: 'text', disabled: true, defaultValue: '자동생성' },
  { label: '주문코드', field: 'ordCd', type: 'text', readonly: true },
  { label: '납기일자', field: 'deliReqDt', type: 'text', readonly: true },
  { label: '거래처', field: 'cpName', type: 'text', readonly: true },
]

const productColumns = [
  { field: 'prodName', header: '제품명', type: 'readonly' },
  { field: 'ordQty', header: '주문수량(BOX)', type: 'readonly', align: 'right' },
  { field: 'returnQty', header: '반품수량(BOX)', type: 'input', inputType: 'number', align: 'right' },
  { field: 'returnRea', header: '반품사유', type: 'input', align: 'left' },
]

const handleSave = async () => {
  const selected = selectedRows.value.filter(p => p.returnQty > 0 && p.returnRea)
  if (selected.length === 0) {
    return alert('반품수량과 사유를 입력한 제품을 선택해주세요.')
  }

  const payload = {
    ordCd: formData.value.ordCd,
    returnItems: selected.map(p => ({
      ordDCd: p.ordDCd,
      returnQty: p.returnQty,
      returnRea: p.returnRea
    }))
  }

  try {
    const res = await registerReturn(payload)
    if (res.data.result_code === 'SUCCESS') {
      alert('반품 요청이 등록되었습니다.')
      router.push('/order/orderList')
    } else {
      alert('저장 실패: ' + res.data.message)
    }
  } catch (err) {
    console.error('반품 저장 실패:', err)
    alert('반품 저장 중 오류 발생')
  }
}

onMounted(async () => {
  try {
    const res = await getOrderDetail(ordCd)
    const data = res.data.data

    formData.value = {
      returnCd: '',
      ordCd: data.ordCd,
      cpName: data.cpName,
      deliReqDt: format(parseISO(data.deliReqDt), 'yyyy-MM-dd')
    }

    products.value = data.orderDetails.map(item => ({
      ...item,
      returnQty: 0,
      returnRea: ''
    }))
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
