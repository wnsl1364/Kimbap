<script setup>
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import InputTable from '@/components/kimbap/table/InputTable.vue';
import { useMaterialStore } from '@/stores/materialStore';
import { storeToRefs } from 'pinia';
import { readonly } from 'vue';
import { format } from 'date-fns';
import { useToast } from 'primevue/usetoast';
import { ref, onMounted } from 'vue';
import { getPurcOrderList } from '@/api/materials';

const materialStore = useMaterialStore();
const { searchColumns, purchaseColumns, purchaseData, purchaseFormButtons } = storeToRefs(materialStore);
const toast = useToast();
const searchData = ref({});

// 검색 폼 설정
searchColumns.value = [
  {
    key: 'purc_cd',
    label: '발주서코드',
    type: 'readonly',
    placeholder: '발주서코드를 입력하세요'
  },
  {
    key: 'regi',
    label: '등록자',
    type: 'readonly',
    placeholder: '등록자를 입력하세요'
  },
  {
    key: 'singleDate',
    label: '주문일자',
    type: 'readonly',
    placeholder: '날짜를 선택하세요'
  }
];

// 폼 버튼들 다 숨기기! 🙈
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
    header: '자재명',
    type: 'readonly',
    width: '100px',
    placeholder: '자재명을 입력하세요'
  },
  {
    field: 'buyer',
    header: '거래처',
    type: 'readonly',
    width: '150px',
    placeholder: '거래처를 입력하세요'
  },
  {
    field: 'number',
    header: '수량',
    type: 'readonly',
    width: '100px',
    placeholder: '수량을 입력하세요'
  },
  {
    field: 'unitPrice',
    header: '단가(원)',
    type: 'readonly',
    width: '100px',
    placeholder: '단가를 입력하세요'
  },
  {
    field: 'totalPrice',
    header: '(수량+단가)총액(원)',
    type: 'readonly',
    width: '150px',
    placeholder: '(수량+단가)총액을 입력하세요'
  },
  {
    field: 'deliveryDate',
    header: '납기일',
    type: 'readonly',
    width: '120px',
    placeholder: '납기일을 선택하세요'
  },
  {
    field: 'remarks',
    header: '비고',
    type: 'readonly',
    width: '200px',
    placeholder: '비고를 입력하세요'
  },
  {
    field: 'rejectionReason',
    header: '반려사유',
    type: 'readonly',
    width: '200px',
    placeholder: '반려사유를 입력하세요'
  }
];

const getPurc = async () => {
  try {
    const response = await fetchPurcOrderList();
    if (response && response.data) {
      purchaseData.value = formatDataDates(response.data);
    } else {
      console.warn('발주서 목록이 비어있습니다.');
      purchaseData.value = [];
    }
  } catch (error) {
    console.error('발주서 목록 불러오기 실패:', error);
    purchaseData.value = [];
  }
};

// 테이블 버튼 설정 - 기존 버튼들 다 숨기기! 🙈
const materialTableButtons = {
  add: { show: false, label: '추가', severity: 'primary' },
  edit: { show: false, label: '수정', severity: 'secondary' },
  delete: { show: false, label: '삭제', severity: 'danger' },
  save: { show: false, label: '저장', severity: 'success' }
};

// 반려/승인 이벤트 핸들러들! 💫
const handleReject = () => {
  console.log('반려 버튼 클릭됨! 😢');
  // 여기에 반려 로직 구현
  alert('반려 처리되었습니다!');
};

const handleApprove = () => {
  console.log('승인 버튼 클릭됨! 🎉');
  // 여기에 승인 로직 구현  
  alert('승인 처리되었습니다!');
};

const formatDataDates = (data) => {
  return data.map(item => {
    return {
      ...item,
      deliveryDate: format(new Date(item.deliveryDate), 'yyyy-MM-dd'),
      orderDate: format(new Date(item.orderDate), 'yyyy-MM-dd')
    };
  });
};



onMounted(() => {
  console.log('MaterialPurchaseConfirm.vue mounted');
  getPurcOrderList();
});

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
          label="승인" 
          severity="success" 
          icon="pi pi-check"
          @click="handleApprove" 
        />
        <Button 
          label="반려" 
          severity="danger" 
          icon="pi pi-times"
          @click="handleReject" 
        />
      </template>
    </InputTable>
</template>