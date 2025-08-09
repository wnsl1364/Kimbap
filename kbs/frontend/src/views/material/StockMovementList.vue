<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { storeToRefs } from 'pinia';
import { useStockMovementListStore } from '@/stores/stockMovementListStore';
import { useMemberStore } from '@/stores/memberStore';
import { useToast } from 'primevue/usetoast';
import InputForm from '@/components/kimbap/searchform/inputForm.vue';
import BasicTable from '@/components/kimbap/table/BasicTable.vue';
import LeftAlignTable from '@/components/kimbap/table/LeftAlignTable.vue';
import Button from 'primevue/button';
import Dialog from 'primevue/dialog';
import Textarea from 'primevue/textarea';

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
const searchFormData = ref({});
const selectedMoveRequestItem = ref(null);
const approverData = ref({});
const rejectDialogVisible = ref(false);
const rejectionReason = ref('');

// 검색 폼 버튼 설정
const searchFormButtons = computed(() => ({
  save: { show: true, label: '검색', severity: 'primary' },
  reset: { show: true, label: '초기화', severity: 'secondary' },
  delete: { show: false },
  load: { show: false }
}));

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
    approvalComment: '',
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
    
    // 날짜 범위 처리
    if (searchData.reqDt && Array.isArray(searchData.reqDt)) {
      searchData.startDate = formatDate(searchData.reqDt[0]);
      searchData.endDate = formatDate(searchData.reqDt[1]);
      delete searchData.reqDt;
    }
    
    stockMovementListStore.setSearchFilter(searchData);
    
    if (Object.keys(searchData).some(key => searchData[key])) {
      await stockMovementListStore.searchMoveRequests(searchData);
    } else {
      await stockMovementListStore.fetchMoveRequestList();
    }
    
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
    searchFormData.value = {};
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
    const comment = approverData.value.approvalComment || '';
    
    await stockMovementListStore.approveMoveRequestById(
      selectedMoveRequestItem.value.moveReqCd,
      approver,
      comment
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

// 거절 다이얼로그 열기
const openRejectDialog = () => {
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

  rejectionReason.value = '';
  rejectDialogVisible.value = true;
};

// 거절 처리
const handleReject = async () => {
  if (!rejectionReason.value.trim()) {
    toast.add({
      severity: 'warn',
      summary: '입력 필요',
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
      rejectionReason.value
    );
    
    toast.add({
      severity: 'info',
      summary: '거절 완료',
      detail: `이동요청 ${selectedMoveRequestItem.value.moveReqCd}이 거절되었습니다.`,
      life: 3000
    });
    
    rejectDialogVisible.value = false;
    rejectionReason.value = '';
    
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

// 거절 다이얼로그 닫기
const closeRejectDialog = () => {
  rejectDialogVisible.value = false;
  rejectionReason.value = '';
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
const handleApproverDataChange = (newData) => {
  approverData.value = { ...approverData.value, ...newData };
};

// 검색 폼 데이터 변경 처리
const handleSearchFormDataChange = (newData) => {
  searchFormData.value = { ...searchFormData.value, ...newData };
};
</script>

<template>
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- 왼쪽: 검색 및 목록 -->
        <div class="space-y-4">
            <!-- 검색 폼 -->
            <div class="card">
                <InputForm
                    title="이동요청 검색"
                    :columns="searchColumns"
                    :data="searchFormData"
                    :buttons="searchFormButtons"
                    @submit="handleSearch"
                    @reset="handleReset"
                    @update:data="handleSearchFormDataChange"
                />
            </div>

            <!-- 이동요청 목록 -->
            <div class="card">
                <BasicTable
                    title="이동요청 목록"
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
            <!-- 이동요청 상세 정보 -->
            <div class="card">
                <LeftAlignTable
                    title="이동요청 상세정보"
                    :data="selectedMoveRequestInfo"
                    :fields="moveRequestFields"
                    :buttons="detailFormButtons"
                    dataKey="moveReqCd"
                />
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

            <!-- 승인자 정보 및 버튼 -->
            <div class="card">
                <div class="space-y-4">
                    <h3 class="text-lg font-semibold text-gray-800">승인 처리</h3>
                    
                    <!-- 승인자 정보 -->
                    <LeftAlignTable
                        :data="approverData"
                        :fields="approverFields.slice(0, 2)"
                        :buttons="detailFormButtons"
                        dataKey="approverName"
                        @update:data="handleApproverDataChange"
                    />
                    
                    <!-- 승인/거절 버튼 -->
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
                            @click="openRejectDialog"
                        />
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- 거절 사유 입력 다이얼로그 -->
    <Dialog
        v-model:visible="rejectDialogVisible"
        modal
        header="이동요청 거절"
        :style="{ width: '500px' }"
        :closable="true"
        @hide="closeRejectDialog"
    >
        <div class="space-y-4">
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">
                    거절 사유 <span class="text-red-500">*</span>
                </label>
                <Textarea
                    v-model="rejectionReason"
                    rows="4"
                    cols="50"
                    placeholder="거절 사유를 입력해주세요."
                    class="w-full"
                    :class="{ 'p-invalid': !rejectionReason.trim() }"
                />
            </div>
        </div>
        
        <template #footer>
            <div class="flex gap-2 justify-end">
                <Button
                    label="취소"
                    severity="secondary"
                    @click="closeRejectDialog"
                />
                <Button
                    label="거절 처리"
                    severity="danger"
                    @click="handleReject"
                    :disabled="!rejectionReason.trim()"
                />
            </div>
        </template>
    </Dialog>
</template>

<style scoped>
.card {
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    padding: 1.5rem;
}

.space-y-4 > :not([hidden]) ~ :not([hidden]) {
    margin-top: 1rem;
}

.space-y-6 > :not([hidden]) ~ :not([hidden]) {
    margin-top: 1.5rem;
}

.grid {
    display: grid;
}

.grid-cols-1 {
    grid-template-columns: repeat(1, minmax(0, 1fr));
}

.gap-6 {
    gap: 1.5rem;
}

.gap-3 {
    gap: 0.75rem;
}

@media (min-width: 1024px) {
    .lg\:grid-cols-2 {
        grid-template-columns: repeat(2, minmax(0, 1fr));
    }
}

.flex {
    display: flex;
}

.justify-end {
    justify-content: flex-end;
}

.text-lg {
    font-size: 1.125rem;
    line-height: 1.75rem;
}

.font-semibold {
    font-weight: 600;
}

.text-gray-800 {
    color: #1f2937;
}

.text-gray-700 {
    color: #374151;
}

.text-red-500 {
    color: #ef4444;
}

.text-sm {
    font-size: 0.875rem;
    line-height: 1.25rem;
}

.font-medium {
    font-weight: 500;
}

.block {
    display: block;
}

.mb-2 {
    margin-bottom: 0.5rem;
}

.w-full {
    width: 100%;
}
</style>

