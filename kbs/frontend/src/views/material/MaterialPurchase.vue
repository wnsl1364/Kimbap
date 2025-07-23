<script setup>
import { CustomerService } from '@/service/CustomerService';
import { onBeforeMount, onUnmounted, ref } from 'vue';
import inputForm from '@/components/kimbap/searchform/inputForm.vue';
import InputTable from '@/components/kimbap/table/InputTable.vue';
import { useMaterialStore } from '@/stores/materialStore';
import { storeToRefs } from 'pinia';

const materialStore = useMaterialStore();
const { searchColumns, purchaseColumns, purchaseData } = storeToRefs(materialStore);

// 검색 폼 설정
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

// 테이블 컬럼 설정
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
    inputType: 'number',
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
    inputType: 'number',
    placeholder: '단가를 입력하세요'
  },
  {
    field: 'totalPrice',
    header: '총액',
    type: 'readonly', // 자동계산되니까 readonly
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

// 초기 데이터 (필요하면)
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
  }
];

// 검색/폼 이벤트 핸들러들 (기존과 동일)
const handleSearch = (searchData) => {
  console.log('검색 데이터:', searchData);
};

const handleReset = () => {
  console.log('리셋됨');
};

const handleSave = (formData) => {
  console.log('저장 데이터:', formData);
  alert('발주서가 저장되었습니다.');
};

const handleDelete = (formData) => {
  console.log('삭제 요청:', formData);
  if (confirm('정말 삭제하시겠습니까?')) {
    alert('발주서가 삭제되었습니다.');
  }
};

const handleLoad = () => {
  console.log('발주서 불러오기 요청');
  alert('발주서 목록을 불러옵니다.');
};

// 테이블 데이터 변경 이벤트 (새로 추가!)
const handleDataChange = (newData) => {
  console.log('테이블 데이터가 변경됨:', newData);
  // 필요시 서버에 저장하거나 다른 로직 처리
};

onUnmounted(() => {
  materialStore.$reset();
});
</script>

<template>
  <div>
    <!-- 발주서 입력 폼 -->
    <inputForm 
      :columns="searchColumns" 
      :buttons="purchaseFormButtons" 
      button-position="top" 
      @submit="handleSave"
      @reset="handleReset" 
      @delete="handleDelete" 
      @load="handleLoad">
    </inputForm>
  </div>

  <div class="mt-10">
    <!-- 자재 발주 테이블 - 체크박스로 선택 후 삭제! -->
    <InputTable 
      :title="'자재 발주 목록'" 
      :columns="purchaseColumns" 
      :data="purchaseData"
      :enableRowActions="true"
      :enableSelection="true"
      @dataChange="handleDataChange">
      
      <!-- 필요시 추가 버튼들 -->
      <!-- <template #top-buttons>
        <Button label="엑셀로 내보내기" icon="pi pi-file-excel" severity="success" />
      </template> -->
    </InputTable>
  </div>
</template>