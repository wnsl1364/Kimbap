<!-- views/production/ProdReqetailModal.vue -->
 <script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { format } from 'date-fns';
import { useProductStore } from '@/stores/productStore';
import { useCommonStore } from '@/stores/commonStore'
import { storeToRefs } from 'pinia';
import { useToast } from 'primevue/usetoast';
import Dialog from 'primevue/dialog';
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import StandartTable from '@/components/kimbap/table/StandardTable.vue';

const toast = useToast();

const props = defineProps({
  visible: Boolean,
  mode: { type: String, default: 'full' }
});
const emit = defineEmits(['update:visible', 'select']);

// 공통코드 가져오기
const common = useCommonStore()
const { commonCodes } = storeToRefs(common)
// 생산요청 목록 가져오기
const store = useProductStore();
const { condProdRequestList, prodRequestDetailList } = storeToRefs(store);
const { fetchProdRequestDetailList, fetchFactoryList } = store;

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
  await common.fetchCommonCodes('0G') // 공통코드 가져오기
  await common.fetchCommonCodes('0B') // 공통코드 가져오기
});
// 검색 목록
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
// 생산요청 조건 검색
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
// 생산요청 행 클릭 시 해당 정보 전달
const handleRowClick = async (row) => {
  if (props.mode === 'basic') {
    emit('select', { basicInfo: row });
  } else {
    await fetchProdRequestDetailList(row.produReqCd);
    emit('select', {
      basicInfo: row,
      detailList: convertDetailUnitCodes(prodRequestDetailList.value)
    });
  }

  emit('update:visible', false);
};
const prodReqColumns = [
  { field: 'produReqCd', header: '생산요청번호' },
  { field: 'reqDt', header: '요청일자' },
  { field: 'deliDt', header: '납기일자' },
  { field: 'facName', header: '공장' },
  { field: 'sumReqQty', header: '총요청수량', align: 'right', slot: true  },
  { field: 'firstUnit', header: '단위' },
  { field: 'note', header: '비고' },
  { field: 'prReqStatus', header: '상태' }
];

// 검색 결과도 초기화
const handleReset = async () => {
  condProdRequestList.value = [];
};
// 모달이 열릴 때 이전 검색 결과 초기화
watch(() => props.visible, (val) => {
  if (val) {

    store.condProdRequestList = []
  }
})
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
    header="생산요청 불러오기"
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
      :title="'생산요청 목록'"
      :data="condProdRequestList"
      :columns="prodReqColumns"
      dataKey="produReqCd"
      scrollHeight="45vh" 
      :selectable="false"
      :showHistoryButton="false"
      :hoverable="true"
      :showRowCount="true"
      @row-click="handleRowClick"
    />
  </Dialog>
</template>
