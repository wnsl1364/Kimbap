<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import InputTable from '@/components/kimbap/table/InputTable.vue';
import { distributionInOutCheck } from '@/api/distribution';

// api 데이터
const rawData = ref([]);

// ✅ 수정: 수량을 * 40으로 변환한 데이터
const cleanConvertedData = computed(() => {
  const arr = Array.isArray(rawData.value) ? rawData.value : [];
  return arr.map(item => ({
    ...item,
    qty: (item.qty || 0) * 40, // ✅ 수량을 40배로 변환
    displayQty: `${(item.qty || 0) * 40}개` // ✅ 단위 표시용 (선택사항)
  }));
});

const searchValues = ref({ type: '전체' });
const onReset = () => { searchValues.value = { type: '전체' } };

// ✅ onMounted 시 API 호출
onMounted(async () => {
  try {
    const result = await distributionInOutCheck({});
    console.log('✅ 응답 데이터:', result.data); // ← 실제 테이블용 데이터 확인
    rawData.value = result.data; // ✅ 핵심 수정
  } catch (e) {
    console.error('데이터 로딩 실패:', e);
  }
});

// ✅ 수정: 카운트 계산 시에도 변환된 데이터 사용
const inOutCounts = computed(() => {
  const arr = Array.isArray(rawData.value) ? rawData.value : [];
  return arr.reduce((acc, cur) => {
    acc.total++;
    if (cur.type === '입고') acc.in += 1;
    if (cur.type === '출고') acc.out += 1;
    return acc;
  }, { total: 0, in: 0, out: 0 });
});

const materialTableButtons = ref({
  add: { show: false },
  edit: { show: false },
  delete: { show: false },
  save: { show: false }
});

const searchColumns = ref([
  {
    key: 'prodName',
    label: '제품명',
    type: 'text',
    placeholder: '제품명을 입력하세요'
  },
  {
    key: 'pcode',
    label: '제품코드',
    type: 'text',
    placeholder: '제품코드를 입력하세요'
  },
  {
    key: 'wareAreaCd',
    label: '창고',
    type: 'text',
    placeholder: '창고를 입력하세요'
  },
  { 
    key: 'inOutDtRange', 
    label: '일자', 
    type: 'dateRange', 
    startPlaceholder: '시작일', 
    endPlaceholder: '종료일' 
  },
  {
    key: 'type',
    label: '구분',
    value: '전체',
    type: 'radio',
    options: [
      { label: '전체', value: '전체' },
      { label: '입고', value: '입고' },
      { label: '출고', value: '출고' }
    ]
  },
]);

const onSearch = async (searchValues) => {
  try {
    // 조건 분해
    const {
      type,
      inOutDtRange,
      prodName,
      pcode,
      wareAreaCd
    } = searchValues;
    
    // 날짜 처리
    const startDate = inOutDtRange?.[0] ?? null;
    const endDate = inOutDtRange?.[1] ?? null;
    
    // 조건 백엔드 전달
    const filter = {
      type,
      startDate,
      endDate,
      prodName,
      pcode,
      wareAreaCd
    };

    console.log('🔍 필터 조건:', filter);
    
    // POST 요청
    const result = await distributionInOutCheck(filter);
    rawData.value = result.data;
  } catch (e) {
    console.error('검색 실패:', e);
  }
};

// InputTable용 컬럼 정의 (실제 데이터 필드와 매치)
const inputTableColumns = computed(() => {
  const baseColumns = [
    {
      field: 'regDt',
      header: '입출고일자',
      type: 'readonly',
      align: 'center'
    },
    {
      field: 'type',
      header: '구분',
      type: 'readonly',
      align: 'center'
    },
    {
      field: 'pcode',
      header: '제품코드',
      type: 'readonly',
      align: 'left'
    },
    {
      field: 'prodName',
      header: '제품명',
      type: 'readonly',
      align: 'left'
    },
    {
      field: 'qty',
      header: '입출고수량(개)', // ✅ 헤더에 단위 명시
      type: 'readonly',
      align: 'right'
    },
    {
      field: 'wareAreaCd',
      header: '창고',
      type: 'readonly',
      align: 'center'
    },
    {
      field: 'note',
      header: '비고',
      type: 'readonly',
      align: 'right'
    },
  ];
  return baseColumns;
});
</script>

<template>
  <div class="grid">
    <div class="col-12">
      <div class="card">
        <h5>완제품 입출고 조회</h5>
        <SearchForm 
          :columns="searchColumns"  
          v-model="searchValues" 
          @search="onSearch" 
          :gridColumns="3" 
          @reset="onReset" 
        />
        
        <!-- 매핑된 InputTable -->
        <InputTable 
          :columns="inputTableColumns" 
          :data="cleanConvertedData" 
          :scroll-height="'50vh'" 
          :height="'60vh'"
          :title="`완제품 입출고 (총 ${inOutCounts.total}건 / 입고 ${inOutCounts.in}건 · 출고 ${inOutCounts.out}건)`" 
          :buttons="materialTableButtons" 
          :enableRowActions="false" 
          :enableSelection="false" 
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.field-radiobutton {
  display: inline-flex;
  align-items: center;
  margin-right: 1rem;
}
</style>