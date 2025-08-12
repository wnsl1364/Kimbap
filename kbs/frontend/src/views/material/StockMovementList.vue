<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { storeToRefs } from 'pinia';
import { useStockMovementListStore } from '@/stores/stockMovementListStore';
import { useMemberStore } from '@/stores/memberStore';
import { useToast } from 'primevue/usetoast';
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import BasicTable from '@/components/kimbap/table/BasicTable.vue';
import LeftAlignTable from '@/components/kimbap/table/LeftAlignTable.vue';
import Button from 'primevue/button';

// Store 및 기본 설정
const stockMovementListStore = useStockMovementListStore();
const memberStore = useMemberStore();
const toast = useToast();

// Store에서 상태 가져오기
const {
  moveRequestList,
  selectedMoveRequest,
  moveRequestDetails,
  isLoading,
  searchFilter,
  searchColumns,
  moveRequestColumns,
  moveRequestFields,
  moveRequestDetailColumns,
  approverFields,
  filteredMoveRequestList,
  selectedMoveRequestInfo
} = storeToRefs(stockMovementListStore);

const { user } = storeToRefs(memberStore);

// 반응형 상태
const selectedMoveRequestItem = ref(null);
const approverData = ref({});

// 검색 폼 버튼 설정 (SearchForm 사용 시 필요없음)
// const searchFormButtons = computed(() => ({
//   save: { show: true, label: '검색', severity: 'primary' },
//   reset: { show: true, label: '초기화', severity: 'secondary' },
//   delete: { show: false },
//   load: { show: false }
// }));

// 상세정보 버튼 설정
const detailFormButtons = computed(() => ({
  save: { show: false },
  reset: { show: false },
  delete: { show: false },
  load: { show: false }
}));

// 승인자 정보 초기화
const initializeApproverData = () => {
  approverData.value = {
    approverName: user.value?.empName || '',
    rejectionReason: ''
  };
  console.log('승인자 정보 초기화:', approverData.value);
};

// 컴포넌트 마운트 시 초기화
onMounted(async () => {
  try {
    console.log('StockMovementList 컴포넌트 초기화');
    initializeApproverData();
    await loadInitialData();
  } catch (error) {
    console.error('초기화 중 오류 발생:', error);
    toast.add({
      severity: 'error',
      summary: '오류',
      detail: '데이터 로드 중 오류가 발생했습니다.',
      life: 3000
    });
  }
});

// 초기 데이터 로드
const loadInitialData = async () => {
  try {
    await stockMovementListStore.fetchMoveRequestList();
    console.log('이동요청 목록 로드 완료');
  } catch (error) {
    console.error('이동요청 목록 로드 실패:', error);
    throw error;
  }
};

// 검색 처리
const handleSearch = async (searchData) => {
  try {
    console.log('검색 조건:', searchData);
    
    stockMovementListStore.setSearchFilter(searchData);
    
    toast.add({
      severity: 'success',
      summary: '검색 완료',
      detail: `${filteredMoveRequestList.value.length}건의 이동요청을 찾았습니다.`,
      life: 3000
    });
  } catch (error) {
    console.error('검색 실패:', error);
    toast.add({
      severity: 'error',
      summary: '검색 실패',
      detail: '검색 중 오류가 발생했습니다.',
      life: 3000
    });
  }
};

// 검색 초기화
const handleReset = async () => {
  try {
    stockMovementListStore.clearSearchFilter();
    stockMovementListStore.clearSelectedMoveRequest();
    selectedMoveRequestItem.value = null;
    initializeApproverData();
    
    await stockMovementListStore.fetchMoveRequestList();
    
    toast.add({
      severity: 'info',
      summary: '초기화 완료',
      detail: '검색 조건이 초기화되었습니다.',
      life: 3000
    });
  } catch (error) {
    console.error('초기화 실패:', error);
    toast.add({
      severity: 'error',
      summary: '초기화 실패',
      detail: '초기화 중 오류가 발생했습니다.',
      life: 3000
    });
  }
};

