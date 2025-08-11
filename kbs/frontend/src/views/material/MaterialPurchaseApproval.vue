<script setup>
import { ref, computed, onMounted, watch, nextTick, onUnmounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useToast } from 'primevue/usetoast';
import { useMaterialStore } from '@/stores/materialStore';
import { useMemberStore } from '@/stores/memberStore';
import { useCommonStore } from '@/stores/commonStore';
import { storeToRefs } from 'pinia';
import { format, isValid } from 'date-fns';

// 컴포넌트 import
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import InputTable from '@/components/kimbap/table/InputTable.vue';
import Toast from 'primevue/toast';
import Button from 'primevue/button';
import Dialog from 'primevue/dialog';
import Textarea from 'primevue/textarea';

// API 함수들 import
import { 
  getPurcOrderWithDetails,
  updatePurchaseOrderStatus,
  bulkApprovePurchaseOrders,
  bulkRejectPurchaseOrders
} from '@/api/materials';

// Store들
const materialStore = useMaterialStore();
const memberStore = useMemberStore();
const commonStore = useCommonStore();
const route = useRoute();
const router = useRouter();
const toast = useToast();

// Store에서 반응형 데이터 가져오기
const { 
  approvalOrderHeader,
  approvalOrderDetails, 
  selectedApprovalItems 
} = storeToRefs(materialStore);

//반응형 데이터들
const isLoading = ref(false);
const purcCd = ref('');

// 선택 상태 관리
const localSelectedItems = ref([]);

// 임시 상태 변경 관리
const pendingChanges = ref([]); // 저장 대기 중인 변경사항들
const hasUnsavedChanges = computed(() => pendingChanges.value.length > 0);

// InputTable에서 선택 상태 변경 시 호출되는 핸들러
const handleSelectionChange = (selectedItems) => {  
  localSelectedItems.value = [...selectedItems]
  materialStore.setSelectedApprovalItems([...selectedItems])
}

// InputTable 참조
const inputTableRef = ref(null);

// 거절 사유 모달 관련
const rejectModalVisible = ref(false);
const rejectReason = ref('');

// SearchForm로 표시할 기본정보 컬럼 (읽기전용)
const basicInfoColumns = computed(() => [
  { key: 'purcCd', label: '발주번호', type: 'readonly', value: approvalOrderHeader.value.purcCd || '' },
  { key: 'ordDt', label: '주문일자', type: 'readonly', value: approvalOrderHeader.value.ordDt || '' },
  { key: 'regi', label: '등록자', type: 'readonly', value: approvalOrderHeader.value.empName || approvalOrderHeader.value.regi || '' },
  { key: 'purcStatus', label: '발주상태', type: 'readonly', value: approvalOrderHeader.value.purcStatus || '' },
  { key: 'ordTotalAmount', label: '총 발주금액', type: 'readonly', value: approvalOrderHeader.value.ordTotalAmount || '' },
  { key: 'approver', label: '승인자', type: 'readonly', value: approvalOrderHeader.value.approver || '' }
]);

// 테이블에 안전하게 전달할 데이터
const tableData = computed(() => Array.isArray(approvalOrderDetails.value) ? approvalOrderDetails.value : []);

// 상세정보 테이블 컬럼 설정
const detailTableColumns = computed(() => [
  {
    field: 'purcDCd',
    header: '발주상세번호',
    type: 'readonly',
    align: 'center'
  },
  {
    field: 'mateName',
    header: '자재명',
    type: 'readonly',
    align: 'left'
  },
  {
    field: 'cpName',
    header: '공급업체',
    type: 'readonly',
    align: 'left'
  },
  {
    field: 'purcQty',
    header: '발주수량',
    type: 'readonly',
    align: 'right'
  },
  {
    field: 'unit',
    header: '단위',
    type: 'readonly',
    align: 'center'
  },
  {
    field: 'unitPrice',
    header: '단가(원)',
    type: 'readonly',
    align: 'right'
  },
  {
    field: 'totalAmount',
    header: '총액(원)',
    type: 'readonly',
    align: 'right'
  },
  {
    field: 'exDeliDt',
    header: '납기예정일',
    type: 'readonly',
    align: 'center'
  },
  {
    field: 'purcDStatusText',
    header: '상태',
    type: 'readonly',
    align: 'center'
  },
  {
    field: 'note',
    header: '비고',
    type: 'readonly',
    align: 'left'
  }
]);

// 테이블 버튼 설정 (저장 버튼 제거, 즉시처리 버튼만 사용)
const tableButtons = ref({
  save: { show: false },
  reset: { show: false },
  delete: { show: false },
  load: { show: false }
});

