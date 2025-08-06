<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { storeToRefs } from 'pinia';
import { useRouter } from 'vue-router';
import { useStockMovementStore } from '@/stores/stockMovementStore';
import { useMemberStore } from '@/stores/memberStore';
import { useCommonStore } from '@/stores/commonStore';
import { useToast } from 'primevue/usetoast';
import InputForm from '@/components/kimbap/searchform/inputForm.vue';
import InputTable from '@/components/kimbap/table/InputTable.vue';
import MultipleSelectModal from '@/components/kimbap/modal/multipleselect.vue';
import LocationSelectModal from './LocationSelectModal.vue';

// Store 및 기본 설정
const stockMovementStore = useStockMovementStore();
const memberStore = useMemberStore();
const commonStore = useCommonStore();
const router = useRouter();
const toast = useToast();

// Store에서 상태 가져오기
const { user } = storeToRefs(memberStore);

// 반응형 상태
const headerFormData = ref({});
const itemTableData = ref([]);
const selectedItems = ref([]);
const itemSelectModalVisible = ref(false);
const locationSelectModalVisible = ref(false);
const currentRowForLocation = ref(null);
const isInitialized = ref(false); // 초기화 상태 추적
const formKey = ref(0); // InputForm 강제 재렌더링용 키

// 품목 선택 모달용 데이터
const availableItems = ref([]);
const selectedItemsFromModal = ref([]);
const modalItemsWithQty = ref([]); // 모달에서 수량 입력된 아이템들

// 자재 선택 모달 컬럼 설정 (자재 전용)
const itemModalColumns = computed(() => [
  { field: 'itemCode', header: '자재번호', type: 'readonly' },
  { field: 'itemName', header: '자재명', type: 'readonly' },
  { field: 'lotNo', header: 'LOT번호', type: 'readonly' },
  { field: 'facName', header: '공장', type: 'readonly' },
  { field: 'areaLocation', header: '구역', type: 'readonly' },
  { field: 'qty', header: '재고수량', type: 'readonly', align: 'right' },
  { field: 'moveQty', header: '이동수량', type: 'input', inputType: 'number', align: 'right', placeholder: '수량 입력', required: true }
]);

// 컴포넌트 마운트 시 초기화
onMounted(async () => {
  if (!isInitialized.value) {
    initializeHeaderForm();
    isInitialized.value = true;
  }
  await loadInitialData();
});

// 헤더 폼 초기화
const initializeHeaderForm = () => {
  const now = new Date();
  const today = formatDate(now);
  
  headerFormData.value = {
    reqDt: today,
    requ: user.value?.empCd || '',
    requName: user.value?.empName || '',
    moveType: 'z1',
    moveRea: '',
    note: ''
  };
};

// 날짜 포맷 함수
const formatDate = (date) => {
  if (!date) return '';
  const d = new Date(date);
  if (isNaN(d.getTime())) return '';
  
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  
  return `${year}-${month}-${day}`;
};

// 초기 데이터 로드
const loadInitialData = async () => {
  try {
    // 공통코드 로드
    await Promise.all([
      commonStore.fetchCommonCodes('0G'), // 단위코드
      commonStore.fetchCommonCodes('0H'), // 품목유형코드
    ]);

    // 공장 목록 로드 (품목 선택 모달용)
    if (!stockMovementStore.factoryList || stockMovementStore.factoryList.length === 0) {
      await stockMovementStore.fetchLocationData('factory');
    }

    // 품목 데이터 로드
    await loadAvailableItems();
  } catch (error) {
    toast.add({
      severity: 'error',
      summary: '초기화 실패',
      detail: '페이지 초기화 중 오류가 발생했습니다.',
      life: 3000
    });
  }
};

