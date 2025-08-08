<script setup>
import { ref, onMounted, computed } from 'vue'
import { format } from 'date-fns';
import { storeToRefs } from 'pinia'
import { useToast } from 'primevue/usetoast';
import { useProductStore } from '@/stores/productStore'
import { useCommonStore } from '@/stores/commonStore'
import { useMemberStore } from '@/stores/memberStore'
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue'
import StandartTable from '@/components/kimbap/table/StandardTable.vue'
import ProdPlanDetailModal from '@/views/production/ProdPlanDetailModal.vue'

const toast = useToast();

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
const { factoryList, prodPlanList, condProdPlanList, prodPlanDetailList } = storeToRefs(store)
const { fetchFactoryList, fetchProdPlanDetailList } = store
// 생산계획 상세 정보 모달 띄우기
const detailModalVisible = ref(false)
const detailList = ref([])
const selectedPlanCd = ref('')

// 공장 목록 조회
onMounted(async () => {
  await fetchFactoryList() // 공장정보 가져오기
  await common.fetchCommonCodes('0G') // 공통코드 가져오기
  condProdPlanList.value = convertUnitCodes(prodPlanList.value); // 공통코드 변환
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
  { key: 'produPlanCd', label: '생산계획번호', type: 'text', placeholder: '예: PRP-20250716-01' },
  { key: 'planDtRange', label: '계획일자', type: 'dateRange', startPlaceholder: '시작일', endPlaceholder: '종료일' },
  { key: 'planRange', label: '계획기간', type: 'dateRange', startPlaceholder: '시작일', endPlaceholder: '종료일' },
  {
    key: 'factory',
    label: '공장',
    type: 'dropdown',
    options: factoryOptions,
    placeholder: '공장을 선택하세요'
  },
];
const prodPlanColumns = [
  { field: 'produPlanCd', header: '생산계획번호' },
  { field: 'planDt', header: '계획일자' },
  { field: 'planStartDt', header: '계획기간(시작)' },
  { field: 'planEndDt', header: '계획기간(종료)' },
  { field: 'facName', header: '공장' },
  { field: 'sumPlanQty', header: '총생산수량' },
  { field: 'firstUnit', header: '단위' },
  { field: 'note', header: '비고' }
]
// 생산계획 목록 검색
const handleSearch = async (searchData) => {

try {
    // 전처리: 날짜 객체를 yyyy-MM-dd로 변환
    const formatted = {
      produPlanCd: searchData.produPlanCd,
      planDtStart: searchData.planDtRange?.start ? format(searchData.planDtRange.start, 'yyyy-MM-dd') : null,
      planDtEnd: searchData.planDtRange?.end ? format(searchData.planDtRange.end, 'yyyy-MM-dd') : null,
      periodStartDt: searchData.planRange?.start ? format(searchData.planRange.start, 'yyyy-MM-dd') : null,
      periodEndDt: searchData.planRange?.end ? format(searchData.planRange.end, 'yyyy-MM-dd') : null,
      fcode: searchData.factory?.fcode || null,
      facVerCd: searchData.factory?.facVerCd || null,
    };

    await store.fetchProdPlanListByCondition(formatted);

    // 조건 검색 결과 후 단위 변환
    condProdPlanList.value = convertUnitCodes(condProdPlanList.value);

    // 조회 결과 건수 toast 표시
    const resultCount = condProdPlanList.value.length;
    
    if (resultCount > 0) {
      toast.add({
        severity: 'success',
        summary: '조회 완료',
        detail: `${resultCount}건의 생산계획이 조회되었습니다.`,
        life: 3000
      });
    } else {
      toast.add({
        severity: 'warn',
        summary: '조회 결과 없음',
        detail: '조건에 맞는 생산계획이 없습니다.',
        life: 3000
      });
    }

  } catch (error) {
    console.error('검색 중 오류 발생:', error);
    toast.add({
      severity: 'error',
      summary: '조회 실패',
      detail: '생산계획 조회 중 오류가 발생했습니다.',
      life: 3000
    });
  }
};
// 공통코드 형변환
const convertUnitCodes = (list) => {
  const unitCodes = common.getCodes('0G');
  return list.map(item => {
    const matched = unitCodes.find(code => code.dcd === item.firstUnit);
    return {
      ...item,
      firstUnit: matched ? matched.cdInfo : item.firstUnit
    };
  });
};

// ====== 생산계획상세 모달 영역 ============================
// 생산계획상세 모달 열기
const openDetailModal = async (produPlanCd) => {
  selectedPlanCd.value = produPlanCd;
  await store.fetchProdPlanDetailList(produPlanCd); // Pinia Store에서 상세정보 조회
  detailList.value = convertDetailUnitCodes(store.prodPlanDetailList); // Store에 있는 결과를 로컬로 바인딩
  detailModalVisible.value = true;             // 모달 열기
}
const detailColumns = [
  { field: 'ppdcode', header: '상세코드' },
  { field: 'produPlanCd', header: '계획번호' },
  { field: 'prodName', header: '제품명' },
  { field: 'planQty', header: '계획수량' },
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
  condProdPlanList.value = [];
};
</script>
<template>
  <div class="grid">
    <div class="col-12">
      <div class="card">
        <h5>생산계획 조회</h5>

        <!-- 현재 사용자 정보 -->
        <div class="mb-4 p-3 border-round surface-100">
          <div class="flex align-items-center gap-3">
            <i class="pi pi-user text-primary"></i>
            <div>
              <strong>
                {{ 
                  user?.memType === 'p1' 
                    ? (user?.empName || '테스트 사용자')
                    : user?.memType === 'p3'
                    ? (user?.cpName || '테스트 거래처')
                    : '테스트 사용자'
                }}
              </strong>
              <span class="ml-2 text-500">
                ({{ actualUserType === 'internal' ? '내부직원' : '공급업체직원' }})
              </span>
            </div>
          </div>
        </div>  

        <div>
          <!-- 검색 모달 -->
          <SearchForm
            :columns="searchColumns"
            @search="handleSearch"
            @reset="handleReset"
          />
          <p></p>
          <!-- 검색 결과 테이블 표시 -->
          <StandartTable
            :title="'생산계획 목록'"
            :data="condProdPlanList"
            :columns="prodPlanColumns"
            dataKey="produPlanCd"
            :height="'60vh'"
            scrollHeight="50vh"
            :selectable="false"
            :showHistoryButton="false"
            :hoverable="true"
            :showRowCount="true"
            @row-click="row => openDetailModal(row.produPlanCd)"
          />
          <!-- 상세정보 모달 -->
          <ProdPlanDetailModal
            :visible="detailModalVisible"
            :title="`생산계획 상세 : ${selectedPlanCd}`"
            :detailList="detailList"
            :columns="detailColumns"
            @update:visible="detailModalVisible = $event"
          />
        </div>
      </div>
    </div>
  </div>  
</template>