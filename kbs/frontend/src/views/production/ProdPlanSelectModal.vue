<!-- views/production/ProdPlanDetailModal.vue -->
 <script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { format } from 'date-fns';
import { useProductStore } from '@/stores/productStore';
import { useCommonStore } from '@/stores/commonStore'
import { storeToRefs } from 'pinia';
import Dialog from 'primevue/dialog';
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import StandartTable from '@/components/kimbap/table/StandardTable.vue';

const props = defineProps({
  visible: Boolean
});
const emit = defineEmits(['update:visible', 'select']);

// 공통코드 가져오기
const common = useCommonStore()
const { commonCodes } = storeToRefs(common)
// 생산관리 목록 가져오기
const store = useProductStore();
const { condProdPlanList, prodPlanDetailList } = storeToRefs(store);
const { fetchProdPlanListByCondition, fetchProdPlanDetailList, fetchFactoryList } = store;

// 공장 정보 가져와서 검색 폼에 전달
const factoryOptions = computed(() => [
  { label: '전체', value: '' },
  ...store.factoryList.map(f => ({
    label: f.facName,
    value: { fcode: f.fcode, facVerCd: f.facVerCd }
  }))
]);

onMounted(async () => {
  await fetchFactoryList();
  await common.fetchCommonCodes('0G');
});
// 검색 목록
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
  }
];
// 생산계획 조건 검색
const handleSearch = async (searchData) => {
  const formatted = {
    produPlanCd: searchData.produPlanCd,
    planDtStart: searchData.planDtRange?.start ? format(searchData.planDtRange.start, 'yyyy-MM-dd') : null,
    planDtEnd: searchData.planDtRange?.end ? format(searchData.planDtRange.end, 'yyyy-MM-dd') : null,
    periodStartDt: searchData.planRange?.start ? format(searchData.planRange.start, 'yyyy-MM-dd') : null,
    periodEndDt: searchData.planRange?.end ? format(searchData.planRange.end, 'yyyy-MM-dd') : null,
    fcode: searchData.factory?.fcode || null,
    facVerCd: searchData.factory?.facVerCd || null
  };
  await store.fetchProdPlanListByCondition(formatted);
};
// 생산계획 행 클릭 시 해당 정보 전달
const handleRowClick = async (row) => {
  await fetchProdPlanDetailList(row.produPlanCd);
  emit('select', {
    basicInfo: row,
    detailList: convertDetailUnitCodes(prodPlanDetailList.value)
  });
  
  emit('update:visible', false);
};

const prodPlanColumns = [
  { field: 'produPlanCd', header: '생산계획번호' },
  { field: 'planDt', header: '계획일자' },
  { field: 'planStartDt', header: '계획시작' },
  { field: 'planEndDt', header: '계획종료' },
  { field: 'facName', header: '공장' },
  { field: 'note', header: '비고' }
];

// 검색 결과도 초기화
const handleReset = async () => {
  condProdPlanList.value = [];
};
// 모달이 열릴 때 이전 검색 결과 초기화
watch(() => props.visible, (val) => {
  if (val) {

    store.condProdPlanList = []
  }
})
// 공통코드 형변환 후 unitName(사용자 표사용) 담아서 전달
const convertDetailUnitCodes = (list) => {
  const unitCodes = common.getCodes('0G');
  return list.map(item => {
    const matched = unitCodes.find(code => code.dcd === item.unit);
    return {
      ...item,
      unitName: matched ? matched.cdInfo : item.unit
    };
  });
};
</script>

<template>
  <Dialog
    v-model:visible="props.visible"
    modal
    header="생산계획 불러오기"
    style="width: 70vw"
    @update:visible="emit('update:visible', $event)"
  >
    <SearchForm
      :columns="searchColumns"
      @search="handleSearch"
      @reset="handleReset"
    />
    <p></p>
    <StandartTable
      :title="'생산계획 목록'"
      :data="condProdPlanList"
      :columns="prodPlanColumns"
      dataKey="produPlanCd"
      scrollHeight="45vh" 
      :selectable="false"
      :showHistoryButton="false"
      :hoverable="true"
      @row-click="handleRowClick"
    />
  </Dialog>
</template>
