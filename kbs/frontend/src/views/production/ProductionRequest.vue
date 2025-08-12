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
import ProdReqSelectModal from '@/views/production/ProdReqSelectModal.vue'   // 생산요청 가져오기 모달

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

const prodDetailList = ref([]);        // 생산계획 제품 목록
const formData = ref({});              // 선택된 행 초기값 
const planModalvisible = ref(false)    // 생산계획 모달 닫기 기본상태
const requestModalvisible = ref(false) // 생산요청 모달 닫기 기본상태

// 공통코드 가져오기
const common = useCommonStore()
const { commonCodes } = storeToRefs(common)


// 모달에서 선택된 생산계획 데이터 처리
const handlePlanSelect = ({ basicInfo, detailList }) => {
  formData.value = {
    produPlanCd: basicInfo.produPlanCd,
    factory: {
      fcode: basicInfo.fcode,
      facVerCd: basicInfo.facVerCd
    },
    requ: user.value.empCd,
    empName: user.value.empName,
    note: basicInfo.note,
  }
  // 오늘 날짜를 기본값으로 설정
  formData.value.reqDt = new Date()

    // 남은수량 계산하여 데이터 가공
  prodDetailList.value = detailList.map(item => ({
    ...item,
    totalReqQty: item.totalReqQty || 0,
    remainingQty: Math.max(0, (item.planQty || 0) - (item.totalReqQty || 0)) // 마이너스일 경우 0으로 설정
  }))
}

// 모달에서 선택된 생산요청 데이터 처리
const handleReqSelect = ({ basicInfo, detailList }) => {
  formData.value = {
    produReqCd: basicInfo.produReqCd,
    produPlanCd: basicInfo.produPlanCd,
    reqDt: basicInfo.reqDt,
    deliDt: basicInfo.deliDt,
    requ: basicInfo.requ,
    empName: basicInfo.empName,
    factory: {
      fcode: basicInfo.fcode,
      facVerCd: basicInfo.facVerCd
    },
    note: basicInfo.note,
  }
  console.log(formData.value)

  prodDetailList.value = detailList
}