// 사용 가능한 품목 데이터 로드
const loadAvailableItems = async () => {
  try {
    const factories = stockMovementStore.factoryList || [];
    const allItems = [];
    let materialLoadCount = 0;
    let failedFactories = [];
    
    for (const factory of factories) {
      console.log(`공장 ${factory.fcode} (${factory.facName}) 자재 조회 시작`);
      
      // 자재만 가져오기 (완제품 제외)
      try {
        await stockMovementStore.fetchAvailableItems(factory.fcode, 'material');
        const materials = (stockMovementStore.availableMaterials || []).map(item => ({
          ...item,
          id: `${item.mcode}_${item.lotNo}_${item.wareAreaCd}`,
          itemType: item.mateType || 'h1',
          itemCode: item.mcode,
          itemName: item.mateName,
          mateVerCd: item.mateVerCd || item.mate_ver_cd || '001', // 백엔드에서 가져온 mate_ver_cd 사용
          areaLocation: item.wareAreaCd || `${item.areaRow || ''}${item.areaCol || ''}` || '미지정'
        }));
        
        allItems.push(...materials);
        materialLoadCount += materials.length;
        console.log(`공장 ${factory.fcode} 자재 로드 성공: ${materials.length}건`);
      } catch (error) {
        console.error(`공장 ${factory.fcode} 자재 로드 실패:`, error);
        if (!failedFactories.includes(factory.facName)) {
          failedFactories.push(factory.facName);
        }
      }
      
      // 완제품 이동 기능은 비활성화
      console.log(`공장 ${factory.fcode} 제품 로드 생략 (완제품 이동 기능 비활성화)`);
    }
    
    // 공통코드 변환 적용
    let processedItems = convertItemTypeCodes(allItems);
    processedItems = convertUnitCodes(processedItems);
    
    availableItems.value = processedItems;
    
    // 로딩 결과 요약 출력
    console.log(`품목 데이터 로드 완료 - 총 ${processedItems.length}건 (자재: ${materialLoadCount}건)`);
    
    // 사용자에게 로딩 결과 알림
    if (failedFactories.length > 0) {
      toast.add({
        severity: 'warn',
        summary: '일부 데이터 로드 실패',
        detail: `${failedFactories.join(', ')} 공장의 일부 자재 데이터를 로드할 수 없습니다. 사용 가능한 ${processedItems.length}건의 자재가 로드되었습니다.`,
        life: 5000
      });
    } else {
      toast.add({
        severity: 'success',
        summary: '자재 데이터 로드 완료',
        detail: `총 ${processedItems.length}건의 자재가 로드되었습니다.`,
        life: 3000
      });
    }
    
  } catch (error) {
    console.error('품목 데이터 로드 실패:', error);
    toast.add({
      severity: 'error',
      summary: '데이터 로드 실패',
      detail: '자재 데이터를 불러오는데 실패했습니다.',
      life: 3000
    });
  }
};

// 헤더 폼 필드 설정
const headerFormFields = computed(() => [
  { key: 'reqDt', label: '요청일', type: 'readonly' },
  { key: 'requName', label: '요청자', type: 'readonly' },
  { 
    key: 'moveType', 
    label: '이동유형', 
    type: 'dropdown',
    required: true,
    options: [
      { label: '내부', value: 'z1' },
      { label: '외부', value: 'z2' }
    ]
  },
  { key: 'moveRea', label: '이동사유', type: 'input', required: true, placeholder: '이동사유를 입력하세요' },
  { key: 'note', label: '비고', type: 'input', placeholder: '비고사항을 입력하세요' }
]);

// 자재 테이블 컬럼 설정
const itemTableColumns = computed(() => [
  { field: 'itemCode', header: '자재코드', type: 'readonly' },
  { field: 'itemName', header: '자재명', type: 'readonly' },
  { field: 'lotNo', header: 'LOT번호', type: 'readonly' },
  { field: 'moveQty', header: '수량', type: 'readonly', align: 'right', description: '자재 선택 시 입력됨' },
  { field: 'unitText', header: '단위', type: 'readonly' },
  { field: 'depaLocation', header: '출발위치', type: 'readonly' },
  { field: 'arrLocation', header: '도착위치', type: 'readonly', width: '250px' },
  { 
    field: 'locationSelect', 
    header: '위치선택', 
    type: 'button', 
    buttonLabel: '선택',
    buttonIcon: 'pi pi-map-marker',
    buttonEvent: 'locationSelect',
    width: '100px'
  }
]);

