<script setup>
import { ref, onMounted, computed } from 'vue'
import { format } from 'date-fns';
import { storeToRefs } from 'pinia'
import { useProductStore } from '@/stores/productStore'
import { useCommonStore } from '@/stores/commonStore'
import { useMemberStore } from '@/stores/memberStore'
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue'
import StandartTable from '@/components/kimbap/table/StandardTable.vue'
import ProdRequestDetailModal from '@/views/production/ProdRequestDetailModal.vue'

// 로그인 정보 가져오기 ====================================================
const memberStore = useMemberStore()
const { user } = storeToRefs(memberStore)

const isEmployee = computed(() => user.value?.memType === 'p1')       // 사원
const isCustomer = computed(() => user.value?.memType === 'p2')       // 매출업체
const isSupplier = computed(() => user.value?.memType === 'p3')       // 공급업체
const isManager = computed(() => user.value?.memType === 'p4')        // 담당자
const isAdmin = computed(() => user.value?.memType === 'p5')          // 시스템 관리자
// ========================================================================

// 공통코드 가져오기
const common = useCommonStore()
const { commonCodes } = storeToRefs(common)
// 생산관리 목록 정보 가져오기
const store = useProductStore()
const { factoryList, prodRequestDetailList, condProdRequestList } = storeToRefs(store)
const { fetchFactoryList } = store
// 생산요청 상세 정보 모달 띄우기
const detailModalVisible = ref(false)
const detailList = ref([])
const selectedReqCd = ref('')

// 공장 목록 조회
onMounted(async () => {
  await fetchFactoryList() // 공장정보 가져오기
  await common.fetchCommonCodes('0G') // 공통코드 가져오기
  await common.fetchCommonCodes('0B') // 공통코드 가져오기
  condProdRequestList.value = convertUnitCodes(condProdRequestList.value); // 공통코드 변환
})

// 공장 드롭다운 옵션
const factoryOptions = computed(() => [
  { label: '전체', value: '' },  
  ...factoryList.value.map(f => ({
    label: f.facName,
    value: { fcode: f.fcode, facVerCd: f.facVerCd }
  }))
])

// 검색 조건 정의
const searchColumns = [
  { key: 'produPeqCd', label: '생산요청번호', type: 'text', placeholder: '예: PROD-20250716-01' },
  { key: 'reqDtRange', label: '요청일자', type: 'dateRange', startPlaceholder: '시작일', endPlaceholder: '종료일' },
  {
    key: 'factory',
    label: '공장',
    type: 'dropdown',
    options: factoryOptions,
    placeholder: '공장을 선택하세요'
  },
];
const prodRequestColumns = [
  { field: 'produReqCd', header: '생산요청번호' },
  { field: 'reqDt', header: '요청일자' },
  { field: 'deliDt', header: '납기일자' },
  { field: 'facName', header: '공장' },
  { field: 'sumReqQty', header: '총요청수량' },
  { field: 'firstUnit', header: '단위' },
  { field: 'note', header: '비고' },
  { field: 'prReqStatus', header: '상태' }
]
// 생산요청 목록 검색
const handleSearch = async (searchData) => {

  // 전처리: 날짜 객체를 yyyy-MM-dd로 변환
  const formatted = {
    produReqCd: searchData.produReqCd,
    reqDtStart: searchData.reqDtRange?.start ? format(searchData.reqDtRange.start, 'yyyy-MM-dd') : null,
    reqDtEnd: searchData.reqDtRange?.end ? format(searchData.reqDtRange.end, 'yyyy-MM-dd') : null,
    fcode: searchData.factory?.fcode || null,
    facVerCd: searchData.factory?.facVerCd || null,
  };

  await store.fetchProdRequestListByCondition(formatted);

  // 조건 검색 결과 후 단위 변환
  condProdRequestList.value = convertUnitCodes(condProdRequestList.value);
};
// 공통코드 형변환
const convertUnitCodes = (list) => {
  const unitCodes = common.getCodes('0G');
  const reqStatusCodes = common.getCodes('0B');
  return list.map(item => {
    const unitMatched = unitCodes.find(code => code.dcd === item.firstUnit);
    const reqMatched = reqStatusCodes.find(code => code.dcd === item.prReqStatus);
    return {
      ...item,
      firstUnit: unitMatched ? unitMatched.cdInfo : item.firstUnit,
      prReqStatus: reqMatched ? reqMatched.cdInfo : item.prReqStatus
    };
  });
};

// ====== 생산요청상세 모달 영역 ============================
// 생산요청상세 모달 열기
const openDetailModal = async (produReqCd) => {
  selectedReqCd.value = produReqCd;
  await store.fetchProdRequestDetailList(produReqCd); // Pinia Store에서 상세정보 조회
  detailList.value = convertDetailUnitCodes(store.prodRequestDetailList); // Store에 있는 결과를 로컬로 바인딩
  detailModalVisible.value = true;             // 모달 열기
}
const detailColumns = [
  { field: 'produProdCd', header: '요청상세번호' },
  { field: 'produReqCd', header: '요청번호' },
  { field: 'prodName', header: '제품명' },
  { field: 'reqQty', header: '요청수량' },
  { field: 'unit', header: '단위' },
  { field: 'exProduDt', header: '생산예정일자' },
  { field: 'seq', header: '우선순위' }
]
// 공통코드 변환
const convertDetailUnitCodes = (list) => {
  const unitCodes = common.getCodes('0G');

  return list.map(item => {
    const matched = unitCodes.find(code => code.dcd === item.unit);
    return {
      ...item,
      unit: matched ? matched.cdInfo : item.unit
    };
  });
};

// 검색 결과도 초기화
const handleReset = async () => {
  condProdRequestList.value = [];
};
</script>
<template>
  <!-- 👑 페이지 헤더 -->
  <div class="mb-6">
    <h1 class="text-3xl font-bold text-gray-800 mb-2">생산요청 조회</h1>
    <div class="flex items-center gap-4 text-sm text-gray-600">
      <span>👤 {{ user?.empName || '로그로그' }}</span>
      <span>🏢 {{ user?.deptName || '생산팀' }}</span>
      <span>{{ user }}</span>
    </div>
  </div>
  <div>
    <!-- 검색 모달 -->
    <SearchForm
      :columns="searchColumns"
      @search="handleSearch"
      @reset="handleReset"
    />

    <!-- 검색 결과 테이블 표시 -->
    <StandartTable
      :title="'생산요청 목록'"
      :data="condProdRequestList"
      :columns="prodRequestColumns"
      dataKey="produReqCd"
      scrollHeight="55vh"
      :selectable="false"
      :showHistoryButton="false"
      :hoverable="true"
      :showRowCount="true"
      @row-click="row => openDetailModal(row.produReqCd)"
    />
    <!-- 상세정보 모달 -->
    <ProdRequestDetailModal
      :visible="detailModalVisible"
      :title="`생산요청 상세 : ${selectedReqCd}`"
      :detailList="detailList"
      :columns="detailColumns"
      @update:visible="detailModalVisible = $event"
    />
  </div>
</template>