onMounted(async () => {
  await fetchFactoryList() // 공장 목록 조회
  await fetchProductList() // 제품 목록 가져오기
  await common.fetchCommonCodes('0G') // 공통코드 가져오기

  // 오늘 날짜를 기본값으로 설정
  if (!formData.value.reqDt) {
    formData.value.reqDt = new Date()
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
  { key: 'produReqCd', label: '생산요청번호', type: 'readonly' },
  { key: 'produPlanCd', label: '생산계획번호', type: 'readonlyModal', clickable: true, placeholder: '클릭 시 검색모달' },
  { key: 'reqDt', label: '생산요청일자', type: 'calendar2', placeholder: 'YYYY-MM-DD' },
  {
    key: 'factory',
    label: '공장',
    type: 'dropdown2',
    options: factoryOptions,
    placeholder: '공장을 선택하세요'
  },
  { key: 'empName', label: '요청자', type: 'readonly' },
  { key: 'deliDt', label: '납기일자', type: 'calendar2', placeholder: 'YYYY-MM-DD' },
  { key: 'note', label: '비고', type: 'textarea', rows: 1, cols: 20 }
]

const prodReqFormButtons = ref({
  save: { show: isAdmin.value || isManager.value, label: '저장', severity: 'success' },
  reset: { show: true, label: '초기화', severity: 'secondary' },
  delete: { show: isAdmin.value || isManager.value, label: '삭제', severity: 'danger' },
  load: { show: true, label: '생산요청 불러오기', severity: 'info' }
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
  { field: 'planQty', header: '계획수량', type: 'input', width: '110px', align: 'right', readonly },
  { field: 'totalReqQty', header: '기요청수량', type: 'input', width: '110px', align: 'right', readonly },
  { field: 'remainingQty', header: '남은수량', type: 'input', width: '110px', align: 'right', readonly },
  { field: 'reqQty', header: '요청수량', type: 'input', width: '110px', align: 'right' },
  { field: 'unitName', header: '단위', type: 'input', width: '80px', align: 'center', readonly },
  { field: 'exProduDt', header: '생산예정일자', type: 'input', inputType: 'date', align: 'center' },
  { field: 'seq', header: '우선순위', type: 'input', width: '110px', align: 'center' }
]
// 생산요청과 관련 상세 저장(등록, 수정)
const handleSave = async (data) => {
  if (!isAdmin.value && !isManager.value) {
    toast.add({
      severity: 'error',
      summary: '등록/수정 실패',
      detail: '등록/수정 권한이 없습니다.',
      life: 3000
    });
    return;
  }
  if (!user.value?.empCd) {
    toast.add({
        severity: 'warn',
        summary: '경고',
        detail: '로그인 정보가 없습니다.',
        life: 3000
    });
    return;
  }
  // 폼 유효성 검사
  if (!validateForm()) {
    return; // 유효성 검사 실패 시 저장 중단
  }
  try {
    const isNew = !formData.value.produReqCd; // 등록/수정 여부 판별

    const payload = {
      request: {
        produReqCd: formData.value.produReqCd || null,
        produPlanCd: formData.value.produPlanCd,
        reqDt: formatDate(formData.value.reqDt),
        deliDt: formatDate(formData.value.deliDt),
        fcode: formData.value.factory?.fcode,
        facVerCd: formData.value.factory?.facVerCd,
        requ: formData.value.requ,  
        note: formData.value.note
      },
      reqDetails: prodDetailList.value.map(item => ({
        produProdCd: item.produProdCd,
        pcode: item.pcode,
        prodVerCd: item.prodVerCd,
        reqQty: item.reqQty,
        unit: item.unit,
        exProduDt: item.exProduDt,
        seq: item.seq
      }))
    }

    console.log('📦 최종 payload (생산요청 저장용)', JSON.stringify(payload, null, 2))

    await store.saveProdReq(payload)
    prodDetailList.value = []
    toast.add({
      severity: 'success',
      summary: isNew ? '신규 등록 완료' : '수정 완료',
      detail: isNew ? '생산요청이 새로 등록되었습니다.' : '생산계획이 수정되었습니다.',
      life: 3000
    });
  } catch (error) {
    const errorMessage = error?.response?.data?.message || error?.response?.data?.error || error?.response?.data || '저장 중 오류가 발생했습니다.';
    toast.add({
      severity: 'error',
      summary: '저장 실패',
      detail: errorMessage,
      life: 3000
    });
  }
}
// 생산요청 유효성 검사 영역 =====================================================================
// 날짜 포맷팅 헬퍼 함수
const formatDate = (date) => {
  if (!date) return null;
  try {
    if (typeof date === 'string') {
      // 이미 YYYY-MM-DD 형태인 경우
      if (/^\d{4}-\d{2}-\d{2}$/.test(date)) {
        return date;
      }
      // 다른 형태의 문자열인 경우 Date 객체로 변환
      date = new Date(date);
    }
    if (date instanceof Date && !isNaN(date.getTime())) {
      return format(date, 'yyyy-MM-dd');
    }
    return null;
  } catch (err) {
    console.error('날짜 포맷팅 오류:', err, date);
    return null;
  }
}

// 폼 유효성 검사 함수
const validateForm = () => {
  // 기본 정보 유효성 검사 (produPlanCd는 null 허용이므로 제외)
  const basicValidationRules = [
    {
      field: 'reqDt',
      value: formData.value.reqDt,
      message: '생산요청일자를 입력해주세요.'
    },
    {
      field: 'factory',
      value: formData.value.factory?.fcode,
      message: '공장을 선택해주세요.'
    },
    {
      field: 'deliDt',
      value: formData.value.deliDt,
      message: '납기일자를 입력해주세요.'
    }
  ];

  for (const rule of basicValidationRules) {
    if (!rule.value) {
      toast.add({
        severity: 'warn',
        summary: '입력 확인',
        detail: rule.message,
        life: 3000
      });
      return false;
    }
  }
  
  // 날짜 순서 검증 (요청일 <= 납기일)
  if (formData.value.reqDt && formData.value.deliDt) {
    const reqDate = new Date(formData.value.reqDt);
    const deliDate = new Date(formData.value.deliDt);
    
    if (reqDate > deliDate) {
      toast.add({
        severity: 'warn',
        summary: '날짜 오류',
        detail: '생산요청일자가 납기일자보다 늦을 수 없습니다.',
        life: 3000
      });
      return false;
    }
  }

  // 제품 목록 유효성 검사
  if (!prodDetailList.value || prodDetailList.value.length === 0) {
    toast.add({
      severity: 'warn',
      summary: '제품 목록 확인',
      detail: '요청할 제품을 하나 이상 추가해주세요.',
      life: 3000
    });
    return false;
  }
  
  // 각 제품 행별 필수값 검증
  for (let i = 0; i < prodDetailList.value.length; i++) {
    const item = prodDetailList.value[i];
    const rowNum = i + 1;

    // 필수 필드 검증
    const requiredFields = [
      { field: 'pcode', label: '제품코드' },
      { field: 'reqQty', label: '요청수량' },
      { field: 'exProduDt', label: '생산예정일자' },
      { field: 'seq', label: '우선순위' }
    ];

    for (const fieldRule of requiredFields) {
      if (!item[fieldRule.field] || (fieldRule.field === 'reqQty' && item[fieldRule.field] <= 0)) {
        toast.add({
          severity: 'warn',
          summary: '제품 정보 확인',
          detail: `${rowNum}번째 행의 ${fieldRule.label}을(를) 입력해주세요.`,
          life: 3000
        });
        return false;
      }
    }

    // 요청수량이 숫자이고 0보다 큰지 검증
    if (isNaN(item.reqQty) || Number(item.reqQty) <= 0) {
      toast.add({
        severity: 'warn',
        summary: '요청수량 오류',
        detail: `${rowNum}번째 행의 요청수량은 0보다 큰 숫자여야 합니다.`,
        life: 3000
      });
      return false;
    }

    // 우선순위가 숫자이고 1 이상인지 검증
    if (isNaN(item.seq) || Number(item.seq) < 1 || !Number.isInteger(Number(item.seq))) {
      toast.add({
        severity: 'warn',
        summary: '우선순위 오류',
        detail: `${rowNum}번째 행의 우선순위는 1 이상의 정수여야 합니다.`,
        life: 3000
      });
      return false;
    }
  }

  // 우선순위 중복 및 순차성 검증
  const seqList = prodDetailList.value.map(item => Number(item.seq)).sort((a, b) => a - b);
  
  // 중복 검사
  const uniqueSeqList = [...new Set(seqList)];
  if (seqList.length !== uniqueSeqList.length) {
    toast.add({
      severity: 'warn',
      summary: '우선순위 중복',
      detail: '우선순위에 중복된 값이 있습니다. 각 제품의 우선순위는 고유해야 합니다.',
      life: 3000
    });
    return false;
  }

  // 1부터 시작하는 순차성 검사
  for (let i = 0; i < seqList.length; i++) {
    if (seqList[i] !== i + 1) {
      toast.add({
        severity: 'warn',
        summary: '우선순위 순서 오류',
        detail: `우선순위는 1부터 시작해서 순차적으로 입력해야 합니다. (현재: ${seqList.join(', ')})`,
        life: 3000
      });
      return false;
    }
  }

  return true;
}
// =========================================================================================
// 검색 입력란과 검색 결과 초기화
const handleReset = () => {
  formData.value = {}
  prodDetailList.value = []
}
// 생산요청과 관련 상세 삭제
const handleDelete = async (data) => {
  const reqCd = formData.value.produReqCd
  if (!reqCd) {
    toast.add({
      severity: 'warn',
      summary: '삭제 불가',
      detail: '저장되지 않은 요청은 삭제할 수 없습니다.',
      life: 3000
    })
    return
  }

  if (!confirm(`생산요청 '${reqCd}'을 정말 삭제하시겠습니까?`)) {
    return
  }

  try {
    await store.deleteProdReq(reqCd)

    toast.add({
      severity: 'success',
      summary: '삭제 완료',
      detail: `'${reqCd}' 삭제되었습니다.`,
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
  requestModalvisible.value = true;
}
const handleFieldClick = (fieldKey) => {
  if (fieldKey === 'produPlanCd') {
    planModalvisible.value = true;
  }
};
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
    <!-- 생산요청 입력 폼 -->
    <InputForm
      v-model:data="formData"
      :columns="fields"
      @fieldClick="handleFieldClick"
      title="생산요청 등록"
      :buttons="prodReqFormButtons"
      buttonPosition="top"
      :autoResetOnSave="false"
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
        :scroll-height="'50vh'" 
        :height="'60vh'"
      />
    </div>
    <!-- 생산계획 불러오기 모달 -->
    <ProdPlanSelectModal
      v-model:visible="planModalvisible"
      mode="basic"
      @select="handlePlanSelect"
    />
    <!-- 생산요청 불러오기 모달 -->
    <ProdReqSelectModal
      v-model:visible="requestModalvisible"
      mode="full"
      @select="handleReqSelect"
    />
    <Toast />
  </div>
</template>