<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useMemberStore } from '@/stores/memberStore';
import { useCommonStore } from '@/stores/commonStore';
import { getReturnList, approveReturn, rejectReturn } from '@/api/return';
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import InputTable from '@/components/kimbap/table/InputTable.vue';
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
  { key: 'dateRange', label: '일자', type: 'dateRange' },
  { key: 'cpName', label: '거래처', type: 'text', placeholder: '거래처명' },
  { key: 'status', label: '상태', type: 'dropdown', options: statusOptions },
  { key: 'prodName', label: '제품명', type: 'text', placeholder: '제품명' }
];

const returnColumns = [
  { field: 'prodReturnCd', header: '반품코드', type: 'readonly' },
  { field: 'returnDt', header: '요청일자', type: 'readonly' },
  { field: 'cpName', header: '거래처', type: 'readonly' },
  { field: 'prodName', header: '품목명', type: 'readonly' },
  { field: 'returnQty', header: '수량(ea)', type: 'readonly', align: 'right' },
  { field: 'returnRea', header: '요청사유', type: 'readonly' },
  { field: 'returnStatus', header: '상태', type: 'readonly' },
  { field: 'returnEndDt', header: '처리일자', type: 'readonly' },
  { field: 'managerName', header: '담당자', type: 'readonly' }
];

// 버튼 설정
const tableButtons = computed(() => {
  return {
    delete: { show: true, label: '반품반려', severity: 'danger' },
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
  console.log('검색조건:', searchData);
  fetchReturnList(searchData);
};

const handleReset = () => {
  fetchReturnList();
};

const handleApprove = async () => {
  if (selectedRows.value.length === 0) {
    alert('처리할 반품 건을 선택하세요.');
    return;
  }

  const confirmed = confirm('선택한 반품건을 승인 처리하시겠습니까?');
  if (!confirmed) return;

  try {
    const payload = [selectedRows.value.prodReturnCd];
    
    await approveReturn(payload);
    alert('승인 처리되었습니다.');
    fetchReturnList();
  } catch (err) {
    console.error('승인 처리 실패:', err);
    alert('승인 처리 중 오류 발생');
  }
};

const handleReject = async () => {
  if (selectedRows.value.length === 0) {
    alert('처리할 반품 건을 선택하세요.');
    return;
  }

  const confirmed = confirm('선택한 반품건을 반려 처리하시겠습니까?');
  if (!confirmed) return;

  try {
    await rejectReturn(selectedRows.value.map(row => row.prodReturnCd));
    alert('반려 처리되었습니다.');
    fetchReturnList();
  } catch (err) {
    console.error('반려 처리 실패:', err);
    alert('반려 처리 중 오류 발생');
  }
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
      :selectionMode="'single'"
      :showRowCount="true"
      :buttons="tableButtons"
      :dateFields="['returnDt', 'returnEndDt']"
      @save="handleApprove"
      @delete="handleReject"
    />
  </div>
</template>