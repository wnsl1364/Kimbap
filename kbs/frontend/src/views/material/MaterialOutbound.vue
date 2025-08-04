<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useMaterialStore } from '@/stores/materialStore'
import { useMemberStore } from '@/stores/memberStore'
import { useCommonStore } from '@/stores/commonStore'
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue'
import InputTable from '@/components/kimbap/table/InputTable.vue'
import Button from 'primevue/button'
// 🔥 완전한 API 연결!
import { getSuppliersMateRel, processMaterialOutboundBatch, updateMaterialOutboundStatus } from '@/api/materials'

// ✨ Store들
const materialStore = useMaterialStore()
const memberStore = useMemberStore()
const commonStore = useCommonStore()

// 🎯 검색 관련 데이터 (발주번호, 상태, 발주일자만!)
const searchData = ref({
  purcCd: '',          // 발주번호
  status: '',          // 상태
  ordDt: null          // 발주일자
})

// 🔥 출고 데이터 (실제 API에서 가져온 데이터)
const outboundData = ref([])

// 🎯 선택된 자재들 (출고 처리용)
const selectedMaterials = ref([])

// 🎨 검색 폼 컬럼 정의 (발주번호, 상태, 발주일자 3개만!)
const searchColumns = [
  { 
    field: 'purcCd', 
    label: '발주번호', 
    type: 'text',
    placeholder: '발주번호를 입력하세요' 
  },
  { 
    field: 'status', 
    label: '상태', 
    type: 'select',
    options: [
      { label: '전체', value: '' },
      { label: '승인완료', value: 'c2' },
      { label: '출고대기', value: 'd1' },
      { label: '출고완료', value: 'd3' }
    ]
  },
  { 
    field: 'ordDt', 
    label: '발주일자', 
    type: 'date' 
  }
]

// 🎨 테이블 컬럼 정의 (실제 출고 업무 스펙에 맞게!)
const outboundColumns = [
  { field: 'purcCd', header: '발주번호', type: 'readonly' },
  { field: 'status', header: '상태', type: 'readonly', align: 'center' },
  { field: 'ordDt', header: '발주일자', type: 'readonly', align: 'center' },
  { field: 'mateName', header: '자재명', type: 'readonly' },
  { field: 'purcQty', header: '요청수량', type: 'readonly', align: 'right' },
  { field: 'unit', header: '단위', type: 'readonly', align: 'center' },
  { field: 'currQty', header: '출고수량', type: 'input', inputType: 'number', align: 'right' },
  { field: 'leftQty', header: '남은수량', type: 'readonly', align: 'right' },
  { field: 'exDeliDt', header: '납기예정일', type: 'readonly', align: 'center' },
  { field: 'deliDt', header: '납기일', type: 'calendar', align: 'center' },
  { field: 'note', header: '비고', type: 'input', inputType: 'text', placeholder: '반려사유 등 입력' }
]

// 🎨 검색 폼 버튼 설정
const searchFormButtons = ref({
  search: { show: true, label: '검색', severity: 'primary' },
  reset: { show: true, label: '초기화', severity: 'secondary' },
  add: { show: false },
  delete: { show: false }
})

// 🎨 테이블 버튼 설정
const materialTableButtons = ref({
  add: { show: false },
  edit: { show: false },
  delete: { show: false },
  save: { show: false }
})

// 💫 공통코드 변환 함수들 (null 체크 추가!)
const getUnitText = (unitCode) => {
  if (!unitCode) return ''
  const unitCodes = commonStore.getCodes ? commonStore.getCodes('0G') : []
  if (!unitCodes || !Array.isArray(unitCodes)) return unitCode
  const found = unitCodes.find(code => code.dcd === unitCode)
  return found ? found.cdInfo : unitCode
}

const getOutboundStatusText = (statusCode) => {
  if (!statusCode) return ''
  const statusMapping = {
    'd1': '출고대기',
    'd2': '출고진행', 
    'd3': '출고완료'
  }
  return statusMapping[statusCode] || statusCode
}