// 헬퍼 함수들
const formatDate = (date) => {
  if (!date) return '';
  try {
    const dateObj = date instanceof Date ? date : new Date(date);
    if (!isValid(dateObj)) return '';
    return format(dateObj, 'yyyy-MM-dd');
  } catch (error) {
    console.error('날짜 포맷 에러:', error);
    return '';
  }
};

const getStatusText = (statusCode) => {
  const statusMap = {
    'c1': '요청',
    'c2': '승인', 
    'c3': '입고대기',
    'c4': '부분입고',
    'c5': '입고완료',
    'c6': '거절',
    'c7': '반품'
  };
  return statusMap[statusCode] || statusCode;
};

const getUnitText = (unitCode) => {
  const unitMap = {
    'g1': 'g',
    'g2': 'kg', 
    'g3': 'ml',
    'g4': 'L',
    'g5': 'ea',
    'g6': 'box',
    'g7': 'mm'
  };
  return unitMap[unitCode] || unitCode;
};

// 계산된 값들
const totalApprovalAmount = computed(() => {
  return localSelectedItems.value.reduce((sum, item) => {
    return sum + (item.totalAmount || 0);
  }, 0);
});

const canApprove = computed(() => {
  return localSelectedItems.value.length > 0 && 
         localSelectedItems.value.every(item => 
           item.purcDStatus === 'c1' || item.tempStatus === 'c1'
         );
});

const canReject = computed(() => {
  return localSelectedItems.value.length > 0 && 
         localSelectedItems.value.every(item => 
           item.purcDStatus === 'c1' || item.tempStatus === 'c1'
         );
});

// 발주 상세 정보 로드
const loadOrderDetails = async (orderCode) => {
  try {
    isLoading.value = true;
    console.log('발주 상세 정보 로드 시작:', orderCode);
    
    const response = await getPurcOrderWithDetails(orderCode);
    console.log('API 응답 데이터:', response.data);
    
    if (response.data && response.data.header && response.data.details) {
      const { header, details } = response.data;
      
      // 헤더 정보 설정
      materialStore.setApprovalOrderHeader({
        purcCd: header.purcCd,
        ordDt: formatDate(header.ordDt),
        regi: header.regi || '등록자명',
        purcStatus: getStatusText(header.purcStatus),
        ordTotalAmount: header.ordTotalAmount ? 
          `${Number(header.ordTotalAmount).toLocaleString()}원` : '0원',
        approver: memberStore.user?.cpName || '현재 로그인 사용자'
      });
      
      // 상세 정보 설정 (임시 상태 필드 추가!)
      const detailsData = details.map((item, index) => ({
        purcDCd: item.purcDCd,
        id: `detail_${index + 1}`,
        mateName: item.mateName,
        cpName: item.cpName,
        purcQty: item.purcQty,
        unit: getUnitText(item.unit),
        unitPrice: Number(item.unitPrice || 0).toLocaleString(),
        totalAmount: Number(item.totalAmount || 0),
        exDeliDt: formatDate(item.exDeliDt),
        purcDStatus: item.purcDStatus,  // 원본 상태
        tempStatus: null,  // 임시 상태 (변경사항)
        purcDStatusText: getStatusText(item.purcDStatus),  // 표시용 상태 텍스트
        note: item.note || '',
        _original: item
      }));
      
      materialStore.setApprovalOrderDetails(detailsData);
      
      // 상태 초기화
      localSelectedItems.value = [];
      pendingChanges.value = [];
      
      console.log('발주 정보 불러오기 완료');
      
      toast.add({
        severity: 'success',
        summary: '불러오기 완료',
        detail: `발주서 ${orderCode} 정보를 성공적으로 불러왔습니다. (${details.length}건)`,
        life: 3000
      });
      
    } else {
      throw new Error('발주서 데이터 구조가 올바르지 않습니다.');
    }
    
  } catch (error) {
    console.error('발주 정보 로드 실패:', error);
    loadSampleData(orderCode);
  } finally {
    isLoading.value = false;
  }
};

// 발주 기본정보 초기화
const resetOrderHeader = () => {
  materialStore.setApprovalOrderHeader({
    purcCd: '',
    ordDt: '',
    regi: '',
    purcStatus: '',
    ordTotalAmount: '0원',
    approver: ''
  });
};

