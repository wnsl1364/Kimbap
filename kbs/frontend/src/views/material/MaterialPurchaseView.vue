<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue';
import { useMaterialStore } from '@/stores/materialStore';
import { useMemberStore } from '@/stores/memberStore';
import { useToast } from 'primevue/usetoast';
import { getPurchaseOrdersForView } from '@/api/materials';
import { useRouter } from 'vue-router';
import { useRoute } from 'vue-router';
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import { format, isValid } from 'date-fns';
import { useCommonStore } from '@/stores/commonStore';
import InputTable from '@/components/kimbap/table/InputTable.vue';

// Store들
const materialStore = useMaterialStore();
const memberStore = useMemberStore();
const common = useCommonStore();
const toast = useToast();
const router = useRouter();
const route = useRoute();
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

// 단위코드 변환 (기존 함수 재사용)
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
const showTestControls = ref(false);

const materialTableButtons = ref({
  add: { show: false },
  edit: { show: false },
  delete: { show: false },
  save: { show: false },
  excel: { show: true, label: '엑셀 다운로드', severity: 'success' }
});

const actualUserType = computed(() => {
  if (showTestControls.value) return userType.value;
  
  const memType = memberStore.user?.memType;
  if (memType === 'p1' || memType === 'p4') return 'internal';
  if (memType === 'p3') return 'supplier';
  return 'internal';
});

// 검색 컬럼: 입고 메뉴(from=inbound)에서 온 경우 발주상태 기본값을 '입고 대기(c3)'로 셋팅
const searchColumns = computed(() => {
  const base = actualUserType.value === 'internal'
    ? materialStore.internalPurchaseSearchColumns
    : materialStore.supplierPurchaseSearchColumns;

  // route.query.from === 'inbound' 일 때만 기본값 주입 (SearchForm은 column.default 우선 사용)
  if (route.query.from === 'inbound') {
    return base.map(col => col.key === 'purcDStatus'
      ? { ...col, default: 'c3' } // '입고 대기' value
      : { ...col });
  }
  // 그대로 반환 (참조 유지하지 않도록 map으로 새 배열 생성 -> SearchForm 재초기화 유도)
  return base.map(col => ({ ...col }));
});

