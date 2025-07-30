<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue';
import { useMaterialStore } from '@/stores/materialStore';
import { useMemberStore } from '@/stores/memberStore';
import { useToast } from 'primevue/usetoast';
import { searchPurchaseOrders } from '@/api/materials';

import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import BasicTable from '@/components/kimbap/table/BasicTable.vue';
import RadioButton from 'primevue/radiobutton';
import { format, isValid } from 'date-fns';
import { useCommonStore } from '@/stores/commonStore';
import InputTable from '@/components/kimbap/table/InputTable.vue';

// Store 및 Toast
const materialStore = useMaterialStore();
const memberStore = useMemberStore();
const common = useCommonStore();
const toast = useToast();

const formatDate = (date) => {
  if (!date) return '';
  
  try {
    const dateObj = date instanceof Date ? date : new Date(date);
    
    if (!isValid(dateObj)) {
      return '';
    }
    
    return format(dateObj, 'yyyy-MM-dd');
  } catch (error) {
    console.error('날짜 포맷 에러:', error);
    return '';
  }
};

const convertUnitCodes = (list) => {
  if (!list || !Array.isArray(list)) {
    return [];
  }

  const unitCodes = common.getCodes('0G'); // 단위코드
  const statusCodes = common.getCodes('0C'); // 발주상태코드
  const matTypeCodes = common.getCodes('0H'); // 자재유형코드

  return list.map(item => {
    const matchedUnit = unitCodes.find(code => code.dcd === item.unit);
    const matchedStatus = statusCodes.find(code => code.dcd === item.purcDStatus);
    const matchedMatType = matTypeCodes.find(code => code.dcd === item.mateType);

    return {
      ...item,
      unit: matchedUnit ? matchedUnit.cdInfo : item.unit,
      purcDStatus: matchedStatus ? matchedStatus.cdInfo : item.purcDStatus,
      mateType: matchedMatType ? matchedMatType.cdInfo : item.mateType,
    };
  });
};

const formatDataDates = (dataList) => {
  const dateFields = ['exDeliDt', 'deliDt', 'ordDt']; // 포맷할 날짜 필드들
  
  return dataList.map(item => {
    const formattedItem = { ...item };
    
    dateFields.forEach(field => {
      if (formattedItem[field]) {
        formattedItem[field] = formatDate(formattedItem[field]);
      }
    });
    
    return formattedItem;
  });
};

// 반응형 데이터
const userType = ref('internal');
const isLoading = ref(false);
const showTestControls = ref(true);

// 🔥 여기가 중요! materialTableButtons를 밖으로 빼냈어!
const materialTableButtons = ref({
  add: { show: false, label: '추가', severity: 'primary' },
  edit: { show: false, label: '수정', severity: 'secondary' },
  delete: { show: false, label: '삭제', severity: 'danger' },
  save: { show: false, label: '저장', severity: 'success' }
});

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
    const formattedData = formatDataDates(response.data);
    const convertedData = convertUnitCodes(formattedData);
    materialStore.setPurchaseOrderDetailData(convertedData);
    
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
    const formattedData = formatDataDates(response.data);
    const convertedData = convertUnitCodes(formattedData);
    materialStore.setPurchaseOrderDetailData(convertedData);

    console.log('데이터 로드 완료:', formattedData.length, '건');

  } catch (error) {
    console.error('데이터 로드 중 오류:', error);
    loadSampleData();
  } finally {
    isLoading.value = false;
  }
};

const convertedTableData = computed(() => {
  const rawData = materialStore.purchaseOrderDetailData;
  if (!rawData || !Array.isArray(rawData)) {
    return [];
  }
  
  return convertUnitCodes(rawData);
});

// 강제 재렌더링용
const forceRenderKey = ref(0);
const forceRender = () => {
  forceRenderKey.value++;
  console.log('🔄 강제 재렌더링 실행:', forceRenderKey.value);
};

// convertedTableData 변화 감지
watch(convertedTableData, (newData) => {
  console.log('🔥 convertedTableData 변경됨:', newData?.length || 0, '건');
  console.log('첫 번째 데이터:', newData?.[0]);
}, { immediate: true, deep: true });