// 테이블 행 선택 처리
const handleRowSelection = async (selection) => {
  console.log('handleRowSelection 호출됨, selection:', selection);
  console.log('selection 타입:', typeof selection);
  console.log('selection 배열 여부:', Array.isArray(selection));
  
  try {
    // selection이 배열이 아닌 경우 (단일 객체인 경우) 배열로 변환
    let selectionArray;
    if (Array.isArray(selection)) {
      selectionArray = selection;
    } else if (selection) {
      selectionArray = [selection];
    } else {
      selectionArray = [];
    }
    
    console.log('변환된 selectionArray:', selectionArray);
    
    if (selectionArray.length > 0) {
      const selected = selectionArray[0];
      console.log('선택된 이동요청:', selected);
      console.log('선택된 이동요청 코드:', selected.moveReqCd);
      
      selectedMoveRequestItem.value = selected;
      
      // 선택된 이동요청의 상세 정보 조회
      console.log('fetchMoveRequestById 호출 시작:', selected.moveReqCd);
      await stockMovementListStore.fetchMoveRequestById(selected.moveReqCd);
      console.log('fetchMoveRequestById 완료');
      
      console.log('fetchMoveRequestDetails 호출 시작:', selected.moveReqCd);
      await stockMovementListStore.fetchMoveRequestDetails(selected.moveReqCd);
      console.log('fetchMoveRequestDetails 완료');
      
      // 승인자 정보 업데이트 (승인 상태에 따라)
      if (selected.moveStatus === 'd2' && selected.apprName) {
        approverData.value.approverName = selected.apprName;
        approverData.value.approvalComment = '승인 완료';
      } else {
        initializeApproverData();
      }
      
      console.log('상세 정보 로드 완료:', {
        selectedMoveRequest: stockMovementListStore.selectedMoveRequest,
        moveRequestDetails: stockMovementListStore.moveRequestDetails
      });
    } else {
      console.log('선택 해제됨');
      selectedMoveRequestItem.value = null;
      stockMovementListStore.clearSelectedMoveRequest();
      initializeApproverData();
    }
  } catch (error) {
    console.error('행 선택 처리 실패:', error);
    toast.add({
      severity: 'error',
      summary: '오류',
      detail: '상세 정보를 불러오는 중 오류가 발생했습니다.',
      life: 3000
    });
  }
};

// 승인 처리
const handleApprove = async () => {
  if (!selectedMoveRequestItem.value) {
    toast.add({
      severity: 'warn',
      summary: '선택 필요',
      detail: '승인할 이동요청을 선택해주세요.',
      life: 3000
    });
    return;
  }

  if (selectedMoveRequestItem.value.moveStatus !== 'd1') {
    toast.add({
      severity: 'warn',
      summary: '승인 불가',
      detail: '요청 상태의 이동요청만 승인할 수 있습니다.',
      life: 3000
    });
    return;
  }

  try {
    const approver = user.value?.empCd || '';
    
    await stockMovementListStore.approveMoveRequestById(
      selectedMoveRequestItem.value.moveReqCd,
      approver,
      '' // 승인 시에는 빈 코멘트
    );
    
    toast.add({
      severity: 'success',
      summary: '승인 완료',
      detail: `이동요청 ${selectedMoveRequestItem.value.moveReqCd}이 승인되었습니다.`,
      life: 3000
    });
    
    // 승인 후 데이터 갱신
    await loadInitialData();
    initializeApproverData();
    
  } catch (error) {
    console.error('승인 처리 실패:', error);
    toast.add({
      severity: 'error',
      summary: '승인 실패',
      detail: '승인 처리 중 오류가 발생했습니다.',
      life: 3000
    });
  }
};

// 거절 처리
const handleReject = async () => {
  if (!selectedMoveRequestItem.value) {
    toast.add({
      severity: 'warn',
      summary: '선택 필요',
      detail: '거절할 이동요청을 선택해주세요.',
      life: 3000
    });
    return;
  }

  if (selectedMoveRequestItem.value.moveStatus !== 'd1') {
    toast.add({
      severity: 'warn',
      summary: '거절 불가',
      detail: '요청 상태의 이동요청만 거절할 수 있습니다.',
      life: 3000
    });
    return;
  }

  if (!approverData.value.rejectionReason?.trim()) {
    toast.add({
      severity: 'warn',
      summary: '거절 사유 필요',
      detail: '거절 사유를 입력해주세요.',
      life: 3000
    });
    return;
  }

  try {
    const approver = user.value?.empCd || '';
    
    await stockMovementListStore.rejectMoveRequestById(
      selectedMoveRequestItem.value.moveReqCd,
      approver,
      approverData.value.rejectionReason
    );
    
    toast.add({
      severity: 'info',
      summary: '거절 완료',
      detail: `이동요청 ${selectedMoveRequestItem.value.moveReqCd}이 거절되었습니다.`,
      life: 3000
    });
    
    // 거절 후 데이터 갱신
    await loadInitialData();
    initializeApproverData();
    
  } catch (error) {
    console.error('거절 처리 실패:', error);
    toast.add({
      severity: 'error',
      summary: '거절 실패',
      detail: '거절 처리 중 오류가 발생했습니다.',
      life: 3000
    });
  }
};

