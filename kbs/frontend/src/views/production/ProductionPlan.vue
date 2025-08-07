<script setup>
import { ref, onMounted, computed, readonly } from 'vue'
import { format } from 'date-fns'
import InputForm from '@/components/kimbap/searchform/inputForm.vue'
import InputTable from '@/components/kimbap/table/InputTable.vue';
import Toast from 'primevue/toast'; // 알람 표시
import { useToast } from 'primevue/usetoast'; // 알람 표시
import { storeToRefs } from 'pinia'
import { useProductStore } from '@/stores/productStore'
import { useCommonStore } from '@/stores/commonStore'
import { useMemberStore } from '@/stores/memberStore'
import ProdPlanSelectModal from '@/views/production/ProdPlanSelectModal.vue' // 생산계획 가져오기 모달
import MrpPreviewModal from '@/views/production/MrpPreviewModal.vue' // MRP 미리보기 모달

// 로그인 정보 가져오기 ====================================================
const memberStore = useMemberStore()
const { user } = storeToRefs(memberStore)

const isEmployee = computed(() => user.value?.memType === 'p1')       // 사원
const isCustomer = computed(() => user.value?.memType === 'p2')       // 매출업체
const isSupplier = computed(() => user.value?.memType === 'p3')       // 공급업체
const isManager = computed(() => user.value?.memType === 'p4')        // 담당자
const isAdmin = computed(() => user.value?.memType === 'p5')          // 시스템 관리자
// ========================================================================
const toast = useToast();

const store = useProductStore()
const { 
  factoryList,      // 공장 목록
  productList       // 제품 목록
} = storeToRefs(store)
const { 
  fetchFactoryList, // 공장 목록 불러오기
  fetchProductList  // 제품 목록 불러오기
} = store

const prodDetailList = ref([]); // 생산계획 제품 목록
const formData = ref({});       // 선택된 행 초기값 
const modalVisible = ref(false) // 모달 열기 상태

// ==============================================
const mrpPreviewVisible = ref(false) // MRP 미리보기 모달 상태
const mrpPreviewData = ref({})       // MRP 미리보기 데이터
const mrpPreviewLoading = ref(false) // 로딩 상태
const pendingPlanData = ref(null)    // 저장 대기 중인 생산계획 데이터
// ==============================================

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

  // 오늘 날짜를 기본값으로 설정
  if (!formData.value.planDt) {
    formData.value.planDt = new Date()
  }
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
  { key: 'planDt', label: '계획일자', type: 'calendar', placeholder: 'MM/DD/YYYY' },
  { key: 'planStartDt', label: '계획기간(시작)', type: 'calendar', placeholder: 'MM/DD/YYYY' },
  { key: 'planEndDt', label: '계획기간(종료)', type: 'calendar', placeholder: 'MM/DD/YYYY' },
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
// 생산계획과 관련 상세 저장(등록, 수정)
const handleSave = async (data) => {
  try {
    const isNew = !formData.value.produPlanCd; // 등록/수정 여부 판별

    const payload = {
      plan: {
        produPlanCd: formData.value.produPlanCd || null,
        planDt: format(formData.value.planDt, 'yyyy-MM-dd'),
        planStartDt: format(formData.value.planStartDt, 'yyyy-MM-dd'),
        planEndDt: format(formData.value.planEndDt, 'yyyy-MM-dd'),
        fcode: formData.value.factory?.fcode,
        facVerCd: formData.value.factory?.facVerCd,
        mname: 'EMP-10001',  
        note: formData.value.note
      },
      planDetails: prodDetailList.value.map(item => ({
        ppdcode: item.ppdcode,
        pcode: item.pcode,
        prodVerCd: item.prodVerCd,
        planQty: item.planQty,
        unit: item.unit,
        exProduDt: item.exProduDt,
        seq: item.seq
      }))
    }

    console.log('📦 최종 payload (생산계획 저장용)', JSON.stringify(payload, null, 2))

    // 신규 등록이고 제품이 있는 경우에만 MRP 미리보기 표시
    if (isNew && prodDetailList.value.length > 0) {
      pendingPlanData.value = payload
      await showMrpPreview(payload)
    } else {
      // 기존처럼 바로 저장
      await saveProdPlanDirect(payload, isNew)
    }
  } catch (err) {
    toast.add({
      severity: 'error',
      summary: '처리 실패',
      detail: '처리 중 오류가 발생했습니다.',
      life: 3000
    });
  } 
}

