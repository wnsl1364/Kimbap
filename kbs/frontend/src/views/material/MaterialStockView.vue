<script setup>
import { ref, computed, onMounted } from 'vue';
import { storeToRefs } from 'pinia';
import { useToast } from 'primevue/usetoast';
import { useMaterialStore } from '@/stores/materialStore';
import { useCommonStore } from '@/stores/commonStore';
import { getMaterialStockStatus, getStockAlerts, exportStockStatusToExcel, getMaterialLotStock } from '@/api/materials';

// 🎯 기존 프로젝트 컴포넌트만 사용!
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import InputTable from '@/components/kimbap/table/InputTable.vue';
import BasicModal from '@/components/kimbap/modal/basicModal.vue';

// 🏪 Store 설정
const materialStore = useMaterialStore();
const commonStore = useCommonStore();
const toast = useToast();

// 📊 반응형 데이터
const stockStatusData = ref([]);
const stockStatistics = ref({});
const stockStatusLoading = ref(false);
const searchParams = ref({});
const { materialTypeOptions } = storeToRefs(materialStore);

// 🔍 LOT별 재고 모달 관련 데이터 (BasicModal 사용!)
const lotStockModalVisible = ref(false);
const lotStockData = ref([]);
const selectedMaterialInfo = ref({
  materialCode: '',
  materialName: ''
});

// 🏷️ LOT별 재고 모달 컬럼 설정
const lotStockColumns = ref([
  { field: 'lotNo', header: 'LOT번호' },
  { field: 'supplierLotNo', header: '공급업체LOT' },
  { field: 'quantity', header: '재고수량' },
  { field: 'unit', header: '단위' },
  { field: 'inboundDate', header: '입고일자' },
  { field: 'expiryDate', header: '유효기간' },
  { field: 'warehouseName', header: '창고명' },
  { field: 'storageCondition', header: '보관조건' },
  { field: 'note', header: '비고' }
]);

// 🔍 SearchForm 설정
const searchColumns = computed(() => [
  {
    key: 'mcode',
    label: '자재코드',
    type: 'text',
    placeholder: '자재코드를 입력하세요'
  },
  {
    key: 'mateName',
    label: '자재명',
    type: 'text',
    placeholder: '자재명을 입력하세요'
  },
  {
    key: 'mateType',
    label: '자재유형',
    type: 'dropdown',
    options: [
      { label: '전체', value: '' },
      ...materialTypeOptions.value
    ],
    placeholder: '자재유형을 선택하세요'
  },
  {
    key: 'facName',
    label: '공장명',
    type: 'text',
    placeholder: '공장명을 입력하세요'
  }
]);

// 📋 InputTable 컬럼 설정
const stockStatusColumns = ref([
  {
    field: 'materialCode',
    header: '자재코드',
    type: 'readonly',
    width: '120px',
    align: 'center'
  },
  {
    field: 'materialName',
    header: '자재명',
    type: 'readonly',
    width: '170px'
  },
  {
    field: 'materialType',
    header: '자재유형',
    type: 'readonly',
    width: '100px',
    align: 'center'
  },
  {
    field: 'factoryName',
    header: '공장명',
    type: 'readonly',
    width: '120px',
    align: 'center'
  },
  {
    field: 'stockStatus',
    header: '재고상태',
    type: 'readonly',
    width: '100px',
    align: 'center'
  },
  {
    field: 'totalQuantity',
    header: '현재재고',
    type: 'readonly',
    width: '100px',
    align: 'right'
  },
  {
    field: 'safeStock',
    header: '안전재고',
    type: 'readonly',
    width: '100px',
    align: 'right'
  },
  {
    field: 'stockDifference',
    header: '재고차이',
    type: 'readonly',
    width: '100px',
    align: 'right'
  },
  {
    field: 'stockPercentage',
    header: '재고비율(%)',
    type: 'readonly',
    width: '100px',
    align: 'right'
  },
  {
    field: 'lastInboundDate',
    header: '최근입고일',
    type: 'readonly',
    width: '120px',
    align: 'center'
  },
  {
    field: 'unit',
    header: '단위',
    type: 'readonly',
    width: '80px',
    align: 'center'
  },
  {
    field: 'lotAction',
    header: 'LOT조회',
    type: 'button',
    width: '100px',
    align: 'center',
    buttonLabel: 'LOT조회',
    buttonSeverity: 'info',
    buttonEvent: 'lotAction'
  }
]);

