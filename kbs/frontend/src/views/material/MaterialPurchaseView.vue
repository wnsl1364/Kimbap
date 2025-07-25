<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue';
import { useMaterialStore } from '@/stores/materialStore';
import { useMemberStore } from '@/stores/memberStore';
import { useToast } from 'primevue/usetoast';
import { searchPurchaseOrders } from '@/api/materials';

import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import BasicTable from '@/components/kimbap/table/BasicTable.vue';
import RadioButton from 'primevue/radiobutton';

// Store 및 Toast
const materialStore = useMaterialStore();
const memberStore = useMemberStore();
const toast = useToast();

// 반응형 데이터
const userType = ref('internal');
const isLoading = ref(false);
const showTestControls = ref(true);

// 실제 사용자 권한 기반 타입 설정
const actualUserType = computed(() => {
  if (showTestControls.value) {
    return userType.value;
  }
  
  const memType = memberStore.user?.memType;
  if (memType === 'p1') return 'internal';
  if (memType === 'p3') return 'supplier';
  return 'internal';
});

// 검색 컬럼 설정 (반응성 유지!)
const searchColumns = computed(() => {
  return actualUserType.value === 'internal' 
    ? materialStore.internalPurchaseSearchColumns 
    : materialStore.supplierPurchaseSearchColumns;
});

// 현재 사용할 테이블 컬럼 (반응성 유지!)
const currentTableColumns = computed(() => {
  return actualUserType.value === 'internal' 
    ? materialStore.internalPurchaseColumns 
    : materialStore.supplierPurchaseColumns;
});

// 사용자 타입 변경 시 데이터 다시 로드
watch(actualUserType, () => {
  console.log('사용자 타입 변경됨:', actualUserType.value);
  loadPurchaseData();
});

// 메서드들
const onSearch = async (searchData) => {
  try {
    isLoading.value = true;
    
    const response = await searchPurchaseOrders(searchData, actualUserType.value);
    materialStore.setPurchaseOrderDetailData(response.data);
    
    toast.add({
      severity: 'success',
      summary: '검색 완료',
      detail: `${response.data.length}건의 발주 데이터를 조회했습니다.`,
      life: 3000
    });
    
  } catch (error) {
    console.error('검색 중 오류:', error);
    toast.add({
      severity: 'error',
      summary: '검색 실패',
      detail: '발주 데이터 조회 중 오류가 발생했습니다.',
      life: 3000
    });
  } finally {
    isLoading.value = false;
  }
};

const onReset = () => {
  loadPurchaseData();
  toast.add({
    severity: 'info',
    summary: '초기화 완료',
    detail: '검색 조건이 초기화되고 전체 목록을 조회했습니다.',
    life: 3000
  });
};

// 발주 데이터 로드
const loadPurchaseData = async () => {
  try {
    isLoading.value = true;
    console.log('데이터 로드 시작 - 사용자 타입:', actualUserType.value);
    
    const response = await searchPurchaseOrders({}, actualUserType.value);
    materialStore.setPurchaseOrderDetailData(response.data);
    
    console.log('데이터 로드 완료:', response.data.length, '건');
    
  } catch (error) {
    console.error('데이터 로드 중 오류:', error);
    loadSampleData();
  } finally {
    isLoading.value = false;
  }
};

// 생명주기
onMounted(async () => {
  console.log('컴포넌트 마운트됨');
  console.log('사용자 권한:', actualUserType.value);
  
  // 잠시 기다린 후 데이터 로드 (Store 초기화 대기)
  await nextTick();
  loadPurchaseData();
});

// 샘플 데이터 로드 (백업용)
const loadSampleData = () => {
  console.log('샘플 데이터 로드');
  const sampleData = [
    {
      purcDCd: 'PD001',
      purcCd: 'P001',
      mcode: 'M001',
      mateName: '스테인리스 스틸 304',
      mateType: '금속재료',
      purcQty: 100,
      unit: 'kg',
      totalAmount: 500000,
      exDeliDt: new Date('2025-08-01'),
      deliDt: null,
      note: '긴급 주문',
      purcDStatus: 'c2',
      ordDt: new Date('2025-07-20')
    },
    {
      purcDCd: 'PD002',
      purcCd: 'P002',
      mcode: 'M002',
      mateName: '알루미늄 합금 6061',
      mateType: '금속재료',
      purcQty: 50,
      unit: 'kg',
      totalAmount: 400000,
      exDeliDt: new Date('2025-08-05'),
      deliDt: null,
      note: '정기 주문',
      purcDStatus: 'c3',
      ordDt: new Date('2025-07-22')
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

        <!-- 현재 사용자 정보 표시 -->
        <div class="mb-4 p-3 border-round surface-100">
          <div class="flex align-items-center gap-3">
            <i class="pi pi-user text-primary"></i>
            <div>
              <strong>{{ memberStore.user?.empName || '테스트 사용자' }}</strong>
              <span class="ml-2 text-500">
                ({{ actualUserType === 'internal' ? '내부직원' : '공급업체직원' }})
              </span>
            </div>
          </div>
        </div>

        <!-- 테스트용 권한 변경 -->
        <div class="mb-4" v-if="showTestControls">
          <h6>테스트용 권한 변경:</h6>
          <div class="field-radiobutton">
            <RadioButton v-model="userType" inputId="internal" value="internal" />
            <label for="internal" class="ml-2">내부직원 (p1)</label>
          </div>
          <div class="field-radiobutton">
            <RadioButton v-model="userType" inputId="supplier" value="supplier" />
            <label for="supplier" class="ml-2">공급업체직원 (p3)</label>
          </div>
          <div class="field-checkbox mt-2">
            <input type="checkbox" v-model="showTestControls" id="testMode" />
            <label for="testMode" class="ml-2">테스트 모드</label>
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
          :data="materialStore.purchaseOrderDetailData"
          :columns="currentTableColumns"
          :title="`발주 목록 (${actualUserType === 'internal' ? '내부직원용' : '공급업체용'})`"
          :loading="isLoading"
          selectionMode="single"
        />

        <!-- 강제 새로고침 버튼 (테스트용) -->
        <div class="mt-4" v-if="showTestControls">
          <button @click="loadPurchaseData" class="p-button p-button-secondary">
            데이터 강제 새로고침
          </button>
        </div>
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