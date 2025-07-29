<script setup>
import { ref, onMounted, computed, readonly } from 'vue'
import { format } from 'date-fns'
import InputForm from '@/components/kimbap/searchform/inputForm.vue'
import InputTable from '@/components/kimbap/table/InputTable.vue';
import { storeToRefs } from 'pinia'
import { useProductStore } from '@/stores/productStore'
import { useCommonStore } from '@/stores/commonStore'
import ProdPlanSelectModal from '@/views/production/ProdPlanSelectModal.vue' // 생산계획 가져오기 모달

const store = useProductStore()
const { factoryList, productList  } = storeToRefs(store)
const { fetchFactoryList, fetchProductList, saveProdPlan } = store

const prodDetailList = ref([]); // 생산계획 제품 목록
const formData = ref({});  // 선택된 행 초기값 
const modalVisible = ref(false) // 모달 열기 상태

// 공통코드 가져오기
const common = useCommonStore()
const { commonCodes } = storeToRefs(common)


// 모달에서 선택된 데이터 처리
const handlePlanSelect = ({ basicInfo, detailList }) => {
  formData.value = {
    produPlanCd: basicInfo.produPlanCd,
    planDt: basicInfo.planDt,
    planStartDt: basicInfo.planStartDt,
    planEndDt: basicInfo.planEndDt,
    factory: {
      fcode: basicInfo.fcode,
      facVerCd: basicInfo.facVerCd
    },
    note: basicInfo.note
  }
  prodDetailList.value = detailList
}

onMounted(async () => {
  await fetchFactoryList() // 공장 목록 조회
  await fetchProductList() // 제품 목록 가져오기
  await common.fetchCommonCodes('0G') // 공통코드 가져오기
})

// 공장 드롭다운 옵션
const factoryOptions = computed(() =>
  factoryList.value.map(f => ({
    label: f.facName,
    value: { fcode: f.fcode, facVerCd: f.facVerCd }
  }))
)

// 폼 필드 정의 (InputForm.vue 기준 key 속성 사용)
const fields = [
  { key: 'produPlanCd', label: '생산계획번호', type: 'readonly' },
  { key: 'planDt', label: '계획일자', type: 'calendar', placeholder: 'YYYY-MM-DD' },
  { key: 'planStartDt', label: '계획기간(시작)', type: 'calendar', placeholder: 'YYYY-MM-DD' },
  { key: 'planEndDt', label: '계획기간(종료)', type: 'calendar', placeholder: 'YYYY-MM-DD' },
  {
    key: 'factory',
    label: '공장',
    type: 'dropdown2',
    options: factoryOptions,
    placeholder: '공장을 선택하세요'
  },
  { key: 'note', label: '비고', type: 'textarea' }
]

const prodPlanFormButtons = ref({
  save: { show: true, label: '저장', severity: 'success' },
  reset: { show: true, label: '초기화', severity: 'secondary' },
  delete: { show: true, label: '삭제', severity: 'danger' },
  load: { show: true, label: '생산계획 불러오기', severity: 'info' }
})
const prodPlanDetailButtons = ref({
  save: { show: false, label: '저장', severity: 'success' },
  reset: { show: false, label: '초기화', severity: 'secondary' }
})

// 제품 테이블 컬럼 정의
const productColumns = [
  {
    field: 'pcode',
    header: '제품코드',
    type: 'inputsearch',
    suffixIcon: 'pi pi-search',
    align: 'left',
    readonly,
    placeholder: '검색'
  },
  { field: 'prodName', header: '제품명', type: 'input', align: 'left', readonly },
  { field: 'planQty', header: '생산수량', type: 'input', align: 'right' },
  { field: 'unitName', header: '단위', type: 'input', align: 'center', readonly },
  { field: 'exProduDt', header: '생산예정일자', type: 'input', inputType: 'date', align: 'center' },
  { field: 'seq', header: '우선순위', type: 'input', align: 'center' }
]

// 버튼 이벤트 핸들러
const handleSave = async (data) => {
  try {
    const payload = {
      plan: {
        produPlanCd: formData.value.produPlanCd || null,
        planDt: format(formData.value.planDt, 'yyyy-MM-dd'),
        planStartDt: format(formData.value.planStartDt, 'yyyy-MM-dd'),
        planEndDt: format(formData.value.planEndDt, 'yyyy-MM-dd'),
        fcode: formData.value.factory?.fcode,
        facVerCd: formData.value.factory?.facVerCd,
        mname: 'EMP-00001',  
        note: formData.value.note
      },
      planDetails: prodDetailList.value.map(item => ({
        pcode: item.pcode,
        prodVerCd: item.prodVerCd,
        planQty: item.planQty,
        unit: item.unit,
        exProduDt: item.exProduDt,
        seq: item.seq
      }))
    }

    console.log('📦 최종 payload (생산계획 저장용)', JSON.stringify(payload, null, 2))

    await store.saveProdPlan(payload)
    alert('저장 성공')
  } catch (err) {
    console.error('❌ 저장 실패:', err)
    alert('저장 실패')
  }
}

const handleReset = () => {
  formData.value = {}
  prodDetailList.value = []
}

const handleDelete = (data) => {
  console.log('🗑 삭제 요청됨:', data)
}
// =========================================
// 생산계획 불러오기 모달 버튼
const handleLoad = () => {
  modalVisible.value = true
}
// =========================================
// 공통코드 변환
const convertedProductList = computed(() => {
  const unitCodes = common.getCodes('0G')
  return productList.value.map(item => {
    const matched = unitCodes.find(code => code.dcd === item.unit)
    return {
      ...item,
      unitName: matched?.cdInfo || item.unit
    }
  })
})
// 제품 정보 가져오기 
const modalDataSets = computed(() => ({
  pcode: {
    items: convertedProductList.value,  // ← 여기서 가공된 productList 사용
    columns: [
      { field: 'pcode', header: '제품코드' },
      { field: 'prodName', header: '제품명' },
      { field: 'unitName', header: '단위' }
    ],
    itemKey: 'pcode',
    displayField: 'pcode',
    mappingFields: {
      pcode: 'pcode',
      prodVerCd: 'prodVerCd',
      prodName: 'prodName',
      unitName: 'unitName',
      unit: 'unit'
    }
  }
}))
</script>

<template>
  <div class="space-y-8">
    <!-- 생산계획 입력 폼 -->
    <InputForm
      v-model:data="formData"
      :columns="fields"
      title="생산계획 기본 정보"
      :buttons="prodPlanFormButtons"
      buttonPosition="top"
      @submit="handleSave"
      @reset="handleReset"
      @delete="handleDelete"
      @load="handleLoad"
    />
    <!-- 제품 목록 테이블 -->
    <div>
      <InputTable
        v-model:data="prodDetailList"
        :columns="productColumns"
        :title="'제품 목록'"
        :dataKey="'pcode'"
        :modalDataSets="modalDataSets"
        buttonPosition="top"
        :buttons="prodPlanDetailButtons"
        enableRowActions
        enableSelection
        scrollHeight="300px"
      />
    </div>
    <!-- 생산계획 불러오기 모달 -->
    <ProdPlanSelectModal
      v-model:visible="modalVisible"
      @select="handlePlanSelect"
    />
  </div>
</template>