// 🛠️ InputTable 버튼 설정
const tableButtons = ref({
  save: { show: false },
  reset: { show: false },
  delete: { show: false },
  add: { show: false },
  edit: { show: false },
  load: { show: true, label: '새로고침', severity: 'info' },
  custom1: { show: true, label: '엑셀다운로드', severity: 'secondary' },
  custom2: { show: true, label: '재고알림', severity: 'warning' }
});

// 📈 계산된 속성들
const totalStockItems = computed(() => stockStatusData.value?.length || 0);

const criticalAlertCount = computed(() => {
  if (!stockStatusData.value) return 0;
  return stockStatusData.value.filter(item => 
    item.stockStatus === 'empty' || item.stockStatus === 'shortage'
  ).length;
});

// 🎨 재고 상태별 텍스트 변환
const getStockStatusText = (status) => {
  const textMap = {
    'empty': '재고없음',
    'shortage': '재고부족', 
    'overstock': '재고과다',
    'normal': '정상'
  };    
  return textMap[status] || status;
};

// 📏 단위 변환 함수 (백엔드에서 이미 변환된 경우 우선 사용)
const getUnitText = (unitCode, unitText) => {
  if (unitText && unitText !== unitCode) {
    return unitText;
  }
  
  if (!unitCode) return '-';
  
  const unitCodes = commonStore.getCodes('0G');
  const unitItem = unitCodes.find(item => item.detailCd === unitCode);
  
  return unitItem ? unitItem.detailNm : unitCode;
};

// 🏭 자재유형 변환 함수 (백엔드에서 이미 변환된 경우 우선 사용)
const getMaterialTypeText = (typeCode, typeText) => {
  if (typeText && typeText !== typeCode) {
    return typeText;
  }
  
  if (!typeCode) return '-';
  
  const typeCodes = commonStore.getCodes('0H');
  const typeItem = typeCodes.find(item => item.detailCd === typeCode);
  
  return typeItem ? typeItem.detailNm : typeCode;
};

// 🔍 검색 기능
const onSearch = async (searchConditions) => {
  console.log('🔍 재고 현황 검색 실행:', searchConditions);
  
  searchParams.value = { ...searchConditions };
  await loadStockStatusData();
};

const onReset = async () => {
  console.log('🔄 검색 조건 초기화');
  
  searchParams.value = {};
  await loadStockStatusData();
};

const emit = defineEmits(['lotAction']);

const handleLotAction = (rowData, column) => {
  console.log('🎯 LOT조회 버튼 클릭:', rowData);
  
  // LOT별 재고 조회 실행!
  viewMaterialLotStock(rowData.materialCode, rowData.materialName);
};

// 📊 데이터 로딩 함수
const loadStockStatusData = async () => {
  try {
    stockStatusLoading.value = true;
    
    console.log('📊 재고 현황 데이터 로딩 시작');
    
    const response = await getMaterialStockStatus(searchParams.value);
    
    if (response.data) {
      stockStatusData.value = response.data.data || [];
      stockStatistics.value = response.data.statistics || {};
      
      // 🔍 디버깅: 첫 번째 아이템의 stockPercentage 확인
      if (stockStatusData.value.length > 0) {
        const firstItem = stockStatusData.value[0];
        console.log('🔍 첫 번째 아이템 stockPercentage 디버깅:', {
          materialCode: firstItem.materialCode,
          materialName: firstItem.materialName,
          stockPercentage: firstItem.stockPercentage,
          type: typeof firstItem.stockPercentage,
          safeStock: firstItem.safeStock,
          totalQuantity: firstItem.totalQuantity,
          calculatedPercentage: firstItem.safeStock && firstItem.safeStock > 0 
            ? ((firstItem.totalQuantity / firstItem.safeStock) * 100).toFixed(2) 
            : 'N/A'
        });
      }
      
      // 재고 상태 텍스트 변환 (백엔드 변환값 우선 사용)
      stockStatusData.value = stockStatusData.value.map(item => ({
        ...item,
        stockStatus: getStockStatusText(item.stockStatus),
        totalQuantity: item.totalQuantity?.toLocaleString() || '0',
        safeStock: item.safeStock?.toLocaleString() || '0',
        stockDifference: item.stockDifference?.toLocaleString() || '0',
        stockPercentage: (item.stockPercentage !== null && item.stockPercentage !== undefined) 
          ? Number(item.stockPercentage).toFixed(1) 
          : (item.safeStock && item.safeStock > 0 && item.totalQuantity !== null) 
            ? ((item.totalQuantity / item.safeStock) * 100).toFixed(1)
            : '-',
        lastInboundDate: item.lastInboundDate ? 
          new Date(item.lastInboundDate).toLocaleDateString('ko-KR') : '-',
        unit: item.unitText || getUnitText(item.unit, item.unitText),
        materialType: item.materialTypeText || getMaterialTypeText(item.materialType, item.materialTypeText)
      }));
      
      console.log('✅ 재고 현황 로딩 완료:', response.data.totalCount + '건');
      
      toast.add({
        severity: 'success',
        summary: '조회 완료',
        detail: `재고 현황 ${response.data.totalCount}건을 조회했습니다.`,
        life: 3000
      });
      
    } else {
      console.warn('⚠️ 응답 데이터가 없습니다.');
      stockStatusData.value = [];
    }
    
  } catch (error) {
    console.error('❌ 재고 현황 로딩 실패:', error);
    
    toast.add({
      severity: 'error',
      summary: '조회 실패',
      detail: '재고 현황을 불러오는데 실패했습니다.',
      life: 5000
    });
    
    stockStatusData.value = [];
    
  } finally {
    stockStatusLoading.value = false;
  }
};

