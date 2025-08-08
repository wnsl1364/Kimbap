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
import ProdRequestDetailModal from '@/views/production/ProdRequestDetailModal.vue'

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
const { factoryList, prodRequestDetailList, condProdRequestList } = storeToRefs(store)
const { fetchFactoryList } = store
// 생산요청 상세 정보 모달 띄우기
const detailModalVisible = ref(false)
const detailList = ref([])
const selectedReqCd = ref('')

const exportColumns = ref([]); // 엑셀 다운로드

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
  { field: 'sumReqQty', header: '총요청수량', align: 'right', slot: true  },
  { field: 'firstUnit', header: '단위' },
  { field: 'note', header: '비고' },
  { field: 'prReqStatus', header: '상태' }
]
// 엑셀 다운로드용 컬럼
exportColumns.value = [
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

  try {
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
      // 조회 결과 건수 toast 표시
    const resultCount = condProdRequestList.value.length;
    
    if (resultCount > 0) {
      toast.add({
        severity: 'success',
        summary: '조회 완료',
        detail: `${resultCount}건의 생산요청이 조회되었습니다.`,
        life: 3000
      });
    } else {
      toast.add({
        severity: 'warn',
        summary: '조회 결과 없음',
        detail: '조건에 맞는 생산요청이 없습니다.',
        life: 3000
      });
    }
  } catch (error) {
    console.error('검색 중 오류 발생:', error);
    toast.add({
      severity: 'error',
      summary: '조회 실패',
      detail: '생산요청 조회 중 오류가 발생했습니다.',
      life: 3000
    });
  }
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
  { field: 'reqQty', header: '요청수량', align: 'right', slot: true  },
  { field: 'unit', header: '단위' },
  { field: 'exProduDt', header: '생산예정일자' },
  { field: 'seq', header: '우선순위', align: 'right', slot: true  }
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
  <div class="grid">
    <div class="col-12">
      <div class="card">
        <h5>생산요청 조회</h5>

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
            :title="'생산요청 목록'"
            :data="condProdRequestList"
            :columns="prodRequestColumns"
            dataKey="produReqCd"
            :height="'60vh'"
            scrollHeight="50vh"
            :selectable="false"
            :showHistoryButton="false"
            :hoverable="true"
            :showRowCount="true"
            @row-click="row => openDetailModal(row.produReqCd)"
            :showExcelDownload="true"
            :exportColumns="exportColumns"
            :exportData="mergedExportData" 
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
      </div>
    </div>
  </div>  
</template>