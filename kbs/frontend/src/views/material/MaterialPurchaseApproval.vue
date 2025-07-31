<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue';
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
import LeftAlignTable from '@/components/kimbap/table/LeftAlignTable.vue';
import Toast from 'primevue/toast';
import Button from 'primevue/button';
import Dialog from 'primevue/dialog';
import Textarea from 'primevue/textarea';

// API 함수들 import
import { 
  getPurcOrderWithDetails,
  updatePurchaseOrderStatus 
} from '@/api/materials';

// Store들
const materialStore = useMaterialStore();
const memberStore = useMemberStore();
const commonStore = useCommonStore();
const route = useRoute();
const router = useRouter();
const toast = useToast();

// 🎯 Store에서 반응형 데이터 가져오기 (storeToRefs 사용!)
const { 
  approvalOrderHeader,
  approvalOrderDetails, 
  selectedApprovalItems 
} = storeToRefs(materialStore);

// 🐛 디버그: 선택 상태 감시
watch(selectedApprovalItems, (newVal, oldVal) => {
  console.log('🐛 selectedApprovalItems 변경됨!');
  console.log('  - 이전값:', oldVal?.length || 0, '개');
  console.log('  - 새값:', newVal?.length || 0, '개');
  console.log('  - 상세:', newVal);
}, { deep: true });

//🎯 반응형 데이터들
const isLoading = ref(false);
const purcCd = ref('');

// 🐛 로컬 선택 데이터 (InputTable 내부 구조에 맞춤!)
const localSelectedItems = ref([]);

// 🎯 더 간단한 방법: 버튼 클릭 시 직접 InputTable에서 선택 상태 가져오기!
const getSelectedItemsFromTable = () => {
  try {
    // InputTable 컴포넌트의 내부 선택 상태에 접근
    const tableComponent = inputTableRef.value;
    if (tableComponent) {
      // InputTable 내부의 selectedRows 접근
      const selectedRows = tableComponent.selectedRows || [];
      console.log('🐛 테이블에서 직접 가져온 선택:', selectedRows?.length || 0, '개');
      console.log('🐛 선택된 데이터:', selectedRows);
      return selectedRows;
    }
    return [];
  } catch (error) {
    console.error('🐛 선택 상태 가져오기 실패:', error);
    return [];
  }
};

// 🎯 InputTable 참조
const inputTableRef = ref(null);

// 반려 사유 모달 관련
const rejectModalVisible = ref(false);
const rejectReason = ref('');
const selectedRejectItems = ref([]);

// 🎨 기본정보 필드 설정 (readonly로!)
const basicInfoFields = ref([
  { field: 'purcCd', label: '발주번호', type: 'input', readonly: true },
  { field: 'ordDt', label: '주문일자', type: 'input', readonly: true },
  { field: 'regi', label: '등록자', type: 'input', readonly: true },
  { field: 'purcStatus', label: '발주상태', type: 'input', readonly: true },
  { field: 'ordTotalAmount', label: '총 발주금액', type: 'input', readonly: true },
  { field: 'approver', label: '승인자', type: 'input', readonly: true }
]);

// 🎨 상세정보 테이블 컬럼 설정
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
    field: 'purcDStatus',
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

// 🎨 테이블 버튼 설정 (승인/반려 버튼만!)
const tableButtons = ref({
  save: { show: false },
  reset: { show: false },
  delete: { show: false },
  load: { show: false }
});

// 🔧 날짜 포맷팅 함수
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

// 🔧 상태 텍스트 변환
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

// 🔧 단위 텍스트 변환
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

// 💰 총 승인 대상 금액 계산 (로컬 데이터 사용!)
const totalApprovalAmount = computed(() => {
  console.log('🐛 totalApprovalAmount 계산:', localSelectedItems.value?.length || 0, '개');
  return localSelectedItems.value.reduce((sum, item) => {
    return sum + (item.totalAmount || 0);
  }, 0);
});

// ✅ 승인 가능 여부 체크 (로컬 데이터 사용!)
const canApprove = computed(() => {
  const result = localSelectedItems.value.length > 0 && 
         localSelectedItems.value.every(item => item.purcDStatus === 'c1');
  console.log('🐛 canApprove 계산:', result, '(선택:', localSelectedItems.value.length, '개)');
  return result;
});