// 헤더 폼 데이터 변경 처리
const handleHeaderDataChange = (newData) => {
  console.log('헤더 데이터 변경 요청:', newData);
  
  // 완전히 빈 객체이거나 모든 값이 빈 문자열인 경우 완전 무시
  if (!newData || Object.keys(newData).length === 0) {
    console.log('빈 데이터 수신 - 완전 무시');
    return;
  }
  
  // 모든 값이 빈 문자열, null, undefined인 경우 무시
  const validValues = Object.entries(newData).filter(([key, value]) => {
    return value !== '' && value !== null && value !== undefined;
  });
  
  if (validValues.length === 0) {
    console.log('모든 필드가 빈 값 - 완전 무시');
    return;
  }
  
  // readonly 필드들은 항상 보존
  const protectedFields = ['reqDt', 'requ', 'requName'];
  const currentData = { ...headerFormData.value };
  
  // 유효한 값만 업데이트
  validValues.forEach(([key, value]) => {
    currentData[key] = value;
  });
  
  // readonly 필드 보호
  protectedFields.forEach(field => {
    if (headerFormData.value[field] && !currentData[field]) {
      currentData[field] = headerFormData.value[field];
    }
  });
  
  headerFormData.value = currentData;
  console.log('최종 업데이트된 헤더 데이터:', headerFormData.value);
};

// 품목 테이블 데이터 변경 처리
const handleItemDataChange = (newData) => {
  // 전체 목록 교체 방지
};

// 체크박스 선택 변경 처리
const handleSelectionChange = (newSelection) => {
  selectedItems.value = newSelection;
};

// 자재 추가 버튼 클릭
const handleAddItem = () => {
  console.log('자재 추가 버튼 클릭됨');
  try {
    selectedItemsFromModal.value = [];
    itemSelectModalVisible.value = true;
    console.log('모달 상태 변경:', itemSelectModalVisible.value);
  } catch (error) {
    console.error('모달 열기 중 오류:', error);
    toast.add({
      severity: 'error',
      summary: '모달 오류',
      detail: '자재 선택 모달을 여는 중 오류가 발생했습니다.',
      life: 3000
    });
  }
};

// 자재 선택 모달에서 자재 선택 완료
const handleItemSelect = (selectedItemsFromModal) => {
  try {
    let hasError = false;
    
    selectedItemsFromModal.forEach(item => {
      // 모달에서 입력된 수량 확인
      if (!item.moveQty || item.moveQty <= 0) {
        toast.add({
          severity: 'warn',
          summary: '수량 입력 필요',
          detail: `${item.itemName}의 이동수량을 입력해주세요.`,
          life: 3000
        });
        hasError = true;
        return;
      }

      // 입력된 수량이 재고수량을 초과하는지 확인
      if (item.moveQty > item.qty) {
        toast.add({
          severity: 'warn',
          summary: '수량 초과',
          detail: `${item.itemName}의 이동수량(${item.moveQty})이 재고수량(${item.qty})을 초과할 수 없습니다.`,
          life: 3000
        });
        hasError = true;
        return;
      }
    });

    // 오류가 있으면 처리 중단
    if (hasError) {
      return;
    }

    selectedItemsFromModal.forEach(item => {
      const existingItem = itemTableData.value.find(existing => 
        existing.itemCode === item.itemCode && 
        existing.lotNo === item.lotNo && 
        existing.depaAreaCd === item.wareAreaCd
      );
      
      if (!existingItem) {
        const newItem = {
          id: Date.now() + Math.random(),
          itemType: item.itemType,
          itemTypeText: item.typeText || getDefaultItemTypeText(item.itemType),
          itemCode: item.itemCode,
          itemName: item.itemName,
          lotNo: item.lotNo,
          currentStock: item.qty,
          moveQty: item.moveQty, // 모달에서 입력된 수량 사용
          unit: item.unit,
          unitText: item.unitText || convertSingleUnitText(item.unit),
          depaAreaCd: item.wareAreaCd,
          depaWareCd: item.wcode || null, // 출발 창고 코드 추가
          depaLocation: `${item.facName} / ${item.wareName} / ${item.wareAreaCd}`,
          arrAreaCd: '',
          arrWareCd: null, // 도착 창고 코드 초기화
          arrLocation: '선택해주세요',
          locationSelect: '', // 위치선택 버튼용 빈 필드
          mcode: item.mcode,
          mateVerCd: item.mateVerCd,
          pcode: item.pcode,
          prodVerCd: item.prodVerCd,
          // LocationSelectModal에서 필요한 원본 자재 정보 추가
          fcode: item.fcode,
          facName: item.facName,
          mateName: item.itemName,
          stoCon: item.stoCon
        };
        
        itemTableData.value.push(newItem);
      } else {
        // 기존 아이템이 있는 경우 수량 업데이트
        existingItem.moveQty = item.moveQty;
      }
    });
    
    toast.add({
      severity: 'success',
      summary: '자재 추가 완료',
      detail: `${selectedItemsFromModal.length}개 자재가 추가되었습니다.`,
      life: 3000
    });
  } catch (error) {
    console.error('자재 추가 중 오류:', error);
    toast.add({
      severity: 'error',
      summary: '자재 추가 실패',
      detail: '자재 추가 중 오류가 발생했습니다.',
      life: 3000
    });
  }
};