// 날짜 포맷 함수
const formatDate = (date) => {
  if (!date) return '';
  const d = new Date(date);
  if (isNaN(d.getTime())) return '';
  
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  
  return `${year}-${month}-${day}`;
};

// 승인 가능 여부 확인
const canApprove = computed(() => {
  return selectedMoveRequestItem.value && 
         selectedMoveRequestItem.value.moveStatus === 'd1' &&
         user.value?.empCd;
});

// 거절 가능 여부 확인
const canReject = computed(() => {
  return selectedMoveRequestItem.value && 
         selectedMoveRequestItem.value.moveStatus === 'd1' &&
         user.value?.empCd;
});

// 상태 변화 모니터링을 위한 watchers
watch(selectedMoveRequest, (newValue) => {
  console.log('selectedMoveRequest changed:', newValue);
}, { deep: true });

watch(moveRequestDetails, (newValue) => {
  console.log('moveRequestDetails changed:', newValue);
}, { deep: true });

watch(selectedMoveRequestInfo, (newValue) => {
  console.log('selectedMoveRequestInfo changed:', newValue);
}, { deep: true });

// 승인자 데이터 변경 처리
// ========== 결합된 상세/승인 테이블 세팅 ==========
// 이동요청 상세 + 승인자 필드를 하나의 LeftAlignTable 로 표기
const combinedFields = computed(() => {
  return [...moveRequestFields.value, ...approverFields.value];
});

// 결합 데이터 (요청 상세는 읽기 전용, 승인자 관련 필드만 갱신)
const combinedData = computed(() => ({
  ...selectedMoveRequestInfo.value,
  ...approverData.value
}));

// 결합 테이블 update 이벤트 처리
const handleCombinedDataChange = (newData) => {
  if (!newData) return;
  const { approverName, rejectionReason } = newData;
  approverData.value = {
    ...approverData.value,
    approverName: approverName ?? approverData.value.approverName,
    rejectionReason: rejectionReason ?? approverData.value.rejectionReason
  };
};
</script>

<template>
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- 왼쪽: 검색 및 목록 -->
        <div class="space-y-4">
            <!-- 검색 폼 -->
            <div class="space-y-4 mb-2">
                <SearchForm 
                    :columns="searchColumns"
                    @search="handleSearch"
                    @reset="handleReset"
                    :gridColumns="2"
                />
            </div>

            <!-- 이동요청 목록 -->
            <div class="card">
                <BasicTable
                    :title="`이동요청 목록 (총 ${filteredMoveRequestList.length}건)`"
                    :data="filteredMoveRequestList"
                    :columns="moveRequestColumns"
                    :selection="selectedMoveRequestItem ? [selectedMoveRequestItem] : []"
                    selectionMode="single"
                    dataKey="moveReqCd"
                    scrollHeight="500px"
                    @update:selection="handleRowSelection"
                />
            </div>
        </div>

        <!-- 오른쪽: 상세 정보 및 승인 -->
        <div class="space-y-4">
            <!-- 이동요청 상세 + 승인 처리 (단일 테이블로 통합) -->
            <div class="card space-y-6">
              <LeftAlignTable
                title="이동요청 상세 / 승인"
                :data="combinedData"
                :fields="combinedFields"
                :buttons="detailFormButtons"
                dataKey="moveReqCd"
                @update:data="handleCombinedDataChange"
              />
              <div class="flex gap-3 justify-end">
                <Button
                  label="승인"
                  severity="success"
                  icon="pi pi-check"
                  :disabled="!canApprove"
                  @click="handleApprove"
                />
                <Button
                  label="거절"
                  severity="danger"
                  icon="pi pi-times"
                  :disabled="!canReject"
                  @click="handleReject"
                />
              </div>
            </div>

            <!-- 요청상세 품목 목록 -->
            <div class="card">
                <BasicTable
                    title="요청상세 품목 목록"
                    :data="moveRequestDetails"
                    :columns="moveRequestDetailColumns"
                    :selection="[]"
                    selectionMode="none"
                    dataKey="mrdcode"
                    scrollHeight="300px"
                />
            </div>
        </div>
    </div>


</template>

<style scoped>

</style>