// ❌ 반려 가능 여부 체크 (로컬 데이터 사용!)
const canReject = computed(() => {
  const result = localSelectedItems.value.length > 0 && 
         localSelectedItems.value.every(item => item.purcDStatus === 'c1');
  console.log('🐛 canReject 계산:', result, '(선택:', localSelectedItems.value.length, '개)');
  return result;
});

// 📋 발주 상세 정보 로드 (핵심 함수!)
const loadOrderDetails = async (orderCode) => {
  try {
    isLoading.value = true;
    console.log('🔍 발주 상세 정보 로드 시작:', orderCode);
    
    const response = await getPurcOrderWithDetails(orderCode);
    console.log('📄 API 응답 데이터:', response.data);
    
    if (response.data && response.data.header && response.data.details) {
      const { header, details } = response.data;
      
      // 🎯 헤더 정보 설정 (store에 저장!)
      materialStore.setApprovalOrderHeader({
        purcCd: header.purcCd,
        ordDt: formatDate(header.ordDt),
        regi: header.regi || '등록자명',
        purcStatus: getStatusText(header.purcStatus),
        ordTotalAmount: header.ordTotalAmount ? 
          `${Number(header.ordTotalAmount).toLocaleString()}원` : '0원',
        approver: memberStore.user?.empName || '현재 로그인 사용자'
      });
      
      // 🎯 상세 정보 설정 (store에 저장!)
      const detailsData = details.map((item, index) => ({
        // 🐛 dataKey 확인: purcDCd를 고유 식별자로 사용
        purcDCd: item.purcDCd,  // 이게 dataKey!
        id: `detail_${index + 1}`,
        mateName: item.mateName,
        cpName: item.cpName,
        purcQty: item.purcQty,
        unit: getUnitText(item.unit),
        unitPrice: Number(item.unitPrice || 0).toLocaleString(),
        totalAmount: Number(item.totalAmount || 0),
        exDeliDt: formatDate(item.exDeliDt),
        purcDStatus: item.purcDStatus,
        purcDStatusText: getStatusText(item.purcDStatus),
        note: item.note || '',
        // 원본 데이터도 보존
        _original: item
      }));
      
      materialStore.setApprovalOrderDetails(detailsData);
      
      // 🐛 데이터 로드 후 선택 초기화
      localSelectedItems.value = [];
      
      console.log('✅ 발주 정보 로드 완료!', {
        header: approvalOrderHeader.value,
        detailCount: approvalOrderDetails.value.length,
        firstDetailKey: detailsData[0]?.purcDCd  // 🐛 dataKey 확인용
      });
      
      toast.add({
        severity: 'success',
        summary: '로드 완료! 🎉',
        detail: `발주서 ${orderCode} 정보를 성공적으로 불러왔어! (${details.length}건)`,
        life: 3000
      });
      
    } else {
      throw new Error('발주서 데이터 구조가 올바르지 않아요 ㅠㅠ');
    }
    
  } catch (error) {
    console.error('❌ 발주 정보 로드 실패:', error);
    
    if (error.response?.status === 404) {
      toast.add({
        severity: 'warn',
        summary: '발주서 없음 😢',
        detail: `발주번호 ${orderCode}에 해당하는 발주서를 찾을 수 없어!`,
        life: 4000
      });
      
      // 목록으로 돌아가기
      setTimeout(() => {
        router.push('/material/MaterialPurchaseView');
      }, 2000);
      
    } else {
      toast.add({
        severity: 'error',
        summary: '로드 실패 ㅠㅠ',
        detail: '발주서 정보를 불러오는 중 문제가 생겼어! 다시 시도해줘~',
        life: 3000
      });
      
      // 샘플 데이터로 대체
      loadSampleData(orderCode);
    }
  } finally {
    isLoading.value = false;
  }
};