// 🔍 LOT별 재고 조회 (BasicModal로 변경!!)
const viewMaterialLotStock = async (materialCode, materialName) => {
  try {
    console.log('🔍 LOT별 재고 조회 시작:', materialCode);
    
    // 🎯 선택된 자재 정보 설정
    selectedMaterialInfo.value = {
      materialCode: materialCode,
      materialName: materialName
    };
    
    // 🚀 실제 API 호출
    const response = await getMaterialLotStock(materialCode);
    
    if (response.data && response.data.length > 0) {
      // 🔥 LOT 데이터 가공 (날짜 포맷팅 등)
      lotStockData.value = response.data.map(lot => ({
        ...lot,
        quantity: lot.quantity?.toLocaleString() || '0',
        inboundDate: lot.inboundDate ? 
          new Date(lot.inboundDate).toLocaleDateString('ko-KR') : '-',
        expiryDate: lot.expiryDate ? 
          new Date(lot.expiryDate).toLocaleDateString('ko-KR') : '-',
        unit: lot.unitText || lot.unit || '-',
        storageCondition: lot.storageConditionText || lot.storageCondition || '-',
        warehouseName: lot.warehouseName || '-',
        note: lot.note || '-'
      }));
      
      // 🎯 모달 열기!
      lotStockModalVisible.value = true;
      
      console.log('✅ LOT별 재고 조회 완료:', response.data.length + '건');
      
    } else {
      // 📝 LOT 정보가 없는 경우
      lotStockData.value = [];
      lotStockModalVisible.value = true; // 빈 모달이라도 보여주기
      
      toast.add({
        severity: 'warn',
        summary: 'LOT 정보 없음',
        detail: `${materialName}(${materialCode})에 대한 LOT별 재고 정보가 없습니다.`,
        life: 4000
      });
    }
    
  } catch (error) {
    console.error('❌ LOT별 재고 조회 실패:', error);
    
    toast.add({
      severity: 'error',
      summary: 'LOT 조회 실패',
      detail: `${materialName}(${materialCode})의 LOT별 재고 조회에 실패했습니다.`,
      life: 5000
    });
  }
};

// ⚠️ 재고 알림 조회
const loadStockAlerts = async () => {
  try {
    console.log('⚠️ 재고 알림 조회');
    
    const response = await getStockAlerts('all');
    
    if (response.data) {
      const alertCount = response.data.alertCount || 0;
      
      toast.add({
        severity: alertCount > 0 ? 'warn' : 'info',
        summary: '재고 알림',
        detail: alertCount > 0 ? 
          `${alertCount}건의 재고 알림이 있습니다.` : 
          '현재 재고 알림이 없습니다.',
        life: 5000
      });
    }
    
  } catch (error) {
    console.error('❌ 재고 알림 조회 실패:', error);
    
    toast.add({
      severity: 'error',
      summary: '알림 조회 실패',
      detail: '재고 알림을 불러오는데 실패했습니다.',
      life: 5000
    });
  }
};

