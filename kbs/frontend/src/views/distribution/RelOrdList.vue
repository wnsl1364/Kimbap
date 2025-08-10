<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import InputTable from '@/components/kimbap/table/InputTable.vue';
import { getRelOrdList } from '@/api/distribution';
import { useRouter } from 'vue-router';
import { useRoute } from 'vue-router';

// 라우터 설정
const router = useRouter();
const route = useRoute();

// api 데이터
const rawData = ref([]);

// 필터링된 데이터
const cleanConvertedData = computed(() => Array.isArray(rawData.value) ? rawData.value : []);

const searchValues = ref({ type: '전체' });
const onReset = () => { searchValues.value = { type: '전체' } };

// ✅ onMounted 시 API 호출
onMounted(async () => {
  try {
    const result = await getRelOrdList({});
    console.log('✅ 응답 데이터:', result.data); // ← 실제 테이블용 데이터 확인
    rawData.value = result.data; // ✅ 핵심 수정
  } catch (e) {
    console.error('데이터 로딩 실패:', e);
  }
});

const materialTableButtons = ref({
  add: { show: false },
  edit: { show: false },
  delete: { show: false },
  save: { show: false }
});


const searchColumns = ref([
  {
    key: 'cpName',
    label: '거래처명',
    type: 'text',
    placeholder: '제품명을 입력하세요'
  },
  {
    key: 'relMasCd',
    label: '출고지시번호',
    type: 'text',
    placeholder: '제품코드를 입력하세요'
  },
  {
    key: 'relDt',
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
    gridColumns: 4,
    options: [
      { label: '전체', value: '전체' },
      { label: '요청', value: 'm1' },
      { label: '부분출고', value: 'm3' },
      { label: '출고완료', value: 'm2' }
    ]
  },
]);

const onSearch = async (searchValues) => {
  try {
    const {
      cpName,
      relMasCd,
      relDtStart,
      relDtEnd,
      type
    } = searchValues;

    const startDate = relDtStart || null;
    const endDate = relDtEnd || null;

    const filter = {
      cpName,
      relMasCd,
      type,
      startDate,
      endDate
    };

    console.log('🔍 필터 조건:', filter);

    const result = await getRelOrdList(filter);
    console.log('🎯 응답 데이터:', result.data);
    rawData.value = result.data;
  } catch (e) {
    console.error('검색 실패:', e);
  }
};





// InputTable용 컬럼 정의 (실제 데이터 필드와 매치)
const inputTableColumns = computed(() => {
  const baseColumns = [
    {
      field: 'relDt',
      header: '출고지시일자',
      type: 'readonly',
      align: 'center'
    },
    {
      field: 'relMasCd',
      header: '출고지시번호',
      type: 'clickable',
      align: 'center',
      width: 200
    },
    {
      field: 'cpName',
      header: '거래처명',
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
      field: 'relOrdQty',
      header: '총수량',
      type: 'readonly',
      align: 'right'
    },
    {
      field: 'deliAdd',
      header: '배송지주소',
      type: 'readonly',
      align: 'center'
    },
    {
      field: 'relOrdStatus',
      header: '출고지시상태',
      type: 'readonly',
      align: 'right'
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

const handleRowclicked = (row) => {
  console.log('📋 선택된 행:', row);
  
  const relMasCd = row.relMasCd;
  
  // 상세 페이지로 이동
  router.push({ path: '/distribution/relOrdAndResult', query: { relMasCd } });
};
</script>

<template>
  <div class="grid">
    <div class="col-12">
      <div class="card">
        <h5>출고지시서 조회</h5>
        <SearchForm :columns="searchColumns" v-model="searchValues" @search="onSearch" :gridColumns="3"
          @reset="onReset" />

        <!-- 매핑된 InputTable -->
        <InputTable :columns="inputTableColumns" :data="cleanConvertedData" dataKey="relMasCd" :scroll-height="'50vh'"
          :height="'60vh'" :title="`입출고 리스트`" :buttons="materialTableButtons" :enableRowActions="false"
          :enableSelection="false" @rowClick="handleRowclicked"/>
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