const getWarehouseText = (wcode) => {
  if (!wcode) return ''
  // 창고 코드 변환 로직 (실제 창고 코드에 맞게 수정 필요)
  const warehouseMapping = {
    'WH001': '1창고',
    'WH002': '2창고', 
    'WH003': '3창고'
  }
  return warehouseMapping[wcode] || wcode
}

// 📅 날짜 포맷 함수
const formatDate = (date) => {
  if (!date) return ''
  try {
    const dateObj = date instanceof Date ? date : new Date(date)
    return dateObj.toISOString().split('T')[0]
  } catch (error) {
    console.error('날짜 포맷 에러:', error)
    return ''
  }
}

const formatDateForTable = (date) => {
  if (!date) return ''
  try {
    const dateObj = date instanceof Date ? date : new Date(date)
    return dateObj.toLocaleDateString('ko-KR')
  } catch (error) {
    console.error('테이블 날짜 포맷 에러:', error)
    return ''
  }
}

// 🔍 검색 기능
const handleSearch = () => {
  console.log('🔍 출고 검색 실행:', searchData.value)
  fetchOutboundData()
}

// 🔄 검색 초기화
const handleReset = () => {
  searchData.value = {
    purcCd: '',
    status: '',
    ordDt: null
  }
  console.log('🔄 검색 조건 초기화')
  fetchOutboundData()
}

// 🎯 실제 DB에서 출고 데이터 가져오는 함수 (완전한 API 연결!)
const fetchOutboundData = async () => {
  try {
    console.log('🔍 출고 데이터 조회 시작...')
    
    // 🔥 실제 API 호출!
    const searchCriteria = {
      purcCd: searchData.value.purcCd,
      purcDStatus: searchData.value.status || 'c2', // 기본값: 승인완료
      ordDtStart: searchData.value.ordDt ? formatDate(searchData.value.ordDt) : null,
      ordDtEnd: searchData.value.ordDt ? formatDate(searchData.value.ordDt) : null
    }
    
    console.log('📤 실제 API 호출 조건:', searchCriteria)
    const response = await getSuppliersMateRel(searchCriteria)
    console.log('📦 DB에서 가져온 실제 출고 목록:', response.data)
    
    // 🎨 실제 DB 데이터를 테이블용으로 변환
    outboundData.value = response.data.map((item, index) => ({
      id: index + 1,
      // 실제 DB 필드들 매핑
      purcCd: item.purcCd,
      status: getOutboundStatusText(item.purcDStatus),
      ordDt: item.ordDt ? formatDateForTable(item.ordDt) : '',
      mateName: item.mateName,
      purcQty: item.purcQty || 0,
      unit: getUnitText(item.unit),
      currQty: item.currQty || 0,
      leftQty: item.leftQty || ((item.purcQty || 0) - (item.currQty || 0)), // DB 계산값 또는 직접 계산
      exDeliDt: item.exDeliDt ? formatDateForTable(item.exDeliDt) : '',
      deliDt: item.deliDt ? formatDateForTable(item.deliDt) : null,
      note: item.note || '',
      // 원본 데이터 보관 (처리 시 필요)
      _originalData: {
        purcCd: item.purcCd,
        purcDCd: item.purcDCd,
        mcode: item.mcode,
        mateVerCd: item.mateVerCd,
        cpCd: item.cpCd,
        purcQty: item.purcQty,
        currQty: item.currQty,
        unit: item.unit,
        unitPrice: item.unitPrice,
        purcDStatus: item.purcDStatus
      }
    }))
    
    console.log('✅ 실제 DB 출고 데이터 변환 완료:', outboundData.value.length, '건')
    
  } catch (error) {
    console.error('❌ 실제 API 호출 실패:', error)
    
    // 🎭 API 실패 시에만 샘플 데이터로 fallback
    console.log('⚠️  API 실패로 샘플 데이터 사용')
    const sampleData = [
      {
        id: 1,
        purcCd: 'PURC-2025-001',
        status: '승인완료',
        ordDt: '2025-08-01',
        mateName: '김(건조)',
        purcQty: 100,
        unit: 'kg',
        currQty: 0,
        leftQty: 100,
        exDeliDt: '2025-08-05',
        deliDt: null,
        note: '',
        _originalData: {
          purcCd: 'PURC-2025-001',
          purcDCd: 'PURC-D-001',
          mcode: 'MAT-1001',
          purcQty: 100,
          purcDStatus: 'c2'
        }
      },
      {
        id: 2,
        purcCd: 'PURC-2025-002',
        status: '승인완료',
        ordDt: '2025-08-02',
        mateName: '쌀(백미)',
        purcQty: 200,
        unit: 'kg',
        currQty: 50,
        leftQty: 150,
        exDeliDt: '2025-08-06',
        deliDt: null,
        note: '',
        _originalData: {
          purcCd: 'PURC-2025-002',
          purcDCd: 'PURC-D-002',
          mcode: 'MAT-1002',
          purcQty: 200,
          purcDStatus: 'c2'
        }
      },
      {
        id: 3,
        purcCd: 'PURC-2025-003',
        status: '승인완료',
        ordDt: '2025-08-03',
        mateName: '참치(캔)',
        purcQty: 500,
        unit: 'ea',
        currQty: 0,
        leftQty: 500,
        exDeliDt: '2025-08-07',
        deliDt: null,
        note: '',
        _originalData: {
          purcCd: 'PURC-2025-003',
          purcDCd: 'PURC-D-003',
          mcode: 'MAT-1003',
          purcQty: 500,
          purcDStatus: 'c2'
        }
      }
    ]
    
    outboundData.value = sampleData
    
    // 에러 메시지 표시
    alert(`실제 API 호출 실패: ${error.message}\n샘플 데이터를 표시합니다.`)
  }
}