// 도착위치 선택 버튼 클릭
const handleLocationSelect = (rowData) => {
  if (!rowData.moveQty || rowData.moveQty <= 0) {
    toast.add({
      severity: 'warn',
      summary: '수량 입력 필요',
      detail: '이동수량을 먼저 입력해주세요.',
      life: 3000
    });
    return;
  }
  
  if (rowData.moveQty > rowData.currentStock) {
    toast.add({
      severity: 'warn',
      summary: '수량 초과',
      detail: `이동수량이 현재재고(${rowData.currentStock})를 초과할 수 없습니다.`,
      life: 3000
    });
    return;
  }
  
  currentRowForLocation.value = {
    ...rowData,
    // 모달에서 사용할 정보 추가
    totalQty: rowData.moveQty
  };
  locationSelectModalVisible.value = true;
};

// 도착위치 선택 완료
const handleLocationSelectConfirm = (locationData) => {
  console.log('도착위치 선택 완료 데이터:', locationData);
  console.log('현재 선택된 행:', currentRowForLocation.value);
  
  if (currentRowForLocation.value && locationData) {
    const targetRowIndex = itemTableData.value.findIndex(item => 
      item.id === currentRowForLocation.value.id
    );
    
    if (targetRowIndex !== -1) {
      // 첫 번째 배치 계획의 구역 정보를 사용 (단일 구역 선택으로 가정)
      const firstPlan = locationData.placementPlan && locationData.placementPlan[0];
      
      if (firstPlan) {
        // 반응형 업데이트를 위해 새 객체로 교체
        const updatedItem = {
          ...itemTableData.value[targetRowIndex],
          arrAreaCd: firstPlan.wareAreaCd,
          arrLocation: `${locationData.facName} / ${locationData.wareName} / ${firstPlan.wareAreaCd}`,
          arrFcode: locationData.fcode,
          arrWcode: locationData.wcode,
          arrWareCd: locationData.wcode // 백엔드 매핑용 필드명 추가
        };
        
        // 배열 요소 교체로 반응형 업데이트 보장
        itemTableData.value.splice(targetRowIndex, 1, updatedItem);
        
        console.log('도착위치 업데이트 완료:', updatedItem.arrLocation);
        
        toast.add({
          severity: 'success',
          summary: '도착위치 선택 완료',
          detail: `도착위치: ${updatedItem.arrLocation}`,
          life: 3000
        });
      } else {
        console.error('배치 계획 정보가 없습니다.');
        toast.add({
          severity: 'error',
          summary: '도착위치 설정 실패',
          detail: '배치 계획 정보가 없습니다.',
          life: 3000
        });
      }
    } else {
      console.error('대상 행을 찾을 수 없습니다.');
      toast.add({
        severity: 'error',
        summary: '도착위치 설정 실패',
        detail: '대상 행을 찾을 수 없습니다.',
        life: 3000
      });
    }
  }
  
  currentRowForLocation.value = null;
  locationSelectModalVisible.value = false;
};