// 샘플 데이터 로드
const loadSampleData = (orderCode) => {
  console.log('샘플 데이터 로드:', orderCode);
  
  materialStore.setApprovalOrderHeader({
    purcCd: orderCode || 'PURC-001',
    ordDt: '2025-07-29',
    regi: '김구매',
    purcStatus: '승인 대기',
    ordTotalAmount: '1,500,000원',
    approver: memberStore.user?.empName || '김승인'
  });
  
  materialStore.setApprovalOrderDetails([
    {
      purcDCd: 'PURC-D-001',
      id: 'detail_1',
      mateName: '김(건조)',
      cpName: '황금쌀농협',
      purcQty: 100,
      unit: 'kg',
      unitPrice: '15,000',
      totalAmount: 1500000,
      exDeliDt: '2025-08-01',
      purcDStatus: 'c1',
      tempStatus: null,
      purcDStatusText: '요청',
      note: '긴급 발주 건',
      _original: {
        purcDCd: 'PURC-D-001',
        purcCd: orderCode || 'PURC-001'
      }
    }
  ]);
  
  localSelectedItems.value = [];
  pendingChanges.value = [];
  
  toast.add({
    severity: 'info',
    summary: '샘플 데이터 로드',
    detail: '서버 연결이 되지 않아 샘플 데이터를 불러옵니다.',
    life: 3000
  });
};

// 임시 상태 변경 함수들 (웹페이지에서만 변경!)
const updateTempStatus = (items, newStatus, reason = '') => {
  items.forEach(item => {
    // 원본 데이터 수정 (화면에 바로 반영)
    const originalItem = approvalOrderDetails.value.find(detail => detail.purcDCd === item.purcDCd);
    if (originalItem) {
      originalItem.tempStatus = newStatus;
      originalItem.purcDStatusText = getStatusText(newStatus);
      
      // 거절인 경우 비고에 사유 추가
      if (newStatus === 'c6' && reason) {
        originalItem.note = `거절사유: ${reason}`;
      }
    }
    
    // 변경사항 기록 (저장용)
    const changeIndex = pendingChanges.value.findIndex(change => change.purcDCd === item.purcDCd);
    if (changeIndex >= 0) {
      // 기존 변경사항 업데이트
      pendingChanges.value[changeIndex] = {
        purcDCd: item.purcDCd,
        purcCd: item._original.purcCd,
        newStatus: newStatus,
        reason: reason,
        item: item
      };
    } else {
      // 새 변경사항 추가
      pendingChanges.value.push({
        purcDCd: item.purcDCd,
        purcCd: item._original.purcCd,
        newStatus: newStatus,
        reason: reason,
        item: item
      });
    }
  });
  
  console.log('임시 상태 변경 완료:', pendingChanges.value.length, '건 대기중');
};

// 선택 초기화 (상단 테이블 초기화)
const clearSelection = () => {
  localSelectedItems.value = [];
  if (inputTableRef.value && inputTableRef.value.selectedRows) {
    inputTableRef.value.selectedRows = [];
  }
};

// 승인 즉시 처리
const handleTempApprove = async () => {
  if (!canApprove.value) {
    toast.add({ severity: 'warn', summary: '승인 불가', detail: '승인할 항목을 선택해 주세요.', life: 3000 });
    return;
  }
  try {
    isLoading.value = true;
    const purcDCdList = localSelectedItems.value.map(it => it.purcDCd);
    await bulkApprovePurchaseOrders(purcDCdList, memberStore.user?.empName || 'system');
    toast.add({ severity: 'success', summary: '승인 완료', detail: `${purcDCdList.length}건 승인 처리되었습니다.`, life: 3000 });
    await loadOrderDetails(purcCd.value);
  } catch (e) {
    console.error('승인 실패:', e);
    toast.add({ severity: 'error', summary: '승인 실패', detail: '승인 처리 중 오류가 발생했습니다.', life: 3000 });
  } finally {
    isLoading.value = false;
    clearSelection();
  }
};

// 거절 모달 열기
const openRejectModal = () => {
  if (!canReject.value) {
    toast.add({
      severity: 'warn',
      summary: '거절 불가',
      detail: '거절할 항목을 선택해 주세요.',
      life: 3000
    });
    return;
  }
  
  rejectReason.value = '';
  rejectModalVisible.value = true;
};

// 거절 즉시 처리
const handleTempReject = async () => {
  if (!rejectReason.value.trim()) {
    toast.add({ severity: 'warn', summary: '거절 사유 필요', detail: '거절 사유를 입력해 주세요.', life: 3000 });
    return;
  }
  if (!canReject.value) {
    toast.add({ severity: 'warn', summary: '거절 불가', detail: '거절할 항목을 선택해 주세요.', life: 3000 });
    return;
  }
  try {
    isLoading.value = true;
    const purcDCdList = localSelectedItems.value.map(it => it.purcDCd);
    await bulkRejectPurchaseOrders(purcDCdList, rejectReason.value, memberStore.user?.empName || 'system');
    toast.add({ severity: 'success', summary: '거절 완료', detail: `${purcDCdList.length}건 거절 처리되었습니다.`, life: 3000 });
    await loadOrderDetails(purcCd.value);
  } catch (e) {
    console.error('거절 실패:', e);
    toast.add({ severity: 'error', summary: '거절 실패', detail: '거절 처리 중 오류가 발생했습니다.', life: 3000 });
  } finally {
    isLoading.value = false;
    rejectModalVisible.value = false;
    rejectReason.value = '';
    clearSelection();
  }
};

