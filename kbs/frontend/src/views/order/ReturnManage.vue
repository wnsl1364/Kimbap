<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useMemberStore } from '@/stores/memberStore';
import { useCommonStore } from '@/stores/commonStore';
import { getReturnList, approveReturn, rejectReturn } from '@/api/return';
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import InputTable from '@/components/kimbap/table/InputTable.vue';
import Dialog from 'primevue/dialog';
import Textarea from 'primevue/textarea';
import Button from 'primevue/button';
import { storeToRefs } from 'pinia';

// 라우터 설정
const router = useRouter();

// 로그인 정보 가져오기
const memberStore = useMemberStore()
const { user } = storeToRefs(memberStore)

const isEmployee = computed(() => user.value?.memType === 'p1')       // 사원
const isCustomer = computed(() => user.value?.memType === 'p2')       // 매출업체
const isSupplier = computed(() => user.value?.memType === 'p3')       // 공급업체
const isManager = computed(() => user.value?.memType === 'p4')        // 담당자
const isAdmin = computed(() => user.value?.memType === 'p5')          // 시스템 관리자

console.log('현재 사용자 권한:', user.value)

// 공통코드
const commonStore = useCommonStore();
const { commonCodes } = storeToRefs(commonStore);

// 공통코드 형변환
const mapReturnStatusLabels = (list) => {
  const returnStatusCodes = commonStore.getCodes('0W');

  return list.map(item => {
    const statusObj = returnStatusCodes.find(code => code.dcd === item.returnStatusInternal);
    return {
      ...item,
      returnStatus: statusObj ? statusObj.cdInfo : item.returnStatusInternal
    };
  });
};

const selectedRows = ref([]);
const returnList = ref([]);

const statusOptions = computed(() => {
  return commonStore.getCodes('0W').map(code => ({
    label: code.cdInfo,
    value: code.dcd
  }));
});

const searchColumns = [
  { key: 'returnDt', label: '요청일자', type: 'dateRange' },
  { key: 'cpName', label: '거래처', type: 'text', placeholder: '거래처명' },
  { key: 'returnStatus', label: '상태', type: 'dropdown', options: statusOptions },
  { key: 'prodName', label: '제품명', type: 'text', placeholder: '제품명' }
];

const returnColumns = [
  { field: 'prodReturnCd', header: '반품코드', type: 'readonly' },
  { field: 'returnDt', header: '요청일자', type: 'readonly' },
  { field: 'cpName', header: '거래처', type: 'readonly' },
  { field: 'prodName', header: '품목명', type: 'readonly' },
  { field: 'returnQty', header: '수량(ea)', type: 'readonly', align: 'right' },
  { field: 'returnStatus', header: '상태', type: 'readonly' },
  { field: 'returnEndDt', header: '처리일자', type: 'readonly' },
  { field: 'managerName', header: '담당자', type: 'readonly' },
  { field: 'returnRea', header: '요청사유', type: 'readonly' },
  {field: 'rejectRea', header: '거절사유', type: 'readonly'},
];

// 버튼 설정
const tableButtons = computed(() => {
  return {
    delete: { show: true, label: '반품거절', severity: 'danger' },
    save: { show: true, label: '반품승인', severity: 'success' }
  }
})

const fetchReturnList = async (params = {}) => {
  try {
    const res = await getReturnList(params);
    returnList.value = mapReturnStatusLabels(res.data.data);
  } catch (err) {
    console.error('반품 목록 조회 실패:', err);
  }
};

const handleSearch = (searchData) => {

  const params = {
    cpName: searchData.cpName || '',
    prodName: searchData.prodName || '',
    returnStatusInternal: searchData.returnStatus || '',
    startDate: searchData.returnDtStart || '',
    endDate: searchData.returnDtEnd || ''
  };

  fetchReturnList(params);
};

const handleReset = () => {
  console.log('검색 조건이 리셋되었습니다');
  handleSearch({});
};

const handleApprove = async () => {
  if (!selectedRows.value.length) {
    alert('처리할 반품 건을 선택하세요.');
    return;
  }

  const confirmed = confirm(`${selectedRows.value.length}건의 반품건을 승인 처리하시겠습니까?`);
  if (!confirmed) return;

  try {
    // 여러 건을 배열로 payload 생성
    const payloadList = selectedRows.value.map(row => ({
      prodReturnCd: row.prodReturnCd,
      manager: user.value.memCd,
      rejectRea: '',
      ordDCd: row.ordDCd,
      returnQty: row.returnQty,
      unitPrice: row.unitPrice
    }));

    await approveReturn(payloadList); // 배열 전송
    alert('승인 처리되었습니다.');
    fetchReturnList();
    selectedRows.value = [];
  } catch (err) {
    console.error('승인 처리 실패:', err);
    alert('승인 처리 중 오류 발생');
  }
};