// 선택된 자재 제거
const handleRemoveSelectedItems = () => {
  if (selectedItems.value.length === 0) {
    toast.add({
      severity: 'warn',
      summary: '선택 필요',
      detail: '제거할 자재를 선택해주세요.',
      life: 3000
    });
    return;
  }
  
  const selectedIds = selectedItems.value.map(item => item.id);
  itemTableData.value = itemTableData.value.filter(item => !selectedIds.includes(item.id));
  selectedItems.value = [];
  
  toast.add({
    severity: 'success',
    summary: '자재 제거 완료',
    detail: `${selectedIds.length}개 자재가 제거되었습니다.`,
    life: 3000
  });
};

// 저장 처리
const handleSave = async () => {
  // 저장 시작 전 헤더 데이터 백업 (깊은 복사)
  const headerDataBackup = JSON.parse(JSON.stringify(headerFormData.value));
  console.log('저장 전 백업 데이터:', headerDataBackup);
  
  try {
    console.log('저장 시작...');
    console.log('저장 전 헤더 데이터:', headerFormData.value);
    
    // 폼 유효성 검증
    if (!validateForm()) {
      console.log('폼 유효성 검증 실패 - 저장 중단');
      
      // 즉시 복원
      headerFormData.value = { ...headerDataBackup };
      console.log('즉시 헤더 데이터 복원:', headerFormData.value);
      
      // InputForm 컴포넌트 강제 재렌더링
      formKey.value += 1;
      
      // 추가 지연 복원 (컴포넌트가 다시 렌더링된 후)
      setTimeout(() => {
        headerFormData.value = { ...headerDataBackup };
        console.log('지연된 헤더 데이터 재복원:', headerFormData.value);
      }, 200);
      
      return;
    }
    
    // 헤더 데이터 구성
    const header = {
      moveType: headerFormData.value.moveType,
      moveRea: headerFormData.value.moveRea,
      note: headerFormData.value.note || '',
      requ: user.value?.empCd || headerFormData.value.requ
    };
    
    // 상세 데이터 구성
    const details = itemTableData.value.map(item => {
      console.log('원본 아이템 데이터:', item); // 디버그 로그
      console.log('mateVerCd 값:', item.mateVerCd);
      console.log('mate_ver_cd 값:', item.mate_ver_cd);
      
      const detailItem = {
        mcode: item.mcode || null,
        mateVerCd: item.mateVerCd || item.mate_ver_cd || '001', // 기본값 추가
        pcode: item.pcode || null,
        prodVerCd: item.prodVerCd || null,
        itemType: item.itemType,
        lotNo: item.lotNo,
        moveQty: item.moveQty,
        unit: item.unit,
        depaAreaCd: item.depaAreaCd,
        arrAreaCd: item.arrAreaCd,
        // 창고 코드 추가
        depaWareCd: item.depaWareCd || null,
        arrWareCd: item.arrWcode || null,
        // 추가 정보
        itemCode: item.itemCode,
        itemName: item.itemName
      };
      
      console.log('전송할 데이터 아이템:', detailItem);
      return detailItem;
    });
    
    const requestData = {
      header: header,
      details: details
    };
    
    console.log('전송할 데이터:', requestData);
    
    // 로딩 상태 표시
    toast.add({
      severity: 'info',
      summary: '등록 중',
      detail: '이동요청서를 등록하고 있습니다...',
      life: 2000
    });
    
    const result = await stockMovementStore.registerNewMoveRequest(header, details);
    
    console.log('등록 결과:', result);
    
    toast.add({
      severity: 'success',
      summary: '등록 완료',
      detail: `이동요청서가 성공적으로 등록되었습니다. (${result.moveReqCd || '요청번호 생성됨'})`,
      life: 5000
    });
    
    // 성공한 경우에만 목록 페이지로 이동
    router.push('/stock-movement/list');
    
  } catch (error) {
    console.error('등록 실패:', error);
    console.log('에러 발생 시 백업 데이터:', headerDataBackup);
    
    // 에러 발생 시 무조건 백업 데이터로 복원
    headerFormData.value = { ...headerDataBackup };
    console.log('에러 시 헤더 데이터 복원:', headerFormData.value);
    
    // InputForm 컴포넌트 강제 재렌더링
    formKey.value += 1;
    
    // 추가 지연 복원
    setTimeout(() => {
      headerFormData.value = { ...headerDataBackup };
      console.log('에러 시 지연된 헤더 데이터 재복원:', headerFormData.value);
    }, 200);
    
    let errorMessage = '이동요청서 등록 중 오류가 발생했습니다.';
    
    if (error.response) {
      // 서버에서 반환한 에러 메시지가 있는 경우
      errorMessage = error.response.data?.message || error.response.data?.detail || errorMessage;
      console.error('서버 에러 응답:', error.response.data);
    }
    
    // 에러 발생 시 폼 데이터는 그대로 유지하고 에러 메시지만 표시
    toast.add({
      severity: 'error',
      summary: '등록 실패',
      detail: errorMessage,
      life: 5000
    });
    
    // 에러 발생 시에는 페이지 이동하지 않고 현재 페이지에 머무름
    // 사용자가 입력한 데이터가 그대로 유지됨
  }
};