// InputTable용 컬럼 정의
const inputTableColumns = computed(() => {
  const baseColumns = [
    {
      field: 'purcCd',
      header: '발주번호',
      type: 'clickable',
      align: 'center',
      width: '120px'
    },
    {
      field: 'purcDCd', 
      header: '발주상세번호',
      type: 'readonly',
      align: 'center',
      width: '130px'
    },
    {
      field: 'mateName',
      header: '자재명',
      type: 'readonly',
      align: 'left',
      width: '150px'
    },
    {
      field: 'cpName',
      header: '거래처명',
      type: 'readonly',
      align: 'left',
      width: '120px'
    },
    {
      field: 'purcQty',
      header: '수량',
      type: 'readonly',
      align: 'right',
      width: '80px'
    },
    {
      field: 'unit',
      header: '단위',
      type: 'readonly',
      align: 'center',
      width: '60px'
    },
    {
      field: 'unitPrice',
      header: '단가(원)',
      type: 'readonly',
      align: 'right',
      width: '100px'
    },
    {
      field: 'totalAmount',
      header: '총액(원)',
      type: 'readonly',
      align: 'right',
      width: '120px'
    },
    {
      field: 'exDeliDt',
      header: '납기예정일',
      type: 'readonly',
      align: 'center',
      width: '110px'
    },
    {
      field: 'purcDStatus',
      header: '발주상태',
      type: 'readonly',
      align: 'center',
      width: '80px'
    },
    {
      field: 'note',
      header: '비고',
      type: 'readonly',
      align: 'left',
      width: '150px'
    }
  ];

  // 사용자 타입별 추가 컬럼
  if (actualUserType.value === 'internal') {
    // 내부직원용: 실제납기일, 등록자, 주문일자 추가
    baseColumns.splice(1, 0, {
      field: 'ordDt',
      header: '주문일자',
      type: 'readonly',
      align: 'center',
      width: '100px'
    });
    
    // baseColumns.splice(2, 0, {
    //   field: 'regiName',
    //   header: '등록자',
    //   type: 'readonly',
    //   align: 'center',
    //   width: '80px'
    // });
    
    baseColumns.splice(11, 0, {
      field: 'deliDt',
      header: '실제납기일',
      type: 'readonly',
      align: 'center',
      width: '110px'
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

const cleanConvertedData = computed(() => {
  if (!cleanPurchaseData.value || !Array.isArray(cleanPurchaseData.value)) {
    return [];
  }
  
  // 날짜 포맷팅
  const formattedData = cleanPurchaseData.value.map(item => ({
    ...item,
    ordDt: formatDate(item.ordDt),
    exDeliDt: formatDate(item.exDeliDt),
    deliDt: formatDate(item.deliDt),
    // 숫자 포맷팅 추가
    unitPrice: Number(item.unitPrice || 0).toLocaleString(),
    totalAmount: Number(item.totalAmount || 0).toLocaleString()
  }));
  
  // 단위코드 변환
  const converted = convertUnitCodes(formattedData);
  
  return converted;
});

const currentCpCd = computed(() =>
  memberStore.user?.cpCd || memberStore.user?.cpCode || memberStore.user?.cp_code || null
);

const onSearch = async (searchData) => {
  try {
    isLoading.value = true;
    // 공급업체는 자신의 거래처 코드로 강제 필터링
    const enforcedParams = actualUserType.value === 'supplier'
      ? { ...searchData, cpCd: currentCpCd.value }
      : searchData;
    const response = await getPurchaseOrdersForView(enforcedParams, actualUserType.value);
    cleanPurchaseData.value = response.data;
    
    toast.add({
      severity: 'success',
      summary: '검색 완료',
      detail: `${response.data.length}건의 발주 데이터를 조회했습니다.`,
      life: 3000
    });
    
  } catch (error) {
    console.error('검색 실패:', error);
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
  loadCleanPurchaseData();
  toast.add({
    severity: 'info',
    summary: '초기화 완료',
    detail: '검색 조건이 초기화되고 전체 목록을 조회했습니다.',
    life: 3000
  });
};

// 🎯 깔끔한 데이터 로드!
const loadCleanPurchaseData = async () => {
  try {
    isLoading.value = true;
    console.log('데이터 로드 시작 - 사용자 타입:', actualUserType.value);
    // 공급업체는 자신의 거래처 코드로만 로드
    const baseParams = actualUserType.value === 'supplier' && currentCpCd.value
      ? { cpCd: currentCpCd.value }
      : {};
    const response = await getPurchaseOrdersForView(baseParams, actualUserType.value);
    cleanPurchaseData.value = response.data;

    console.log('깔끔한 데이터 로드 완료:', response.data.length, '건');

  } catch (error) {
    console.error('깔끔한 데이터 로드 실패:', error);
    loadCleanSampleData();
  } finally {
    isLoading.value = false;
  }
};

const loadCleanSampleData = () => {
  console.log('깔끔한 샘플 데이터 로드');
  
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
  // 공급업체는 자신의 거래처 데이터만 노출
  if (actualUserType.value === 'supplier' && currentCpCd.value) {
    cleanPurchaseData.value = cleanSampleData.filter(it => it.cpCd === currentCpCd.value);
  } else {
    cleanPurchaseData.value = cleanSampleData;
  }
  console.log('샘플 데이터 설정 완료');
};

const handleRowClick = (rowData) => {
  console.log('[MaterialPurchaseView.vue] 라우터 이동 대상:', rowData)
  console.log('[MaterialPurchaseView.vue] 사용자 정보:', {
    memType: memberStore.user?.memType,
    empName: memberStore.user?.empName,
    actualUserType: actualUserType.value
  })
  
  const purcCd = rowData.purcCd
  const purcDStatus = rowData.purcDStatus
  const memType = memberStore.user?.memType

  console.log('[MaterialPurchaseView.vue] 상태 확인:', {
    purcCd,
    purcDStatus,
    purcStatus: rowData.purcStatus,
    isC3Status: purcDStatus === '입고 대기' || purcDStatus === '입고대기' || rowData.purcDStatus === 'c3'
  })

  if (!purcCd) return;

  // 사용자 타입에 따른 페이지 이동
  if (memType === 'p3' || memType === 'p5') {
    console.log('[MaterialPurchaseView.vue] 공급업체 → 발주승인 페이지')
    // 공급업체는 모든 발주를 승인 페이지로
    router.push({ path: '/material/MaterialPurchaseApproval', query: { purcCd } })
  } else if (memType === 'p1' || memType === 'p4' || memType === 'p5') {
    // 내부직원(사원, 담당자): 입고대기 상태만 자재입고 페이지로, 나머지는 발주승인 페이지로
    if (purcDStatus === '입고 대기' || purcDStatus === '입고대기' || rowData.purcDStatus === 'c3') {
      console.log('[MaterialPurchaseView.vue] 내부직원 + 입고대기 → 자재입고 페이지')
      router.push({ path: '/material/materialInbound', query: { purcCd } })
    } else {
      console.log('[MaterialPurchaseView.vue] 내부직원 + 다른상태 → 발주승인 페이지')
      router.push({ path: '/material/MaterialPurchaseApproval', query: { purcCd } })
    }
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
  // 입고 화면에서 넘어온 경우: 기본 필터(입고대기 c3) + 즉시 검색 실행
  if (route.query.from === 'inbound') {
    // 불필요한 전체 로드 대신 바로 상태 필터 검색 실행
    await onSearch({ purcDStatus: 'c3' });
  } else {
    // 일반 진입은 전체 데이터 로드
    loadCleanPurchaseData();
  }
});
</script>

<template>

        <!-- 현재 사용자 정보 -->
        <!-- <div class="mb-4 p-3 border-round surface-100">
          <div class="flex align-items-center gap-3">
            <i class="pi pi-user text-primary"></i>
            <div>
              <strong>
                {{ 
                  memberStore.user?.memType === 'p1' 
                    ? (memberStore.user?.empName || '테스트 사용자')
                    : memberStore.user?.memType === 'p3'
                    ? (memberStore.user?.cpName || '테스트 거래처')
                    : '테스트 사용자'
                }}
              </strong>
              <span class="ml-2 text-500">
                ({{ actualUserType === 'internal' ? '내부직원' : '공급업체직원' }})
              </span>
            </div>
          </div>
        </div> -->

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
          title="자재 발주 조회"
          :columns="searchColumns"
          @search="onSearch"
          :gridColumns="4"
          @reset="onReset"
        />

        <div class="mt-4">
        <!-- InputTable -->
        <InputTable
          :key="`purchase-table-${actualUserType}`"
          :columns="inputTableColumns"
          :data="cleanConvertedData"
          :scroll-height="'42vh'" 
          :height="'52vh'"
          :title="`발주 목록 조회 (${actualUserType === 'internal' ? '내부직원용' : '공급업체용'})`"
          dataKey="purcCd"
          :buttons="materialTableButtons"
          :enableRowActions="false"
          :enableSelection="false"
          @rowClick="handleRowClick"
          :enableRowClick="true"
          @dataChange="(newData) => console.log('InputTable 데이터 변경:', newData)"
        />
        </div>
</template>

<style scoped>
.field-radiobutton {
  display: inline-flex;
  align-items: center;
  margin-right: 1rem;
}
</style>