// 🎯 출고완료 처리 함수
const handleOutboundComplete = async () => {
  try {
    console.log('🚚 출고완료 처리 시작...')
    
    // 유효성 검증
    if (!selectedMaterials.value || selectedMaterials.value.length === 0) {
      alert('출고완료 처리할 항목을 선택해주세요! 😅')
      return
    }
    
    console.log('📦 선택된 자재들:', selectedMaterials.value)
    
    // 출고완료 처리할 데이터 준비
    const outboundCompleteDataList = selectedMaterials.value.map((material) => ({
      outbCd: material._originalData?.outbCd || material.outbCd,
      mcode: material._originalData?.mcode || material.mcode,
      outbStatus: 'd3', // 출고완료로 상태 변경
      outbQty: material._originalData?.outbQty || material.outbQty,
      outbDt: new Date(), // 출고완료 시간 업데이트
      regi: memberStore.user?.empName || '김김밥',
      note: `${material.mateName} 출고완료 처리`
    }))
    
    console.log('📤 출고완료 처리 데이터:', outboundCompleteDataList)
    
    // 🔥 실제 출고완료 API 호출!
    try {
      console.log('🚚 실제 출고완료 API 호출 시작...')
      
      // 일괄 처리 API 호출
      const response = await processMaterialOutboundBatch(outboundCompleteDataList)
      console.log('✅ 실제 출고완료 API 응답:', response.data)
      
      // 성공 시 추가 처리
      if (response.data.success) {
        console.log('🎉 출고완료 처리 성공!')
      }
      
    } catch (apiError) {
      console.error('❌ 출고완료 API 호출 실패:', apiError)
      
      // API 실패 시 개별 처리 시도
      try {
        console.log('🔄 개별 처리로 재시도...')
        for (const completeData of outboundCompleteDataList) {
          await updateMaterialOutboundStatus({
            purcDCd: completeData.purcDCd,
            currQty: completeData.currQty,
            deliDt: completeData.deliDt,
            note: completeData.note,
            outbStatus: 'd3' // 출고완료
          })
        }
        console.log('✅ 개별 처리 성공!')
      } catch (individualError) {
        console.error('❌ 개별 처리도 실패:', individualError)
        throw individualError // 상위로 에러 전파
      }
    }
    
    // Store에 데이터 저장 (실제 함수 사용!)
    materialStore.setOutboundData({
      completedMaterials: [...selectedMaterials.value],
      processedAt: new Date(),
      processedBy: memberStore.user?.empName || '김김밥'
    })
    
    // 성공 메시지
    alert(`출고완료 처리가 완료되었습니다! 🎉 
(처리된 자재: ${selectedMaterials.value.length}개)`)
    
    // 선택 초기화
    selectedMaterials.value = []
    
    // 데이터 새로고침
    await fetchOutboundData()
    
  } catch (error) {
    console.error('❌ 출고완료 처리 중 오류 발생:', error)
    
    let errorMessage = '출고완료 처리 중 오류가 발생했습니다. 😢'
    
    if (error.response) {
      errorMessage = `출고완료 처리 실패: ${error.response.data?.message || '서버 오류'}`
      console.log('서버 에러 상세:', error.response)
    } else if (error.request) {
      errorMessage = '서버와 통신할 수 없습니다. 네트워크를 확인해주세요! 📡'
    }
    
    alert(errorMessage)
  }
}