// 🧪 샘플 데이터 로드 (API 실패 시 대비)
const loadSampleData = (orderCode) => {
  console.log('🧪 샘플 데이터 로드:', orderCode);
  
  // Store에 샘플 헤더 데이터 저장
  materialStore.setApprovalOrderHeader({
    purcCd: orderCode || 'PURC-001',
    ordDt: '2025-07-29',
    regi: '김구매',
    purcStatus: '승인 대기',
    ordTotalAmount: '1,500,000원',
    approver: memberStore.user?.empName || '김승인'
  });
  
  // Store에 샘플 상세 데이터 저장
  materialStore.setApprovalOrderDetails([
    {
      // 🐛 dataKey 확인: purcDCd를 고유 식별자로 사용
      purcDCd: 'PURC-D-001',  // 이게 dataKey!
      id: 'detail_1',
      mateName: '김(건조)',
      cpName: '황금쌀농협',
      purcQty: 100,
      unit: 'kg',
      unitPrice: '15,000',
      totalAmount: 1500000,
      exDeliDt: '2025-08-01',
      purcDStatus: 'c1',
      purcDStatusText: '요청',
      note: '긴급 발주 건',
      _original: {
        purcDCd: 'PURC-D-001',
        purcCd: orderCode || 'PURC-001'
      }
    }
  ]);
  
  // 🐛 샘플 데이터 로드 후 선택 초기화
  localSelectedItems.value = [];
  
  toast.add({
    severity: 'info',
    summary: '샘플 데이터 로드 📋',
    detail: '서버 연결이 안 되어서 샘플 데이터로 보여줄게!',
    life: 3000
  });
};

// ✅ 승인 처리 함수 (실시간 선택 상태 가져오기!)
const handleApprove = async () => {
  // 🎯 버튼 클릭 시 실시간으로 선택 상태 가져오기!
  const currentSelection = getSelectedItemsFromTable();
  localSelectedItems.value = currentSelection;
  
  console.log('🐛 승인 처리 시작 - 실시간 선택:', localSelectedItems.value?.length || 0, '개');
  console.log('🐛 선택된 항목들:', localSelectedItems.value);
  
  const canApproveNow = localSelectedItems.value.length > 0 && 
                       localSelectedItems.value.every(item => item.purcDStatus === 'c1');
  
  if (!canApproveNow) {
    toast.add({
      severity: 'warn',
      summary: '승인 불가 ⚠️',
      detail: '승인할 항목을 선택하거나, 이미 처리된 항목은 승인할 수 없어!',
      life: 3000
    });
    return;
  }
  
  try {
    isLoading.value = true;
    console.log('✅ 승인 처리 시작:', localSelectedItems.value.length, '건');
    
    // 선택된 각 발주상세에 대해 승인 처리
    for (const detail of localSelectedItems.value) {
      const statusData = {
        purcDCd: detail.purcDCd,
        purcCd: detail._original.purcCd,
        purcDStatus: 'c2', // 승인
        note: `${memberStore.user?.empName || '시스템'}에 의해 승인됨`
      };
      
      console.log('🐛 승인 API 호출:', statusData);
      await updatePurchaseOrderStatus(statusData);
      console.log(`✅ ${detail.purcDCd} 승인 완료!`);
    }
    
    toast.add({
      severity: 'success',
      summary: '승인 완료! 🎉',
      detail: `${localSelectedItems.value.length}건의 발주가 승인되었어! 완전 굿~! 👏`,
      life: 4000
    });
    
    // 선택 초기화 및 데이터 새로고침
    localSelectedItems.value = [];
    await loadOrderDetails(purcCd.value);
    
  } catch (error) {
    console.error('❌ 승인 처리 실패:', error);
    toast.add({
      severity: 'error',
      summary: '승인 실패 ㅠㅠ',
      detail: '승인 처리 중 문제가 생겼어! 다시 시도해줘~',
      life: 3000
    });
  } finally {
    isLoading.value = false;
  }
};

// ❌ 반려 모달 열기 (실시간 선택 상태 가져오기!)
const openRejectModal = () => {
  // 🎯 버튼 클릭 시 실시간으로 선택 상태 가져오기!
  const currentSelection = getSelectedItemsFromTable();
  localSelectedItems.value = currentSelection;
  
  console.log('🐛 반려 모달 열기 - 실시간 선택:', localSelectedItems.value?.length || 0, '개');
  console.log('🐛 선택된 항목들:', localSelectedItems.value);
  
  const canRejectNow = localSelectedItems.value.length > 0 && 
                      localSelectedItems.value.every(item => item.purcDStatus === 'c1');
  
  if (!canRejectNow) {
    toast.add({
      severity: 'warn',
      summary: '반려 불가 ⚠️',
      detail: '반려할 항목을 선택하거나, 이미 처리된 항목은 반려할 수 없어!',
      life: 3000
    });
    return;
  }
  
  selectedRejectItems.value = [...localSelectedItems.value];
  rejectReason.value = '';
  rejectModalVisible.value = true;
};

