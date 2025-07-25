<script setup>
import { ref, onMounted, computed } from 'vue'
import { format } from 'date-fns';
import { storeToRefs } from 'pinia'
import { useProductStore } from '@/stores/productStore'
import { useCommonStore } from '@/stores/commonStore'
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue'
import StandartTable from '@/components/kimbap/table/StandardTable.vue'
import ProdPlanDetailModal from '@/views/production/ProdPlanDetailModal.vue'


// 공통코드 가져오기
const common = useCommonStore()
const { commonCodes } = storeToRefs(common)
// 생산관리 목록 정보 가져오기
const store = useProductStore()
const { factoryList, prodPlanList, condProdPlanList } = storeToRefs(store)
const { fetchFactoryList } = store
// 생산계획 상세 정보 모달 띄우기
const detailModalVisible = ref(false)
const detailList = ref([])
const selectedPlanCd = ref('')

// 공장 목록 조회
onMounted(async () => {
  await fetchFactoryList()

  await common.fetchCommonCodes('0G')

  condProdPlanList.value = convertUnitCodes(prodPlanList.value);
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
const tableColumns = [
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
  console.log('검색 조건 전달:', searchData);

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
// 모달 열기 함수
const openDetailModal = async (produPlanCd) => {
  selectedPlanCd.value = produPlanCd
  const res = await getProdPlanDetailList(produPlanCd)
  detailList.value = res.data
  detailModalVisible.value = true
}
</script>
<template>
  <div>
    <!-- 검색 모달 -->
    <SearchForm
      :columns="searchColumns"
      @search="handleSearch"
      @reset="handleReset"
    />

    <!-- 검색 결과 테이블 표시 -->
    <StandartTable
      :title="'생산계획 목록'"
      :data="condProdPlanList"
      :columns="tableColumns"
      dataKey="produPlanCd"
      scrollHeight="55vh"
      :selectable="false"
      :showHistoryButton="false"
      :hoverable="true"
      @row-select="row => openDetailModal(row.produPlanCd)"
    />
    <!-- 상세정보 모달 -->
    <ProdPlanDetailModal
      :visible="detailModalVisible"
      :title="`생산계획 상세 - ${selectedPlanCd}`"
      :detailList="detailList"
      @update:visible="val => detailModalVisible.value = val"
    />
  </div>
</template>