// 실제 저장 처리! (진짜 API 호출)
const handleSaveChanges = async () => {
  if (!hasUnsavedChanges.value) {
    toast.add({
      severity: 'info',
      summary: '저장할 변경사항 없음',
      detail: '변경된 내용이 없습니다.',
      life: 3000
    });
    return;
  }
  
  try {
    isLoading.value = true;
    console.log('실제 저장 시작:', pendingChanges.value.length, '건');
    
    // 각 변경사항에 대해 실제 API 호출!
    for (const change of pendingChanges.value) {
      const statusData = {
        purcDCd: change.purcDCd,
        purcCd: change.purcCd,
        purcDStatus: change.newStatus,
        note: change.newStatus === 'c6' 
          ? `거절사유: ${change.reason} (거절자: ${memberStore.user?.empName || '시스템'})`
          : `${memberStore.user?.empName || '시스템'}에 의해 ${change.newStatus === 'c2' ? '승인' : '처리'}됨`
      };
      
      console.log('실제 API 호출:', statusData);
      await updatePurchaseOrderStatus(statusData);
      console.log(`${change.purcDCd} 저장 완료`);
    }
    
    toast.add({
      severity: 'success', 
      summary: '저장 완료',
      detail: `${pendingChanges.value.length}건의 변경사항이 성공적으로 저장되었습니다.`,
      life: 4000
    });
    
    // 저장 후 데이터 새로고침
    await loadOrderDetails(purcCd.value);
    
  } catch (error) {
    console.error('저장 실패:', error);
    toast.add({
      severity: 'error',
      summary: '저장 실패',
      detail: '저장 중 문제가 생겼습니다.',
      life: 3000
    });
  } finally {
    isLoading.value = false;
  }
};

// 변경사항 초기화
const handleResetChanges = () => {
  if (!hasUnsavedChanges.value) {
    toast.add({
      severity: 'info',
      summary: '초기화할 변경사항 없음',
      detail: '변경된 내용이 없습니다.',
      life: 3000
    });
    return;
  }
  
  // 모든 임시 변경사항 되돌리기
  pendingChanges.value.forEach(change => {
    const originalItem = approvalOrderDetails.value.find(detail => detail.purcDCd === change.purcDCd);
    if (originalItem) {
      originalItem.tempStatus = null;
      originalItem.purcDStatusText = getStatusText(originalItem.purcDStatus);
      originalItem.note = change.item.note; // 원본 비고로 복원
    }
  });
  
  pendingChanges.value = [];
  localSelectedItems.value = [];
  
  toast.add({
    severity: 'info',
    summary: '변경사항 초기화 완료',
    detail: '모든 임시 변경사항이 초기화되었습니다.',
    life: 3000
  });
};

// 목록으로 돌아가기
const goBackToList = () => {
  router.push('/material/MaterialPurchaseView');
};

// 초기화
onMounted(async () => {
  purcCd.value = route.query.purcCd || route.params.purcCd || '';
  
  if (!purcCd.value) {
    toast.add({
      severity: 'warn',
      summary: '발주번호 없음',
      detail: '발주번호가 없어서 목록으로 돌아갑니다.',
      life: 3000
    });
    
    setTimeout(() => {
      router.push('/material/MaterialPurchaseView');
    }, 2000);
    return;
  }
  
  console.log('처리할 발주번호:', purcCd.value);
  await loadOrderDetails(purcCd.value);
});

// route 변경 감지
watch(() => route.query.purcCd, (newPurcCd) => {
  if (newPurcCd && newPurcCd !== purcCd.value) {
    purcCd.value = newPurcCd;
    loadOrderDetails(newPurcCd);
  }
});

onUnmounted(async () => {  
  // 컴포넌트 언마운트 시 선택 상태 초기화
  resetOrderHeader();
  materialStore.setApprovalOrderDetails([]);
  localSelectedItems.value = [];
  materialStore.setSelectedApprovalItems([]);
  
  // 임시 상태 초기화
  pendingChanges.value = [];
});
</script>