// 폼 유효성 검증
const validateForm = () => {
  console.log('폼 유효성 검증 시작...');
  console.log('헤더 데이터:', headerFormData.value);
  console.log('자재 데이터:', itemTableData.value);
  
  // 1. 기본정보 검증
  if (!headerFormData.value.moveType) {
    toast.add({
      severity: 'warn',
      summary: '입력 확인',
      detail: '이동유형을 선택해주세요.',
      life: 3000
    });
    return false;
  }
  
  if (!headerFormData.value.moveRea || headerFormData.value.moveRea.trim() === '') {
    toast.add({
      severity: 'warn',
      summary: '입력 확인',
      detail: '이동사유를 입력해주세요.',
      life: 3000
    });
    return false;
  }
  
  // 2. 자재 목록 검증
  if (itemTableData.value.length === 0) {
    toast.add({
      severity: 'warn',
      summary: '자재 선택 필요',
      detail: '이동할 자재를 추가해주세요.',
      life: 3000
    });
    return false;
  }
  
  // 3. 각 자재별 상세 검증
  for (let i = 0; i < itemTableData.value.length; i++) {
    const item = itemTableData.value[i];
    
    // 이동수량 검증
    if (!item.moveQty || item.moveQty <= 0) {
      toast.add({
        severity: 'warn',
        summary: '수량 입력 필요',
        detail: `${i + 1}번째 자재(${item.itemName})의 이동수량을 입력해주세요.`,
        life: 3000
      });
      return false;
    }
    
    // 재고 초과 검증
    if (item.moveQty > item.currentStock) {
      toast.add({
        severity: 'warn',
        summary: '수량 초과',
        detail: `${i + 1}번째 자재(${item.itemName})의 이동수량이 현재재고를 초과합니다.`,
        life: 3000
      });
      return false;
    }
    
    // 도착위치 선택 검증
    if (!item.arrAreaCd || item.arrAreaCd.trim() === '') {
      toast.add({
        severity: 'warn',
        summary: '도착위치 선택 필요',
        detail: `${i + 1}번째 자재(${item.itemName})의 도착위치를 선택해주세요.`,
        life: 3000
      });
      return false;
    }
    
    // 필수 필드 검증
    if (!item.itemCode || !item.lotNo) {
      toast.add({
        severity: 'error',
        summary: '데이터 오류',
        detail: `${i + 1}번째 자재의 필수 정보가 누락되었습니다.`,
        life: 3000
      });
      return false;
    }
  }
  
  console.log('폼 유효성 검증 통과');
  return true;
};