// 📊 엑셀 다운로드
const downloadExcel = async () => {
  try {
    console.log('📊 엑셀 다운로드 시작');
    
    const response = await exportStockStatusToExcel(searchParams.value);
    
    toast.add({
      severity: 'info',
      summary: '다운로드 준비',
      detail: '엑셀 파일 다운로드를 준비 중입니다.',
      life: 3000
    });
    
  } catch (error) {
    console.error('❌ 엑셀 다운로드 실패:', error);
    
    toast.add({
      severity: 'error',
      summary: '다운로드 실패',
      detail: '엑셀 다운로드에 실패했습니다.',
      life: 5000
    });
  }
};

// 🔄 데이터 새로고침
const refreshData = async () => {
  await loadStockStatusData();
};

// 🎯 InputTable 버튼 및 액션 핸들러
const handleTableAction = (action, data) => {
  console.log('📋 테이블 액션:', action, data);
  
  switch (action) {
    case 'load':
      refreshData();
      break;
    case 'custom1':
      downloadExcel();
      break;
    case 'custom2':
      loadStockAlerts();
      break;
    default:
      console.log('처리되지 않은 액션:', action);
  }
};

// 🎯 행별 액션 핸들러 (새로 추가!!)
const handleRowAction = (action, rowData) => {
  console.log('🎯 행 액션 실행:', action, rowData);
  
  switch (action) {
    case 'lot':
      // 🔍 LOT별 재고 조회 (자재코드와 자재명 함께 전달!)
      viewMaterialLotStock(rowData.materialCode, rowData.materialName);
      break;
    case 'view':
      // 🔍 상세 보기
      toast.add({
        severity: 'info',
        summary: '상세 보기',
        detail: `${rowData.materialName}(${rowData.materialCode})의 상세 정보를 조회합니다.`,
        life: 3000
      });
      break;
    case 'edit':
      // ✏️ 수정
      toast.add({
        severity: 'info',
        summary: '수정',
        detail: `${rowData.materialName}(${rowData.materialCode})를 수정합니다.`,
        life: 3000
      });
      break;
    default:
      console.log('처리되지 않은 행 액션:', action);
  }
};

// 🎯 컴포넌트 마운트 시 초기 데이터 로딩
onMounted(async () => {
  console.log('🎯 MaterialStockView 컴포넌트 마운트');
  
  try {
    await Promise.all([
      commonStore.fetchCommonCodes('0H'), // 자재유형
      commonStore.fetchCommonCodes('0G'), // 단위
      commonStore.fetchCommonCodes('0E')  // 상태
    ]);
    
    console.log('✅ 공통코드 로딩 완료');
    
    await loadStockStatusData();
    
  } catch (error) {
    console.error('❌ 초기 데이터 로딩 실패:', error);
    
    toast.add({
      severity: 'error',
      summary: '초기 로딩 실패',
      detail: '페이지 초기화에 실패했습니다.',
      life: 5000
    });
  }
});
</script>

<template>
  <div class="material-stock-view">
    <!-- 🔍 검색 폼 (기존 SearchForm.vue 사용) -->
    <SearchForm 
      :columns="searchColumns"
      :gridColumns="4"
      @search="onSearch"
      @reset="onReset"
    />

    <!-- 📋 재고 현황 테이블 (기존 InputTable.vue 사용) -->
    <InputTable 
      :data="stockStatusData"
      :columns="stockStatusColumns"
      :title="`재고 현황 목록 (${totalStockItems}건 / 긴급알림: ${criticalAlertCount}건)`"
      :buttons="tableButtons"
      :scrollHeight="'55vh'"
      :height="'65vh'"
      :loading="stockStatusLoading"
      :enableRowActions="true"
      @action="handleTableAction"
      @lotAction="handleLotAction"
      @rowAction="handleRowAction"
    />

    <!-- 🔍 LOT별 재고 조회 모달 (BasicModal 사용!) -->
    <BasicModal
      v-model:visible="lotStockModalVisible"
      :items="lotStockData"
      :columns="lotStockColumns"
      :titleName="selectedMaterialInfo.materialName"
      :titleCode="selectedMaterialInfo.materialCode"
    />
  </div>
</template>

<style scoped>
.material-stock-view {
  padding: 1rem;
}
</style>