<template>
  <div class="p-4 pb-0">
    <Toast />
    
    <!-- 페이지 헤더 -->
    <div class="mb-6 flex justify-between items-center">
      <div>
        <h1 class="text-3xl font-bold text-gray-800 mb-2">발주 승인/거절 처리</h1>
        <p class="text-gray-600">
          {{ approvalOrderHeader.purcCd || '발주번호 로딩중...' }} 
          <span class="mx-2">|</span>
          👤 {{ memberStore.user?.cpName || '회사이름나와야함' }}
        </p>
      </div>
      
      <div class="flex gap-2">
        <Button 
          label="목록으로 돌아가기" 
          icon="pi pi-arrow-left" 
          severity="secondary"
          @click="goBackToList"
          :disabled="isLoading"
        />
      </div>
    </div>

    <!-- 발주 기본정보 -->
    <div class="mb-6">
      <SearchForm :columns="basicInfoColumns" :gridColumns="3" :showActions="false" />
    </div>

  <!-- 승인 요약 정보 제거됨 -->

  <!-- 저장 대기/변경사항 요약 UI 제거됨: 즉시 처리 정책 적용 -->

    <!-- 발주 상세 목록 -->
    <div class="mb-6">
      <InputTable
        ref="inputTableRef"
        :columns="detailTableColumns"
        :data="tableData"
        :scroll-height="'35vh'"
        :height="'47vh'"
        title="발주 상세 목록"
        dataKey="purcDCd"
        :buttons="tableButtons"
        :enableRowActions="false"
        :enableSelection="true"
        selectionMode="multiple"
        :showRowCount="true"
    @selectionChange="handleSelectionChange"
      >
        <!-- 승인/거절 버튼들 -->
        <template #top-buttons>
          <!-- 선택 요약: 버튼들 왼쪽에 작게 표시 -->
          <div class="flex items-center text-xs md:text-sm text-gray-600 mr-3 whitespace-nowrap">
            <span>선택 {{ localSelectedItems.length }}건</span>
            <span class="mx-2 text-gray-300">|</span>
            <span>금액 {{ totalApprovalAmount.toLocaleString() }}원</span>
          </div>
          <Button 
      label="승인" 
            severity="success" 
            icon="pi pi-check"
            @click="handleTempApprove"
            :disabled="!canApprove || isLoading"
          />
          <Button 
      label="거절" 
            severity="danger" 
            icon="pi pi-times"
            @click="openRejectModal"
            :disabled="!canReject || isLoading"
          />
        </template>
      </InputTable>
    </div>

    <!-- 거절 사유 입력 모달 -->
    <Dialog 
      v-model:visible="rejectModalVisible"
      modal
      header="거절 사유 입력"
      :style="{ width: '500px' }"
      :closable="false"
    >
      <div class="mb-4">
        <h4 class="mb-2">거절할 발주 상세 ({{ localSelectedItems.length }}건)</h4>
        <div class="max-h-32 overflow-y-auto bg-gray-50 p-3 rounded">
          <div v-for="item in localSelectedItems" :key="item.purcDCd" 
               class="text-sm mb-1">
            • {{ item.purcDCd }} - {{ item.mateName }}
          </div>
        </div>
      </div>
      
      <div class="mb-4">
        <label for="rejectReason" class="block text-sm font-medium mb-2">
          거절 사유 <span class="text-red-500">*</span>
        </label>
        <Textarea 
          id="rejectReason"
          v-model="rejectReason"
          rows="4"
          cols="50"
          placeholder="거절 사유를 상세히 입력해주세요..."
          class="w-full"
          :class="{ 'p-invalid': !rejectReason.trim() }"
        />
      </div>
      
      <div class="flex justify-end gap-2">
        <Button 
          label="취소" 
          severity="secondary" 
          @click="rejectModalVisible = false"
          :disabled="isLoading"
        />
        <Button 
          label="거절 처리" 
          severity="danger" 
          @click="handleTempReject"
          :disabled="!rejectReason.trim() || isLoading"
        />
      </div>
    </Dialog>
  </div>
</template>

<style scoped>
:deep(.p-toast) {
  z-index: 9999;
}

:deep(.p-dialog) {
  z-index: 9998;
}

/* 저장 대기 중인 항목 스타일링 */
:deep(.p-datatable-tbody > tr.pending-change) {
  background-color: #fef3c7 !important;
  border-left: 4px solid #f59e0b !important;
}

/* 승인 대기 상태 강조 */
:deep(.status-approved) {
  color: #059669 !important;
  font-weight: bold !important;
}

:deep(.status-rejected) {
  color: #dc2626 !important;
  font-weight: bold !important;
}
</style>