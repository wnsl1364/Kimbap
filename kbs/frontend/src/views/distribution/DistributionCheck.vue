<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import InputTable from '@/components/kimbap/table/InputTable.vue';
import { distributionInOutCheck } from '@/api/distribution';

// api 데이터
const rawData = ref([]);

// 필터링된 데이터
const cleanConvertedData = computed(() => Array.isArray(rawData.value) ? rawData.value : []);

// ✅ onMounted 시 API 호출
onMounted(async () => {
  try {
    const result = await distributionInOutCheck();
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
        key: 'materialName',
        label: '자재명',
        type: 'text',
        placeholder: '자재명을 입력하세요'
    },
    {
        key: 'location',
        label: '위치',
        type: 'text',
        placeholder: '위치를 입력하세요'
    },
    {
        key: 'status',
        label: '상태',
        type: 'radio',
        options: [
            { label: '정상', value: 'normal' },
            { label: '부족', value: 'shortage' },
            { label: '과재고', value: 'overstock' }
        ]
    },
    {
        key: 'lastUpdated',
        label: '최종 업데이트',
        type: 'calendar',
        placeholder: '날짜를 선택하세요'
    }
]);



// 🔥 InputTable용 컬럼 정의 (실제 데이터 필드와 매치!)
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
      header: '수량',
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
      field: 'stockQty',
      header: '잔여재고',
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


</script>

<template>
  <div class="grid">
    <div class="col-12">
      <div class="card">
        <h5>완제품 입출고 조회</h5>
        <SearchForm 
          :columns="searchColumns"
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
          :title="`입출고 리스트`"
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