// 공통코드 형변환 함수들
const convertItemTypeCodes = (list) => {
  const itemTypeCodes = commonStore.getCodes('0H'); // 품목유형코드
  
  return list.map(item => {
    const matchedType = itemTypeCodes.find(code => code.dcd === item.itemType);
    
    return {
      ...item,
      typeText: matchedType ? matchedType.cdInfo : getDefaultItemTypeText(item.itemType)
    };
  });
};

const convertUnitCodes = (list) => {
  const unitCodes = commonStore.getCodes('0G'); // 단위코드
  
  return list.map(item => {
    const matchedUnit = unitCodes.find(code => code.dcd === item.unit);
    
    return {
      ...item,
      unitText: matchedUnit ? matchedUnit.cdInfo : item.unit
    };
  });
};

// 기본 품목유형 텍스트 (공통코드 로드 전 대체용)
const getDefaultItemTypeText = (itemType) => {
  switch(itemType) {
    case 'h1': return '원자재';
    case 'h2': return '부자재';
    case 'h3': return '완제품';
    default: return itemType || '기타';
  }
};

// 단일 아이템 단위 변환
const convertSingleUnitText = (unit) => {
  const unitCodes = commonStore.getCodes('0G') || [];
  const unitCode = unitCodes.find(code => code.dcd === unit);
  return unitCode ? unitCode.cdInfo : unit;
};

// selectedItems 변경 감지
watch(selectedItems, (newSelection) => {
  // Store 상태 동기화 등 필요 시 처리
}, { deep: true });
</script>

<template>
<div class="space-y-4">

  <!-- 기본정보 입력 폼 -->
  <div class="mb-6">
    <InputForm
      :key="`header-form-${formKey}`"
      :columns="headerFormFields"
      :data="headerFormData"
      title="기본정보"
      :buttons="{ 
        save: { show: true, label: '등록', icon: 'pi pi-save' },
        reset: { show: false },
        delete: { show: false },
        load: { show: false }
      }"
      @update:data="handleHeaderDataChange"
      @submit="handleSave"
    />
  </div>

  <!-- 자재 목록 테이블 -->
  <div>
    <InputTable
      :data="itemTableData"
      :columns="itemTableColumns"
      title="자재 목록"
      v-model:selection="selectedItems"
      :dataKey="'id'"
      :selectionMode="'multiple'"
      :enableSelection="true"
      :enableRowActions="false"
      :scrollHeight="'400px'"
      :showRowCount="true"
      :buttons="{ 
        save: { show: false },
        reset: { show: false },
        delete: { show: true, label: '제거', severity: 'danger' },
        load: { show: true, label: '추가', severity: 'info' }
      }"
      @dataChange="handleItemDataChange"
      @locationSelect="handleLocationSelect"
      @delete="handleRemoveSelectedItems"
      @load="handleAddItem"
    />
  </div>

  <!-- 자재 선택 모달 -->
  <MultipleSelectModal
    v-model:visible="itemSelectModalVisible"
    v-model:modelValue="selectedItemsFromModal"
    :items="availableItems"
    :itemKey="'id'"
    :columns="itemModalColumns"
    @update:modelValue="handleItemSelect"
    :style="{ '--modal-width': '90vw', '--modal-height': '80vh' }"
  />

  <!-- 도착위치 선택 모달 -->
  <LocationSelectModal
    v-model:visible="locationSelectModalVisible"
    :selectedMaterial="currentRowForLocation"
    :loadingQuantity="currentRowForLocation?.moveQty || 0"
    @confirm="handleLocationSelectConfirm"
  />
</div>
</template>

<style scoped>
.space-y-4 > :not([hidden]) ~ :not([hidden]) {
 margin-top: 1rem;
}

.mb-6 {
 margin-bottom: 1.5rem;
}
</style>

<style>
/* 강제로 모든 다이얼로그 크기 변경 */
.p-dialog-mask .p-dialog {
  width: 90vw !important;
  max-width: 1200px !important;

}

.p-dialog .p-dialog-content {
  height: calc(85vh - 120px) !important;
  overflow: auto !important;
}

.p-datatable-wrapper {
  height: 400px !important;
  overflow: auto !important;
}
</style>