// ❌ 반려 처리 함수
const handleReject = async () => {
  if (!rejectReason.value.trim()) {
    toast.add({
      severity: 'warn',
      summary: '반려 사유 필요 📝',
      detail: '반려 사유를 입력해줘야 해!',
      life: 3000
    });
    return;
  }
  
  try {
    isLoading.value = true;
    console.log('❌ 반려 처리 시작:', selectedRejectItems.value.length, '건');
    
    // 선택된 각 발주상세에 대해 반려 처리
    for (const detail of selectedRejectItems.value) {
      const statusData = {
        purcDCd: detail.purcDCd,
        purcCd: detail._original.purcCd,
        purcDStatus: 'c6', // 거절
        note: `반려사유: ${rejectReason.value} (반려자: ${memberStore.user?.empName || '시스템'})`
      };
      
      await updatePurchaseOrderStatus(statusData);
      console.log(`❌ ${detail.purcDCd} 반려 완료!`);
    }
    
    toast.add({
      severity: 'info',
      summary: '반려 완료 📋',
      detail: `${selectedRejectItems.value.length}건의 발주가 반려되었어.`,
      life: 4000
    });
    
    // 모달 닫기 및 데이터 새로고침
    rejectModalVisible.value = false;
    await loadOrderDetails(purcCd.value);
    localSelectedItems.value = []; // 로컬 선택 초기화!
    materialStore.clearSelectedApprovalItems(); // store 초기화!
    selectedRejectItems.value = [];
    rejectReason.value = '';
    
  } catch (error) {
    console.error('❌ 반려 처리 실패:', error);
    toast.add({
      severity: 'error',
      summary: '반려 실패 ㅠㅠ',
      detail: '반려 처리 중 문제가 생겼어! 다시 시도해줘~',
      life: 3000
    });
  } finally {
    isLoading.value = false;
  }
};

// 🔙 목록으로 돌아가기
const goBackToList = () => {
  router.push('/material/MaterialPurchaseView');
};

// 🎯 초기화 (컴포넌트 마운트 시)
onMounted(async () => {
  console.log('🚀 MaterialPurchaseApproval 마운트됨!');
  
  // URL에서 purcCd 파라미터 가져오기
  purcCd.value = route.query.purcCd || route.params.purcCd || '';
  
  if (!purcCd.value) {
    toast.add({
      severity: 'warn',
      summary: '발주번호 없음 😅',
      detail: '발주번호가 없어서 목록으로 돌아갈게!',
      life: 3000
    });
    
    setTimeout(() => {
      router.push('/material/MaterialPurchaseView');
    }, 2000);
    return;
  }
  
  console.log('📋 처리할 발주번호:', purcCd.value);
  
  // 발주 상세 정보 로드
  await loadOrderDetails(purcCd.value);
});

// 🎯 route 변경 감지 (발주번호가 바뀔 때)
watch(() => route.query.purcCd, (newPurcCd) => {
  if (newPurcCd && newPurcCd !== purcCd.value) {
    purcCd.value = newPurcCd;
    loadOrderDetails(newPurcCd);
  }
});
</script>

