<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import InputTable from '@/components/kimbap/table/InputTable.vue';
import { getRelOrdList } from '@/api/distribution';
import { useRouter } from 'vue-router';
import { useRoute } from 'vue-router';
import { useCommonStore } from '@/stores/commonStore'; // 🔥 공통코드 store 추가

// 라우터 설정
const router = useRouter();
const route = useRoute();

// 🔥 공통코드 store 추가
const commonStore = useCommonStore();

// api 데이터
const rawData = ref([]);

// 🔥 공통코드 형변환 함수
const convertStatusCodes = (list) => {
  const statusCodes = commonStore.getCodes('0M'); // 출고지시상태 코드

  return list.map(item => {
    const matchedStatus = statusCodes.find(code => code.dcd === item.relOrdStatus);

    return {
      ...item,
      relOrdStatus: matchedStatus ? matchedStatus.cdInfo : item.relOrdStatus,
    };
  });
};

// 🔥 변환된 데이터 computed
const cleanConvertedData = computed(() => {
  const dataArray = Array.isArray(rawData.value) ? rawData.value : [];
  return convertStatusCodes(dataArray);
});

const searchValues = ref({ type: '전체' });
const onReset = () => { searchValues.value = { type: '전체' } };

// ✅ onMounted 시 API 호출 + 공통코드 로드
onMounted(async () => {
  try {
    // 🔥 공통코드 로드
    await commonStore.fetchCommonCodes('0M'); // 출고지시상태 코드

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

// 🔥 공통코드 원본값 조회 함수 (검색 시 사용)
const getOriginalStatusCode = (displayValue) => {
  const statusCodes = commonStore.getCodes('0M');
  const found = statusCodes.find(code => code.cdInfo === displayValue);
  return found ? found.dcd : displayValue;
};

const searchColumns = ref([
  {
    key: 'cpName',
    label: '거래처명',
    type: 'text',
    placeholder: '거래처명을 입력하세요'
  },
  {
    key: 'relMasCd',
    label: '출고지시번호',
    type: 'text',
    placeholder: '출고지시번호를 입력하세요'
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
      { label: '대기', value: 'm1' }, // 🔥 원본 코드값 사용
      { label: '부분출고', value: 'm3' },
      { label: '완료', value: 'm2' },
      { label: '거절', value: 'm4' } // 🔥 거절 옵션 추가
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
      type,
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
      type: 'readonly',
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
      field: 'ordQty',
      header: '주문수량',
      type: 'readonly',
      align: 'right'
    },
    {
      field: 'relOrdQty',
      header: '지시수량',
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
      align: 'center' // 🔥 상태는 가운데 정렬이 더 적절
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
          :height="'60vh'" :title="`출고지시서 (총 ${cleanConvertedData.length}건)`" :buttons="materialTableButtons"
          :enableRowActions="false" :enableSelection="false" @rowClick="handleRowclicked" />
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