// 🎯 선택된 자재들 watch
watch(selectedMaterials, (newSelection) => {
  console.log('📋 선택된 자재 변경:', newSelection.length, '개')
}, { deep: true })

// 🎯 컴포넌트 초기화
onMounted(async () => {
  console.log('🚀 MaterialOutbound 컴포넌트 초기화 시작!')
  
  try {
    // 공통코드 로드 대기 (안전한 방식으로 수정)
    if (!commonStore.getCodes || (commonStore.codes && commonStore.codes.length === 0)) {
      console.log('⏳ 공통코드 로드 대기 중...')
      await new Promise(resolve => setTimeout(resolve, 1000))
    }
    
    // 실제 출고 데이터 조회
    await fetchOutboundData()
    
    console.log('✅ MaterialOutbound 초기화 완료!')
  } catch (error) {
    console.error('❌ MaterialOutbound 초기화 실패:', error)
    
    // 에러 발생해도 기본 데이터는 로드
    await fetchOutboundData()
  }
})

// 🎯 computed로 선택 가능한 항목 수 계산
const selectableItemsCount = computed(() => {
  return outboundData.value.filter(item => 
    item.status === '승인완료'
  ).length
})

</script>

<template>
  <div class="material-outbound-container">
    <!-- 🔍 검색 폼 -->
    <SearchForm
      :columns="searchColumns"
      v-model:searchData="searchData"
      :formButtons="searchFormButtons"
      :gridColumns="3"
      @search="handleSearch"
      @reset="handleReset"
    />
    
    <!-- 📊 출고 현황 요약 -->
    <div class="flex justify-between items-center mb-4 p-4 bg-blue-50 rounded-lg">
      <div class="flex gap-6">
        <div class="text-center">
          <div class="text-2xl font-bold text-blue-600">{{ outboundData.length }}</div>
          <div class="text-sm text-gray-600">총 발주 항목</div>
        </div>
        <div class="text-center">
          <div class="text-2xl font-bold text-orange-600">{{ selectableItemsCount }}</div>
          <div class="text-sm text-gray-600">출고 가능 항목</div>
        </div>
        <div class="text-center">
          <div class="text-2xl font-bold text-green-600">{{ selectedMaterials.length }}</div>
          <div class="text-sm text-gray-600">선택된 항목</div>
        </div>
      </div>
      <div class="text-sm text-gray-500">
        마지막 업데이트: {{ new Date().toLocaleString('ko-KR') }}
      </div>
    </div>
    
    <!-- 📋 출고 목록 테이블 -->
    <InputTable
      :columns="outboundColumns"
      :data="outboundData"
      :buttons="materialTableButtons"
      :enableRowActions="false"
      :enableSelection="true"
      selectionMode="multiple"
      v-model:selection="selectedMaterials"
      dataKey="id"
      :autoCalculation="{
        enabled: true,
        quantityField: 'currQty',
        totalField: 'leftQty',
        calculation: (item) => (item.purcQty || 0) - (item.currQty || 0)
      }"
    >
      <!-- 🎯 상단 버튼들 -->
      <template #top-buttons>
        <div class="flex gap-2">
          <Button 
            label="출고완료 처리" 
            outlined 
            severity="success" 
            icon="pi pi-check-circle"
            @click="handleOutboundComplete"
            :disabled="selectedMaterials.length === 0"
          />
          <Button 
            label="새로고침" 
            outlined 
            severity="info" 
            icon="pi pi-refresh"
            @click="fetchOutboundData"
          />
        </div>
      </template>
      
      <!-- 🎨 상태별 스타일링 -->
      <template #status="{ data }">
        <span 
          :class="{
            'bg-green-100 text-green-800 px-2 py-1 rounded-full text-xs': data.status === '승인완료',
            'bg-yellow-100 text-yellow-800 px-2 py-1 rounded-full text-xs': data.status === '출고대기',
            'bg-blue-100 text-blue-800 px-2 py-1 rounded-full text-xs': data.status === '출고완료'
          }"
        >
          {{ data.status }}
        </span>
      </template>
      
      <!-- 🎯 수량 포맷팅 -->
      <template #purcQty="{ data }">
        <span class="font-mono text-right">{{ data.purcQty?.toLocaleString() }}</span>
      </template>
      
      <template #currQty="{ data }">
        <span class="font-mono text-right">{{ data.currQty?.toLocaleString() }}</span>
      </template>
      
      <template #leftQty="{ data }">
        <span class="font-mono text-right font-semibold" 
              :class="{ 'text-red-600': data.leftQty < 0, 'text-blue-600': data.leftQty > 0 }">
          {{ data.leftQty?.toLocaleString() }}
        </span>
      </template>
    </InputTable>
    
    <!-- 💡 도움말 섹션 -->
    <div class="mt-6 p-4 bg-gray-50 rounded-lg">
      <h3 class="text-lg font-semibold mb-2 text-gray-700">💡 자재출고 처리 안내</h3>
      <ul class="text-sm text-gray-600 space-y-1">
        <li>• <strong>승인완료</strong> 상태의 발주 항목만 출고 처리 가능합니다</li>
        <li>• <strong>출고수량</strong>을 입력하면 남은수량이 자동으로 계산됩니다</li>
        <li>• <strong>납기일</strong>을 클릭하여 실제 납기일을 입력할 수 있습니다</li>
        <li>• <strong>비고</strong>란에 반려사유나 특이사항을 입력할 수 있습니다</li>
        <li>• 여러 항목을 선택하여 일괄 출고완료 처리할 수 있습니다</li>
      </ul>
    </div>
  </div>
</template>

<style scoped>
.material-outbound-container {
  padding: 1rem;
}

/* 🎨 테이블 스타일 개선 */
:deep(.p-datatable .p-datatable-tbody > tr:hover) {
  background-color: #f8fafc;
}

:deep(.p-datatable .p-datatable-thead > tr > th) {
  background-color: #f1f5f9;
  font-weight: 600;
}

/* 🎯 선택된 행 스타일 */
:deep(.p-datatable .p-datatable-tbody > tr.p-highlight) {
  background-color: #dbeafe !important;
  color: #1e40af;
}

/* 📱 반응형 스타일 */
@media (max-width: 768px) {
  .material-outbound-container {
    padding: 0.5rem;
  }
  
  .flex.gap-6 {
    flex-direction: column;
    gap: 1rem;
  }
}
</style>