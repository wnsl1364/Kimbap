<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue';
import { useMaterialStore } from '@/stores/materialStore';
import { useMemberStore } from '@/stores/memberStore';
import { useToast } from 'primevue/usetoast';
// 🎯 새로운 깔끔한 API 함수 import!
import { getPurchaseOrdersForView } from '@/api/materials';
import { useRouter } from 'vue-router';
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import BasicTable from '@/components/kimbap/table/BasicTable.vue';
import RadioButton from 'primevue/radiobutton';
import { format, isValid } from 'date-fns';
import { useCommonStore } from '@/stores/commonStore';
import InputTable from '@/components/kimbap/table/InputTable.vue';

// Store들
const materialStore = useMaterialStore();
const memberStore = useMemberStore();
const common = useCommonStore();
const toast = useToast();
const router = useRouter();

// 🎯 깔끔한 데이터 구조!
const cleanPurchaseData = ref([]);

const formatDate = (date) => {
  if (!date) return '';
  
  try {
    const dateObj = date instanceof Date ? date : new Date(date);
    if (!isValid(dateObj)) return '';
    return format(dateObj, 'yyyy-MM-dd');
  } catch (error) {
    console.error('날짜 포맷 에러:', error);
    return '';
  }
};

// 🎯 단위코드 변환 (기존 함수 재사용)
const convertUnitCodes = (list) => {
  if (!list || !Array.isArray(list)) return [];

  const unitCodes = common.getCodes('0G');
  const statusCodes = common.getCodes('0C');
  const matTypeCodes = common.getCodes('0H');

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

// 반응형 데이터
const userType = ref('internal');
const isLoading = ref(false);
const showTestControls = ref(true);

const materialTableButtons = ref({
  add: { show: false },
  edit: { show: false },
  delete: { show: false },
  save: { show: false }
});

const actualUserType = computed(() => {
  if (showTestControls.value) return userType.value;
  
  const memType = memberStore.user?.memType;
  if (memType === 'p1') return 'internal';
  if (memType === 'p3') return 'supplier';
  return 'internal';
});

const searchColumns = computed(() => {
  return actualUserType.value === 'internal' 
    ? materialStore.internalPurchaseSearchColumns 
    : materialStore.supplierPurchaseSearchColumns;
});

// 🔥 InputTable용 컬럼 정의 (실제 데이터 필드와 매치!)
const inputTableColumns = computed(() => {
  const baseColumns = [
    {
      field: 'purcCd',
      header: '발주번호',
      type: 'clickable',
      align: 'center'
    },
    {
      field: 'purcDCd', 
      header: '발주상세번호',
      type: 'readonly',
      align: 'center'
    },
    {
      field: 'mateName',
      header: '자재명',
      type: 'readonly',
      align: 'left'
    },
    {
      field: 'cpName',
      header: '거래처명',
      type: 'readonly',
      align: 'left'
    },
    {
      field: 'purcQty',
      header: '수량',
      type: 'readonly',
      align: 'right'
    },
    {
      field: 'unit',
      header: '단위',
      type: 'readonly',
      align: 'center'
    },
    {
      field: 'unitPrice',
      header: '단가(원)',
      type: 'readonly',
      align: 'right'
    },
    {
      field: 'totalAmount',
      header: '총액(원)',
      type: 'readonly',
      align: 'right'
    },
    {
      field: 'exDeliDt',
      header: '납기예정일',
      type: 'readonly',
      align: 'center'
    },
    {
      field: 'purcDStatus',
      header: '발주상태',
      type: 'readonly',
      align: 'center'
    },
    {
      field: 'note',
      header: '비고',
      type: 'readonly',
      align: 'left'
    }
  ];

  // 🔥 사용자 타입별 추가 컬럼
  if (actualUserType.value === 'internal') {
    // 내부직원용: 실제납기일, 등록자, 주문일자 추가
    baseColumns.splice(1, 0, {
      field: 'ordDt',
      header: '주문일자',
      type: 'readonly',
      align: 'center'
    });
    
    baseColumns.splice(2, 0, {
      field: 'regiName',
      header: '등록자',
      type: 'readonly',
      align: 'center'
    });
    
    baseColumns.splice(10, 0, {
      field: 'deliDt',
      header: '실제납기일',
      type: 'readonly',
      align: 'center'
    });
  }

  return baseColumns;
});

// BasicTable용 컬럼 (기존 Store 사용)
const currentTableColumns = computed(() => {
  return actualUserType.value === 'internal' 
    ? materialStore.internalPurchaseColumns 
    : materialStore.supplierPurchaseColumns;
});

// 🎯 깔끔한 데이터만 표시!
const cleanConvertedData = computed(() => {
  console.log('🎯 깔끔한 데이터 변환 시작:', cleanPurchaseData.value?.length);
  
  if (!cleanPurchaseData.value || !Array.isArray(cleanPurchaseData.value)) {
    return [];
  }
  
  // 날짜 포맷팅
  const formattedData = cleanPurchaseData.value.map(item => ({
    ...item,
    ordDt: formatDate(item.ordDt),
    exDeliDt: formatDate(item.exDeliDt),
    deliDt: formatDate(item.deliDt),
    // 🔥 숫자 포맷팅 추가
    unitPrice: Number(item.unitPrice || 0).toLocaleString(),
    totalAmount: Number(item.totalAmount || 0).toLocaleString()
  }));
  
  // 단위코드 변환
  const converted = convertUnitCodes(formattedData);
  
  console.log('✅ 깔끔한 데이터 변환 완료:', converted?.length);
  return converted;
});

// 🎯 새로운 깔끔한 API 호출!
const onSearch = async (searchData) => {
  try {
    isLoading.value = true;
    console.log('🎯 깔끔한 검색 시작:', searchData, actualUserType.value);
    
    const response = await getPurchaseOrdersForView(searchData, actualUserType.value);
    cleanPurchaseData.value = response.data;
    
    console.log('✅ 깔끔한 검색 완료:', response.data);
    
    toast.add({
      severity: 'success',
      summary: '검색 완료! 🎉',
      detail: `${response.data.length}건의 깔끔한 발주 데이터를 조회했습니다!`,
      life: 3000
    });
    
  } catch (error) {
    console.error('❌ 깔끔한 검색 실패:', error);
    toast.add({
      severity: 'error',
      summary: '검색 실패 ㅠㅠ',
      detail: '발주 데이터 조회 중 오류가 발생했습니다.',
      life: 3000
    });
  } finally {
    isLoading.value = false;
  }
};

const onReset = () => {
  loadCleanPurchaseData();
  toast.add({
    severity: 'info',
    summary: '초기화 완료 ✨',
    detail: '검색 조건이 초기화되고 전체 목록을 조회했습니다.',
    life: 3000
  });
};

// 🎯 깔끔한 데이터 로드!
const loadCleanPurchaseData = async () => {
  try {
    isLoading.value = true;
    console.log('🎯 깔끔한 데이터 로드 시작 - 사용자 타입:', actualUserType.value);
    
    const response = await getPurchaseOrdersForView({}, actualUserType.value);
    cleanPurchaseData.value = response.data;

    console.log('✅ 깔끔한 데이터 로드 완료:', response.data.length, '건');

  } catch (error) {
    console.error('❌ 깔끔한 데이터 로드 실패:', error);
    loadCleanSampleData();
  } finally {
    isLoading.value = false;
  }
};

// 🎯 깔끔한 샘플 데이터!
const loadCleanSampleData = () => {
  console.log('🧹 깔끔한 샘플 데이터 로드');
  
  const cleanSampleData = [
    {
      id: 'PURC-001-PURC-D-001',
      purcCd: 'PURC-001',
      ordDt: '2025-07-29',
      regi: 'EMP-10001',
      regiName: '김구매',
      purcStatus: 'c2',
      ordTotalAmount: 1500000,
      purcDCd: 'PURC-D-001',
      mcode: 'MAT-1001',
      mateVerCd: 'V1',
      purcQty: 100,
      unit: 'g2',
      unitPrice: 15000,
      exDeliDt: '2025-08-01',
      purcDStatus: 'c1',
      note: '긴급 발주',
      mateName: '김(건조)',
      mateType: 'h1',
      cpCd: 'CP-001',
      cpName: '황금쌀농협',
      totalAmount: 1500000,
      deliDt: null
    },
    {
      id: 'PURC-002-PURC-D-002',
      purcCd: 'PURC-002',
      ordDt: '2025-07-28',
      regi: 'EMP-10002',
      regiName: '이발주',
      purcStatus: 'c2',
      ordTotalAmount: 560000,
      purcDCd: 'PURC-D-002',
      mcode: 'MAT-1002',
      mateVerCd: 'V1',
      purcQty: 200,
      unit: 'g2',
      unitPrice: 2800,
      exDeliDt: '2025-08-05',
      purcDStatus: 'c2',
      note: '정기 발주',
      mateName: '쌀(백미)',
      mateType: 'h1',
      cpCd: 'CP-002',
      cpName: '바다김수산',
      totalAmount: 560000,
      deliDt: '2025-07-30'
    }
  ];
  
  cleanPurchaseData.value = cleanSampleData;
  console.log('🧹 깔끔한 샘플 데이터 설정 완료!');
};

const handleRowClick = (rowData) => {
  console.log('[MaterialPurchaseView.vue] 라우터 이동 대상:', rowData)
  const purcCd = rowData.purcCd
  const memType = memberStore.user?.memType

  if (!purcCd) return;

  if (memType === 'p2') {
    // 매출업체는 주문등록(수정) 페이지로
    router.push({ path: '/material/MaterialPurchaseApproval', query: { purcCd } })
  } else if (['p1', 'p4', 'p5'].includes(memType)) {
    // 사원/관리자/물류는 주문검토 페이지로
    router.push({ path: '/material/MaterialPurchaseApproval', query: { purcCd } })
  } else {
    console.warn('지원되지 않는 사용자 유형입니다:', memType)
  }
}

// 사용자 타입 변경 감지
watch(actualUserType, () => {
  console.log('👤 사용자 타입 변경됨:', actualUserType.value);
  loadCleanPurchaseData();
});

// 초기화
onMounted(async () => {
  await Promise.all([
    common.fetchCommonCodes('0G'),
    common.fetchCommonCodes('0C'),
    common.fetchCommonCodes('0H')
  ]);
  
  await nextTick();
  loadCleanPurchaseData();
});
</script>

<template>
  <div class="grid">
    <div class="col-12">
      <div class="card">
        <h5>자재 구매/발주 관리</h5>

        <!-- 현재 사용자 정보 -->
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
        <!-- <div class="mb-4" v-if="showTestControls">
          <h6>테스트용 권한 변경:</h6>
          <div class="field-radiobutton">
            <RadioButton v-model="userType" inputId="internal" value="internal" />
            <label for="internal" class="ml-2">내부직원 (p1)</label>
          </div>
          <div class="field-radiobutton">
            <RadioButton v-model="userType" inputId="supplier" value="supplier" />
            <label for="supplier" class="ml-2">공급업체직원 (p3)</label>
          </div>
        </div> -->

        <!-- 검색 폼 -->
        <SearchForm 
          :columns="searchColumns"
          @search="onSearch"
          :gridColumns="5"
          @reset="onReset"
        />

        <!-- 🔥 완벽 매핑된 InputTable -->
        <InputTable
          :key="`clean-input-table-${cleanConvertedData?.length || 0}`"
          :columns="inputTableColumns"
          :data="cleanConvertedData"
          :scroll-height="'50vh'" 
          :height="'60vh'"
          :title="`발주 목록 조회 (${actualUserType === 'internal' ? '내부직원용' : '공급업체용'})`"
          dataKey="purcCd"
          :buttons="materialTableButtons"
          :enableRowActions="false"
          :enableSelection="false"
          @rowClick="handleRowClick"
          :enableRowClick="true"
          @dataChange="(newData) => console.log('🎯 깔끔한 InputTable 데이터 변경:', newData)"
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