// materialStore 데이터 직접 감지
watch(() => materialStore.purchaseOrderDetailData, (newData) => {
  console.log('🏪 Store 데이터 변경됨:', newData?.length || 0, '건');
}, { immediate: true, deep: true });

// 생명주기
onMounted(async () => {
  // 공통코드 로드 추가
  await Promise.all([
    common.fetchCommonCodes('0G'), // 단위코드
    common.fetchCommonCodes('0C'),  // 발주상태코드
    common.fetchCommonCodes('0H')  // 자재유형코드
  ]);
  
  // 기존 코드...
  await nextTick();
  loadPurchaseData();
});

// 샘플 데이터 로드 (백업용) - 🔥 materialTableButtons 제거!
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

        <!-- 🔥 디버깅 정보 추가! -->
        <div class="mb-4 p-3 bg-yellow-100 border border-yellow-400 rounded">
          <h6 class="text-yellow-800">🐛 디버깅 정보</h6>
          <p><strong>convertedTableData 길이:</strong> {{ convertedTableData?.length || 0 }}</p>
          <p><strong>store 데이터 길이:</strong> {{ materialStore.purchaseOrderDetailData?.length || 0 }}</p>
          <p><strong>첫 번째 데이터:</strong></p>
          <pre class="text-xs">{{ JSON.stringify(convertedTableData?.[0], null, 2) }}</pre>
        </div>

        <!-- 현재 사용자 정보 표시 -->
        <div class="mb-4 p-3 border-round surface-100">
          <div class="flex align-items-center gap-3">
            <i class="pi pi-user text-primary"></i>
            <div>
              <strong>{{ memberStore.user?.empName || '테스트 사용자' }}</strong>
              <span class="ml-2 text-500">
                ({{ actualUserType === 'internal' ? '내부직원' : '공급업체용' }})
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
          :gridColumns="4"
          @reset="onReset"
        />

        <!-- 기본 데이터 테이블 -->
        <BasicTable 
          :data="convertedTableData"
          :columns="currentTableColumns"
          :title="`BasicTable 발주 목록 (${actualUserType === 'internal' ? '내부직원용' : '공급업체용'})`"
          :loading="isLoading"
          selectionMode="single"
        />

        <!-- 🎯 InputTable - 강제로 key 추가해서 재렌더링 유도 -->
        <InputTable
          :key="`input-table-${convertedTableData?.length || 0}`"
          :columns="currentTableColumns"
          :data="convertedTableData"
          :scroll-height="'50vh'" 
          :height="'60vh'"
          :title="`InputTable 발주 목록 (${actualUserType === 'internal' ? '내부직원용' : '공급업체용'})`"
          dataKey="purcDCd"
          :buttons="materialTableButtons"
          :enableRowActions="false"
          :enableSelection="false"
          @dataChange="(newData) => console.log('InputTable 데이터 변경:', newData)"
        />

        <!-- 🔥 원본 데이터 직접 테스트 -->
        <div class="mt-4 p-4 bg-blue-50 border border-blue-200 rounded">
          <h6 class="text-blue-800">🧪 원본 데이터 직접 테스트</h6>
          <InputTable
            :key="`raw-test-${Date.now()}`"
            :columns="[
              { field: 'purcDCd', header: '발주상세코드', type: 'readonly' },
              { field: 'mateName', header: '자재명', type: 'readonly' },
              { field: 'purcQty', header: '수량', type: 'readonly' },
              { field: 'unit', header: '단위', type: 'readonly' }
            ]"
            :data="[
              { purcDCd: 'TEST-001', mateName: '테스트자재1', purcQty: 100, unit: 'kg' },
              { purcDCd: 'TEST-002', mateName: '테스트자재2', purcQty: 200, unit: 'ea' }
            ]"
            :scroll-height="'30vh'" 
            :height="'40vh'"
            title="🧪 하드코딩 테스트 데이터"
            dataKey="purcDCd"
            :buttons="{ save: { show: false }, reset: { show: false } }"
            :enableRowActions="false"
            :enableSelection="false"
          />
        </div>

        <!-- 강제 새로고침 버튼 (테스트용) -->
        <div class="mt-4" v-if="showTestControls">
          <button @click="loadPurchaseData" class="p-button p-button-secondary mr-2">
            데이터 강제 새로고침
          </button>
          <button @click="forceRender" class="p-button p-button-info">
            강제 재렌더링
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