<template>
  <div class="p-4">
    <Toast />
    
    <!-- 페이지 헤더 -->
    <div class="mb-6 flex justify-between items-center">
      <div>
        <h1 class="text-3xl font-bold text-gray-800 mb-2">발주 승인/반려 처리</h1>
        <p class="text-gray-600">
          {{ approvalOrderHeader.purcCd || '발주번호 로딩중...' }} 
          <span class="mx-2">|</span>
          👤 {{ memberStore.user?.empName || '김승인' }}
          <span class="mx-2">|</span>
          🏢 {{ memberStore.user?.deptName || '구매승인팀' }}
        </p>
      </div>
      
      <Button 
        label="목록으로 돌아가기" 
        icon="pi pi-arrow-left" 
        severity="secondary"
        @click="goBackToList"
        :disabled="isLoading"
      />
    </div>

    <!-- 📋 발주 기본정보 -->
    <div class="mb-6">
      <LeftAlignTable
        :data="approvalOrderHeader"
        :fields="basicInfoFields"
        title="발주 기본정보"
      />
    </div>

    <!-- 💰 승인 요약 정보 (선택된 항목이 있을 때만) -->
    <div v-if="localSelectedItems.length > 0" 
         class="mb-6 p-4 bg-blue-50 rounded-lg border border-blue-200">
      <h3 class="text-lg font-semibold text-blue-800 mb-2">승인 처리 요약</h3>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
        <div>
          <span class="text-gray-600">선택된 항목:</span>
          <span class="font-bold ml-2">{{ localSelectedItems.length }}건</span>
        </div>
        <div>
          <span class="text-gray-600">총 승인 금액:</span>
          <span class="font-bold ml-2 text-blue-600">
            {{ totalApprovalAmount.toLocaleString() }}원
          </span>
        </div>
        <div>
          <span class="text-gray-600">처리 가능:</span>
          <span :class="canApprove ? 'text-green-600 font-bold' : 'text-red-600'" class="ml-2">
            {{ canApprove ? '승인 가능 ✅' : '승인 불가 ❌' }}
          </span>
        </div>
      </div>
      
      <!-- 🐛 디버그 정보 -->
      <div class="mt-3 p-2 bg-yellow-100 rounded text-xs">
        <strong>🐛 디버그:</strong> 
        로컬선택: {{ localSelectedItems.length }}개 | 
        Store선택: {{ selectedApprovalItems.length }}개 |
        dataKey 확인: {{ localSelectedItems[0]?.purcDCd || 'N/A' }}
      </div>
    </div>

    <!-- 📊 발주 상세 목록 -->
    <div class="mb-6">
      <InputTable
        ref="inputTableRef"
        :columns="detailTableColumns"
        :data="approvalOrderDetails"
        :scroll-height="'40vh'"
        :height="'50vh'"
        title="발주 상세 목록"
        dataKey="purcDCd"
        :buttons="tableButtons"
        :enableRowActions="false"
        :enableSelection="true"
        selectionMode="multiple"
        :showRowCount="true"
        @dataChange="(newData) => console.log('🐛 InputTable dataChange:', newData)"
      >
        <!-- 🎯 승인/반려 버튼들 -->
        <template #top-buttons>
          <Button 
            label="승인" 
            severity="success" 
            icon="pi pi-check"
            @click="handleApprove"
            :disabled="!canApprove || isLoading"
            :loading="isLoading"
          />
          <Button 
            label="반려" 
            severity="danger" 
            icon="pi pi-times"
            @click="openRejectModal"
            :disabled="!canReject || isLoading"
          />
        </template>
      </InputTable>
    </div>

    <!-- ❌ 반려 사유 입력 모달 -->
    <Dialog 
      v-model:visible="rejectModalVisible"
      modal
      header="반려 사유 입력"
      :style="{ width: '500px' }"
      :closable="false"
    >
      <div class="mb-4">
        <h4 class="mb-2">반려할 발주 상세 ({{ selectedRejectItems.length }}건)</h4>
        <div class="max-h-32 overflow-y-auto bg-gray-50 p-3 rounded">
          <div v-for="item in selectedRejectItems" :key="item.purcDCd" 
               class="text-sm mb-1">
            • {{ item.purcDCd }} - {{ item.mateName }}
          </div>
        </div>
      </div>
      
      <div class="mb-4">
        <label for="rejectReason" class="block text-sm font-medium mb-2">
          반려 사유 <span class="text-red-500">*</span>
        </label>
        <Textarea 
          id="rejectReason"
          v-model="rejectReason"
          rows="4"
          cols="50"
          placeholder="반려 사유를 상세히 입력해주세요..."
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
          label="반려 처리" 
          severity="danger" 
          @click="handleReject"
          :disabled="!rejectReason.trim() || isLoading"
          :loading="isLoading"
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
</style>