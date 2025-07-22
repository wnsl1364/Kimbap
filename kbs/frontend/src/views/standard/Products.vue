  <script setup>
  import { ref } from 'vue'
  import { CustomerService } from '@/service/CustomerService';
  import { onBeforeMount} from 'vue';
  import { defineProps } from 'vue';
  import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
  import InputForm from '@/components/kimbap/searchform/inputForm.vue';

  const customers2 = ref(null);
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
      },
      inputColumns: {
          type: Array,
          default: () => [
          { key: 'materialCode', label: '자재코드', type: 'readonly', defaultValue: '자동생성' },
          { key: 'materialName', label: '자재명', type: 'text' },
          { key: 'materialType', label: '자재유형', type: 'dropdown', options: [{ label: '원자재', value: 'raw' }, { label: '부자재', value: 'sub' }] },
          { key: 'unit', label: '단위', type: 'dropdown', options: [{ label: 'kg', value: 'kg' }, { label: 'box', value: 'box' }] },
          { key: 'deliveryUnit', label: '납개단위', type: 'dropdown', options: [{ label: '매', value: '매' }, { label: '장', value: '장' }, { label: 'EA', value: 'ea' }] },
          { key: 'minOrderUnit', label: '최소발주단위', type: 'text' },
          { key: 'supplier', label: '공급사', type: 'text' },
          { key: 'shelfLife', label: '소비기한(일)', type: 'text' },
          { key: 'storageCondition', label: '보관조건', type: 'dropdown', options: [{ label: '냉동', value: '냉동' }, { label: '냉장', value: '냉장' }, { label: '상온', value: '상온' }] },
          { key: 'spec', label: '규격', type: 'text' },
          { key: 'conversionQty', label: '환산수량', type: 'text' },
          { key: 'safeStock', label: '안전재고', type: 'text' },
          { key: 'leadTime', label: '리드타임(일)', type: 'text' },
          { key: 'origin', label: '원산지', type: 'text' },
          { key: 'status', label: '사용여부', type: 'radio', options: [{ label: '활성화', value: 'active' }, { label: '비활성화', value: 'inactive' }] },
          { key: 'changeReason', label: '변경사유', type: 'text' },
          { key: 'note', label: '비고', type: 'text' },
          { key: 'registrant', label: '등록일자', type: 'readonly', defaultValue: new Date().toISOString().slice(0, 10) }
        ]
      }
  });
      

  function formatCurrency(value) {
  return value.toLocaleString('en-US', { style: 'currency', currency: 'USD' });
  }
  onBeforeMount(() => {
  CustomerService.getCustomersLarge().then((data) => (customers2.value = data));
  });

  // 검색 이벤트 핸들러
  const handleSearch = (searchData) => {
      console.log('테이블 컴포넌트에서 받은 검색 데이터:', searchData);
      // 여기에 검색 로직 구현
  };

  // 리셋 이벤트 핸들러
  const handleReset = () => {
      console.log('검색 조건이 리셋되었습니다');
      // 여기에 리셋 로직 구현
  };
  
  </script>
<template>
  <div>
    <SearchForm 
        :columns="searchColumns"
        @search="handleSearch"
        @reset="handleReset"
    />
  </div>
    <InputForm
       :columns="inputColumns"
       class="mt-4"
    />
</template>