const handleSelectionChange = (selection) => {
  if (!selection) return;

  if (Array.isArray(selection)) {
    // 승인/거절 불가능한 항목 필터링
    const filtered = selection.filter(item => !['w1', 'w2', 'w3'].includes(item.returnStatusInternal));
    if (filtered.length < selection.length) {
      alert('이미 승인(완료/거절)된 건은 선택할 수 없습니다.');
    }
    selectedRows.value = filtered; // ✅ 배열 그대로 저장
  } else {
    if (['w1', 'w2', 'w3'].includes(selection.returnStatusInternal)) {
      alert('이미 승인(완료/거절)된 건은 선택할 수 없습니다.');
      selectedRows.value = [];
    } else {
      selectedRows.value = [selection];
    }
  }
};

// 거절사유 모달
const rejectModalVisible = ref(false);
const rejectReason = ref('');

const openRejectModal = () => {
  if (!selectedRows.value) {
    alert('처리할 반품 건을 선택하세요.');
    return;
  }
  rejectReason.value = '';
  rejectModalVisible.value = true;
};

const handleRejectWithReason = async () => {
  if (!rejectReason.value.trim()) {
    alert('거절 사유를 입력하세요.');
    return;
  }

  try {
    const payload = {
      // prodReturnCd: selectedRows.value.prodReturnCd,
      prodReturnCd: selectedRows.value[0].prodReturnCd,
      manager: user.value.memCd,
      rejectRea: rejectReason.value
    };

    console.log('reject payload:', payload);

    await rejectReturn(payload);
    alert('거절 처리되었습니다.');
    rejectModalVisible.value = false;
    fetchReturnList();  // 목록 새로고침
  } catch (err) {
    console.error('거절 처리 실패:', err);
    alert('거절 처리 중 오류 발생');
  }
};

const handleReject = () => {
  if (!selectedRows.value || selectedRows.value.length === 0) {
    alert('처리할 반품 건을 선택하세요.');
    return;
  }

  if (selectedRows.value.length > 1) {
    alert('반품 거절은 한 건씩만 가능합니다.');
    return;
  }

  openRejectModal();
};

onMounted(async () => {
  try {
    // 공통코드 '0W' 가져오기 (반품상태코드)
    await commonStore.fetchCommonCodes('0W');

    // 반품 목록 조회
    fetchReturnList();
  } catch (err) {
    console.error('초기 로딩 실패:', err);
  }
});

watch(selectedRows, (newVal) => {
  console.log('선택된 목록:', newVal)
})
</script>

<template>
  <div class="space-y-4">
    <SearchForm :columns="searchColumns" @search="handleSearch" @reset="handleReset" />
  </div>
  <div class="space-y-4 mt-8">
    <InputTable
      v-model:data="returnList"
      v-model:selection="selectedRows"
      :dataKey="'prodReturnCd'"
      :columns="returnColumns"
      :enableRowActions="false"
      :enableSelection="true"
      :scroll-height="'55vh'" :height="'65vh'"
      :selectionMode="'multiple'"
      :showRowCount="true"
      :buttons="tableButtons"
      :dateFields="['returnDt', 'returnEndDt']"
      @save="handleApprove"
      @delete="handleReject"
      @selectionChange="handleSelectionChange"
    />
  </div>

  <Dialog 
    v-model:visible="rejectModalVisible"
    modal
    header="거절 사유 입력"
    :style="{ width: '500px' }"
    :closable="false"
  >
    <div class="mb-4">
      <h4 class="mb-2">거절할 반품건</h4>
      <div class="bg-gray-50 p-3 rounded">
        <!-- {{ selectedRows?.prodReturnCd }} - {{ selectedRows?.prodName }} -->
        {{ selectedRows[0]?.prodReturnCd }} - {{ selectedRows[0]?.prodName }}
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
        placeholder="거절 사유를 입력하세요..."
        class="w-full"
      />
    </div>

    <div class="flex justify-end gap-2">
      <Button 
        label="취소" 
        severity="secondary" 
        @click="rejectModalVisible = false"
      />
      <Button 
        label="거절 처리" 
        severity="danger" 
        @click="handleRejectWithReason"
        :disabled="!rejectReason.trim()"
      />
    </div>
  </Dialog>
</template>