// ===================================================
// MRP 미리보기 표시
const showMrpPreview = async (payload) => {
  mrpPreviewLoading.value = true
  mrpPreviewVisible.value = true
  
  try {
    const previewData = await store.fetchMrpPreview(payload)
    mrpPreviewData.value = previewData
  } catch (err) {
    toast.add({
      severity: 'error',
      summary: 'MRP 미리보기 실패',
      detail: 'MRP 미리보기를 불러오지 못했습니다.',
      life: 3000
    })
    mrpPreviewVisible.value = false
  } finally {
    mrpPreviewLoading.value = false
  }
}
// 실제 생산계획 저장
const saveProdPlanDirect = async (payload, isNew) => {
  await store.saveProdPlan(payload)
  prodDetailList.value = []
  toast.add({
    severity: 'success',
    summary: isNew ? '신규 등록 완료' : '수정 완료',
    detail: isNew ? '생산계획이 새로 등록되었습니다.' : '생산계획이 수정되었습니다.',
    life: 3000
  });
}
// MRP 미리보기 모달에서 확인 버튼 클릭
const handleMrpPreviewConfirm = async () => {
  try {
    if (pendingPlanData.value) {
      await saveProdPlanDirect(pendingPlanData.value, true)
      pendingPlanData.value = null
      mrpPreviewData.value = {}
    }
  } catch (err) {
    toast.add({
      severity: 'error',
      summary: '저장 실패',
      detail: '생산계획 저장 중 오류가 발생했습니다.',
      life: 3000
    });
  }
}
// MRP 미리보기 모달에서 취소 버튼 클릭
const handleMrpPreviewCancel = () => {
  pendingPlanData.value = null
  mrpPreviewData.value = {}
}
// ===================================================

// 검색 입력란과 검색 결과 초기화
const handleReset = () => {
  formData.value = {}
  prodDetailList.value = []
}
// 생산계획과 관련 상세 삭제
const handleDelete = async (data) => {
    const planCd = formData.value.produPlanCd
  if (!planCd) {
    toast.add({
      severity: 'warn',
      summary: '삭제 불가',
      detail: '저장되지 않은 계획은 삭제할 수 없습니다.',
      life: 3000
    })
    return
  }

  if (!confirm(`생산계획 '${planCd}'을 정말 삭제하시겠습니까?`)) {
    return
  }

  try {
    await store.deleteProdPlan(planCd)

    toast.add({
      severity: 'success',
      summary: '삭제 완료',
      detail: `'${planCd}' 삭제되었습니다.`,
      life: 3000
    })

    formData.value = {}
    prodDetailList.value = []

  } catch (err) {
    toast.add({
      severity: 'error',
      summary: '삭제 실패',
      detail: '삭제 중 오류가 발생했습니다.',
      life: 3000
    })
  }
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
  <Toast />

    <!-- 👑 페이지 헤더 -->
    <div class="mb-6">
      <h1 class="text-3xl font-bold text-gray-800 mb-2">생산계획 등록</h1>
      <div class="flex items-center gap-4 text-sm text-gray-600">
        <span>👤 {{ user?.empName || '로그로그' }}</span>
        <span>🏢 {{ user?.deptName || '생산팀' }}</span>
        <span>{{ user }}</span>
      </div>
    </div>
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
    <!-- MRP 미리보기 모달 -->
    <MrpPreviewModal
      v-model:visible="mrpPreviewVisible"
      :mrp-preview="mrpPreviewData"
      :loading="mrpPreviewLoading"
      @confirm="handleMrpPreviewConfirm"
      @cancel="handleMrpPreviewCancel"
    />
  </div>
</template>