<script setup>
import { ref, computed, onMounted } from 'vue';
import { useMaterialStore } from '@/stores/materialStore';
import { useToast } from 'primevue/usetoast';

import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import BasicTable from '@/components/kimbap/table/BasicTable.vue';
import RadioButton from 'primevue/radiobutton';

// Store 및 Toast
const materialStore = useMaterialStore();
const toast = useToast();

// 반응형 데이터
const userType = ref('internal'); // 'internal' 또는 'supplier'

// computed 속성들
const { 
  purchaseOrderDetailData, 
  purchaseStatusOptions,
  updatePurchaseStatus,
  modalDataSets
} = materialStore;

// 검색 컬럼 설정 (권한별로 다름!)
const searchColumns = computed(() => {
  if (userType.value === 'internal') {
    // 내부직원용 검색 조건
    return [
      { 
        key: 'purcDCd', 
        label: '발주상세번호', 
        type: 'text',
        placeholder: '발주상세번호 검색'
      },
      { 
        key: 'materialName', 
        label: '자재명', 
        type: 'text',
        placeholder: '자재명 검색'
      },
      { 
        key: 'materialRequest', 
        label: '자재요청', 
        type: 'text',
        placeholder: '자재요청 검색'
      },
      { 
        key: 'purcDStatus', 
        label: '발주상태', 
        type: 'dropdown',
        options: [
          { label: '전체', value: '' },
          ...purchaseStatusOptions
        ],
        placeholder: '상태 선택'
      },
      { 
        key: 'ordDt', 
        label: '주문일자', 
        type: 'dateRange',
        startPlaceholder: '시작일',
        endPlaceholder: '종료일'
      }
    ];
  } else {
    // 공급업체직원용 검색 조건
    return [
      { 
        key: 'mcode', 
        label: '자재코드', 
        type: 'text',
        placeholder: '자재코드 검색'
      },
      { 
        key: 'materialName', 
        label: '자재명', 
        type: 'text',
        placeholder: '자재명 검색'
      },
      { 
        key: 'exDeliDt', 
        label: '납기일', 
        type: 'dateRange',
        startPlaceholder: '납기 시작일',
        endPlaceholder: '납기 종료일'
      },
      { 
        key: 'purcDStatus', 
        label: '발주상태', 
        type: 'dropdown',
        options: [
          { label: '전체', value: '' },
          ...purchaseStatusOptions
        ],
        placeholder: '상태 선택'
      }
    ];
  }
});

// 메서드들
const onSearch = (searchData) => {
  console.log('검색 데이터:', searchData);
  toast.add({
    severity: 'success',
    summary: '검색 완료',
    detail: '발주 데이터를 조회했습니다.',
    life: 3000
  });
};

const onReset = () => {
  toast.add({
    severity: 'info',
    summary: '초기화 완료',
    detail: '검색 조건이 깔끔하게 초기화되었습니다.',
    life: 3000
  });
};

// 생명주기
onMounted(() => {
  loadSampleData();
});

// 샘플 데이터 로드
const loadSampleData = () => {
  const sampleData = [
    {
      purcDCd: 'PD001',
      purcCd: 'P001',
      cpCd: 'CP001',
      mcode: 'M001',
      materialName: '스테인리스 스틸 304',
      materialType: '금속재료',
      mateVerCd: 'V001',
      purcQty: 100,
      unit: 'kg',
      unitPrice: 5000,
      ordTotalAmount: 500000,
      exDeliDt: new Date('2025-08-01'),
      actualDeliDt: null,
      note: '긴급 주문',
      purcDStatus: 'ORD_COMP',
      ordDt: new Date('2025-07-20'),
      regi: '김관리'
    },
    {
      purcDCd: 'PD002',
      purcCd: 'P002',
      cpCd: 'CP002',
      mcode: 'M002',
      materialName: '알루미늄 합금 6061',
      materialType: '금속재료',
      mateVerCd: 'V002',
      purcQty: 50,
      unit: 'kg',
      unitPrice: 8000,
      ordTotalAmount: 400000,
      exDeliDt: new Date('2025-08-05'),
      actualDeliDt: null,
      note: '정기 주문',
      purcDStatus: 'PROD',
      ordDt: new Date('2025-07-22'),
      regi: '이담당'
    },
    {
      purcDCd: 'PD003',
      purcCd: 'P003',
      cpCd: 'CP003',
      mcode: 'M003',
      materialName: '탄소강 ASTM A36',
      materialType: '금속재료',
      mateVerCd: 'V003',
      purcQty: 200,
      unit: 'kg',
      unitPrice: 3500,
      ordTotalAmount: 700000,
      exDeliDt: new Date('2025-07-30'),
      actualDeliDt: new Date('2025-07-28'),
      note: '품질 검증 필요',
      purcDStatus: 'DELI_COMP',
      ordDt: new Date('2025-07-15'),
      regi: '박주임'
    }
  ];
  
  materialStore.setPurchaseOrderDetailData(sampleData);
};
</script>

<template>
  <div class="grid">
    <div class="col-12">
      <div class="card">
        <h5>자재 구매/발주 관리</h5>
        
        <!-- 사용자 타입 선택 -->
        <div class="mb-4">
          <div class="field-radiobutton">
            <RadioButton v-model="userType" inputId="internal" value="internal" />
            <label for="internal" class="ml-2">내부직원</label>
          </div>
          <div class="field-radiobutton">
            <RadioButton v-model="userType" inputId="supplier" value="supplier" />
            <label for="supplier" class="ml-2">공급업체직원</label>
          </div>
        </div>

        <!-- 검색 폼 -->
        <SearchForm 
          :columns="searchColumns"
          @search="onSearch"
          @reset="onReset"
        />

        <!-- 데이터 테이블 -->
        <BasicTable 
          :data="purchaseOrderDetailData"
          :columns="currentTableColumns"
          :title="userType === 'internal' ? '발주 목록 (내부직원용)' : '발주 목록 (공급업체용)'"
          selectionMode="single"
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