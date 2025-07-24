<script setup>
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import InputTable from '@/components/kimbap/table/InputTable.vue';
import { useMaterialStore } from '@/stores/materialStore';
import { storeToRefs } from 'pinia';
import { readonly } from 'vue';

const materialStore = useMaterialStore();
const { searchColumns, purchaseColumns, purchaseData, purchaseFormButtons } = storeToRefs(materialStore);

// 검색 폼 설정
searchColumns.value = [
  {
    key: 'purc_cd',
    label: '입고번호',
    type: 'text',
    placeholder: '입고번호를 입력하세요'
  },
  {
    key: 'regi',
    label: '자재코드',
    type: 'text',
    placeholder: '자재코드를 입력하세요'
  },
  {
    key: 'singleDate',
    label: '자재명',
    type: 'text',
    placeholder: '자재명을 입력하세요'
  },
    {
        key: 'status',
        label: '창고',
        type: 'dropdown',
        options: [
        { label: 'A 창고', value: 'waiting' },
        { label: 'B 창고', value: 'in_progress' },
        { label: '엄준식', value: 'completed' }
        ],
        placeholder: '창고를 선택하세요'
    },
    {
        key: 'dateRange',
        label: '입고일자',
        type: 'dateRange',
        placeholder: '입고일자를 선택하세요'
    }
];

// 폼 버튼들 다 숨기기
purchaseFormButtons.value = {
  save: { show: false, label: '저장', severity: 'success' },
  reset: { show: false, label: '초기화', severity: 'secondary' },
  delete: { show: false, label: '삭제', severity: 'danger' },
  load: { show: false, label: '발주서 불러오기', severity: 'info' }
};

// 테이블 컬럼 설정 (다 readonly로!)
purchaseColumns.value = [
  {
    field: 'materialName',
    header: '입고번호',
    type: 'readonly',
    width: '100px',
    placeholder: '자재명을 입력하세요'
  },
  {
    field: 'buyer',
    header: '입고일자',
    type: 'readonly',
    width: '150px',
    placeholder: '거래처를 입력하세요'
  },
  {
    field: 'number',
    header: '자재코드',
    type: 'readonly',
    width: '100px',
    placeholder: '수량을 입력하세요'
  },
  {
    field: 'unitPrice',
    header: '자재명',
    type: 'readonly',
    width: '100px',
    placeholder: '단가를 입력하세요'
  },
  {
    field: 'totalPrice',
    header: '(LOT번호',
    type: 'readonly',
    width: '150px',
    placeholder: '(수량+단가)총액을 입력하세요'
  },
  {
    field: 'deliveryDate',
    header: '적재대기수량',
    type: 'readonly',
    width: '120px',
    placeholder: '납기일을 선택하세요'
  },
  {
    field: 'remarks',
    header: '단위',
    type: 'readonly',
    width: '200px',
    placeholder: '비고를 입력하세요'
  },
  {
    field: 'rejectionReason',
    header: '창고',
    type: 'readonly',
    width: '200px',
    placeholder: '반려사유를 입력하세요'
  },
    {
        field: 'status',
        header: '비고',
        type: 'readonly',
        width: '100px',
        placeholder: '상태를 입력하세요'
    }
];

// 테이블 버튼 설정 - 기존 버튼들 다 숨기기!
const materialTableButtons = {
  add: { show: false, label: '추가', severity: 'primary' },
  edit: { show: false, label: '수정', severity: 'secondary' },
  delete: { show: false, label: '삭제', severity: 'danger' },
  save: { show: false, label: '저장', severity: 'success' }
};
const outBndscs = () => {
  console.log('출고완료 버튼 클릭됨! 🎉');
  // 여기에 출고완료 로직 구현
  alert('출고완료 처리되었습니다!');
};

</script>
<template>
    <SearchForm
      :columns="searchColumns"
      v-model:searchData="searchData"
      :formButtons="purchaseFormButtons"
    />
    <InputTable
      :columns="purchaseColumns"
      :data="purchaseData"
      :buttons="materialTableButtons"
      :enableRowActions="false"
      :enableSelection="false"
      dataKey="id"
    >
      <!-- slot으로 반려/승인 버튼 추가! -->
      <template #top-buttons>
        <Button 
          label="출고완료" 
          outlined 
          severity="info" 
          icon="pi pi-cart-minus"
          @click="outBndscs" 
        />
      </template>
    </InputTable>
</template>