<script>
import { CustomerService } from '@/service/CustomerService';
import { onBeforeMount, ref } from 'vue';
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import ToggleButton from 'primevue/togglebutton';
import { defineProps, defineEmits } from 'vue';

const customers2 = ref(null);
const balanceFrozen = ref(false);

function formatCurrency(value) {
  return value.toLocaleString('en-US', { style: 'currency', currency: 'USD' });
}

// SearchForm props 정의
const props = defineProps({
  searchColumns: {
    type: Array,
    default: () => [
      {
        key: 'name',
        label: '이름',
        type: 'text',
        placeholder: '이름을 입력하세요'
      },
      {
        key: 'status',
        label: '상태',
        type: 'radio',
        options: [
          { label: '활성', value: 'active' },
          { label: '비활성', value: 'inactive' }
        ]
      },
      // 숫자 범위 검색 예시  
      {
        key: 'balanceRange',
        label: '잔액 범위',
        type: 'numberRange',
        step: 1000,
        minPlaceholder: '최소 잔액',
        maxPlaceholder: '최대 잔액'
      },
      // 날짜 범위 검색 예시
      {
        key: 'dateRange',
        label: '등록일 범위',
        type: 'dateRange',
        startPlaceholder: '시작일을 선택하세요',
        endPlaceholder: '종료일을 선택하세요'
      },
      {
        key: 'singleDate',
        label: '특정일',
        type: 'calendar',
        placeholder: '날짜를 선택하세요'
      }
    ]
  }
});

onBeforeMount(() => {
  CustomerService.getCustomersLarge().then((data) => (customers2.value = data));
});

// 검색 이벤트 핸들러
const handleSearch = (searchData) => {
  console.log('테이블 컴포넌트에서 받은 검색 데이터:', searchData);
  // 여기에 검색 로직 구현
  props.searchColumns.forEach(column => {
    column.value = searchData[column.key] || '';
  });
};

// 리셋 이벤트 핸들러
const handleReset = () => {
  console.log('검색 조건이 리셋되었습니다');
  // 여기에 리셋 로직 구현
  props.searchColumns.forEach(column => {
    column.value = '';
  });
};
</script>
<template>
  <div>
    <SearchForm :columns="searchColumns" @search="handleSearch" @reset="handleReset" />
  </div>
</template>