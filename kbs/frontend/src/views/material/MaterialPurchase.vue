<script setup>
/**
 * SearchForm Import 문제 해결 기록:
 * 
 * 1. 기존 문제점:
 *    - <script>에서 defineProps, defineEmits를 import해서 사용했으나 
 *      이는 <script setup> 전용 문법이므로 일반 <script>에서 사용 불가
 *    - Composition API를 사용하면서 <script setup> 문법을 사용하지 않음
 *    - props로 정의한 searchColumns를 템플릿에서 props.searchColumns로 접근
 * 
 * 2. 해결 방법:
 *    - <script> → <script setup>으로 변경
 *    - defineProps, defineEmits import 제거 (setup에서는 자동 제공)
 *    - searchColumns를 props 대신 ref()로 reactive 데이터로 변경
 *    - 템플릿에서 props.searchColumns → searchColumns로 변경
 *    - 이벤트 핸들러에서 props.searchColumns → searchColumns.value로 변경
 */
import { CustomerService } from '@/service/CustomerService';
import { onBeforeMount, ref } from 'vue';
import inputForm from '@/components/kimbap/searchform/inputForm.vue';
import InputTable from '@/components/kimbap/table/InputTable.vue';
import { useMaterialStore } from '@/stores/materialStore';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import ToggleButton from 'primevue/togglebutton';
import { storeToRefs } from 'pinia';

const customers2 = ref(null);
const balanceFrozen = ref(false);
function formatCurrency(value) {
  return value.toLocaleString('en-US', { style: 'currency', currency: 'USD' });
}

const materialStore = useMaterialStore();
const { searchColumns, purchaseColumns, purchaseData } = storeToRefs (materialStore);

// SearchForm props 정의
searchColumns.value = [
  {
    key: 'purc_cd',
    label: '발주서코드',
    type: 'text',
    placeholder: '발주서코드를 입력하세요'
  },
  {
    key: 'regi',
    label: '등록자',
    type: 'text',
    placeholder: '등록자를 입력하세요'
  },
  {
    key: 'singleDate',
    label: '주문일자',
    type: 'calendar',
    placeholder: '날짜를 선택하세요'
  }
];

const purchaseFormButtons = ref({
  save: { show: true, label: '저장', severity: 'success' },
  reset: { show: true, label: '초기화', severity: 'secondary' },
  delete: { show: true, label: '삭제', severity: 'danger' },
  load: { show: true, label: '발주서 불러오기', severity: 'info' }
});

purchaseColumns.value = [
  {
    field: 'materialName',
    header: '자재명',
    type: 'input',
    placeholder: '자재명을 입력하세요'
  },
  {
    field: 'buyer',
    header: '거래처',
    type: 'input',
    placeholder: '거래처를 입력하세요'
  },
  {
    field: 'number',
    header: '수량',
    type: 'input',
    placeholder: '수량을 입력하세요'
  },
  {
    field: 'unit',
    header: '단위',
    type: 'input',
    placeholder: '단위를 입력하세요'
  },
  {
    field: 'price',
    header: '단가',
    type: 'input',
    placeholder: '단가를 입력하세요'
  },
  {
    field: 'totalPrice',
    header: '총액',
    type: 'input',
    placeholder: '총액을 입력하세요'
  },
  {
    field: 'date',
    header: '납기예정일',
    type: 'calendar',
    placeholder: '납기예정일을 선택하세요'
  },
  {
    field: 'memo',
    header: '비고',
    type: 'input',
    placeholder: '비고를 입력하세요'
  }
];

// 예시 데이터
purchaseData.value = [
  {
    id: 1,
    materialName: '자재1',
    buyer: '거래처1',
    number: 100,
    unit: '개',
    price: 5000,
    totalPrice: 500000,
    date: '2025-07-22',
    memo: '비고 내용'
  },
  {
    id: 2,
    materialName: '자재2',
    buyer: '거래처2',
    number: 200,
    unit: '개',
    price: 3000,
    totalPrice: 600000,
    date: '2025-07-23',
    memo: '비고 내용'
  }
];

onBeforeMount(() => {
  CustomerService.getCustomersLarge().then((data) => (customers2.value = data));
});

// 검색 이벤트 핸들러
const handleSearch = (searchData) => {
  console.log('테이블 컴포넌트에서 받은 검색 데이터:', searchData);
  // 여기에 검색 로직 구현
  searchColumns.value.forEach(column => {
    column.value = searchData[column.key] || '';
  });
};

// 리셋 이벤트 핸들러
const handleReset = () => {
  console.log('검색 조건이 리셋되었습니다');
  // 여기에 리셋 로직 구현
  searchColumns.value.forEach(column => {
    column.value = '';
  });
};
const handleSave = (formData) => {
  console.log('저장 데이터:', formData);
  // 여기에 저장 로직 구현
  alert('발주서가 저장되었습니다!');
};

const handleDelete = (formData) => {
  console.log('삭제 요청:', formData);
  if (confirm('정말 삭제하시겠습니까?')) {
    // 삭제 로직 구현
    alert('발주서가 삭제되었습니다!');
  }
};

const handleLoad = () => {
  console.log('발주서 불러오기 요청');
  // 발주서 목록을 보여주는 모달이나 페이지로 이동하는 로직
  alert('발주서 목록을 불러옵니다!');
};

// 커스텀 버튼 핸들러 (슬롯용)
// const handleExport = () => {
//   console.log('엑셀로 내보내기');
//   alert('엑셀 파일로 내보냅니다!');
// };
</script>
<template>
  <div>
    <!-- 발주서 입력 폼 -->
    <inputForm :columns="searchColumns" :buttons="purchaseFormButtons" button-position="top" @submit="handleSave"
      @reset="handleReset" @delete="handleDelete" @load="handleLoad">
      <!-- 슬롯으로 추가 버튼 넣기 -->
      <!-- <template #top-buttons>
        <Button label="엑셀 내보내기" severity="help" @click="handleExport" />
      </template> -->
    </inputForm>
  </div>

  <div class="mt-10">
    <InputTable :columns="purchaseColumns" :data="purchaseData" @search="handleSearch" @reset="handleReset" />
  </div>
</template>