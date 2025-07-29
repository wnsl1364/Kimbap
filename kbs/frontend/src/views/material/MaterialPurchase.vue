<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue';
import { useToast } from 'primevue/usetoast';
import { useMaterialStore } from '@/stores/materialStore';
import { useMemberStore } from '@/stores/memberStore';
import InputForm from '@/components/kimbap/searchform/inputForm.vue';
import InputTable from '@/components/kimbap/table/InputTable.vue';
import SingleSelectModal from '@/components/kimbap/modal/singleselect.vue';
import { storeToRefs } from 'pinia';
import Toast from 'primevue/toast';
import { format, parseISO, isValid, formatISO } from 'date-fns';

// 🔥 실제 API 함수들 import
import {
  getPurcOrderList,
  getPurcOrderWithDetails,
  savePurchaseOrder,
  generatePurchaseCode,
  getMaterialsWithSuppliers
} from '@/api/materials';

// 🎯 Store 활용하기!
const materialStore = useMaterialStore();
const memberStore = useMemberStore();
const toast = useToast();

// Store에서 데이터 가져오기 (진짜 필요한 것만!)
const { purchaseData, modalDataSets } = storeToRefs(materialStore);

// 🔥 주문 기본정보 (3개 항목만!)
const orderBasicInfo = ref({
  purcCd: '',  // 발주번호
  regi: '',    // 등록자
  ordDt: ''    // 주문일자
});

// 📋 기본정보 컬럼 (3개만!)
const basicInfoColumns = ref([
  {
    key: 'purcCd',
    label: '발주번호',
    type: 'text',
    placeholder: '(자동생성)',
    required: true
  },
  {
    key: 'regi',
    label: '등록자',
    type: 'readonly',
    defaultValue: memberStore.user?.empName || '김김밥'
  },
  {
    key: 'ordDt',
    label: '주문일자',
    type: 'calendar',
    defaultValue: new Date().toISOString().split('T')[0],
    required: true
  }
]);

const formatDateForBackend = (dateInput) => {
  if (!dateInput) return null;

  try {
    let date;

    if (dateInput instanceof Date) {
      date = dateInput;
    } else if (typeof dateInput === 'string') {
      date = new Date(dateInput);
    } else {
      return null;
    }

    if (!isValid(date)) return null;

    // ✨ Date 객체 자체를 반환 (String 아님!)
    return date;

  } catch (error) {
    console.error('Date formatting error:', error);
    return null;
  }
};

const formatDateForInput = (dateInput) => {
  if (!dateInput) return '';

  try {
    let date;

    if (dateInput instanceof Date) {
      date = dateInput;
    } else if (typeof dateInput === 'string') {
      date = dateInput.includes('T') ? parseISO(dateInput) : new Date(dateInput);
    } else {
      return '';
    }

    if (!isValid(date)) return '';

    // input[type="date"]에서 사용할 YYYY-MM-DD 형식
    return format(date, 'yyyy-MM-dd');

  } catch (error) {
    console.error('Date input formatting error:', error, dateInput);
    return '';
  }
};

const formatDateForDisplay = (dateInput) => {
  if (!dateInput) return '';

  try {
    let date;

    if (dateInput instanceof Date) {
      date = dateInput;
    } else if (typeof dateInput === 'string') {
      date = dateInput.includes('T') ? parseISO(dateInput) : new Date(dateInput);
    } else {
      return '';
    }

    if (!isValid(date)) return '';

    // 사용자에게 보여줄 형식 (예: 2025년 7월 28일)
    return format(date, 'yyyy년 M월 d일');

  } catch (error) {
    console.error('Date display formatting error:', error, dateInput);
    return '';
  }
};

// 📦 자재 테이블 컬럼 (자재명/거래처명 검색 가능)
const materialColumns = ref([
  {
    field: 'materialName',
    header: '자재명',
    type: 'inputsearch',
    width: '150px',
    suffixIcon: 'pi pi-search',
    placeholder: '자재명 검색',
    readonly: false
  },
  {
    field: 'buyer',
    header: '거래처명',
    type: 'inputsearch',
    width: '150px',
    suffixIcon: 'pi pi-search',
    placeholder: '거래처명 검색',
    readonly: false
  },
  {
    field: 'mcode',
    header: '자재코드',
    type: 'readonly',
    width: '120px'
  },
  {
    field: 'mateVerCd',
    header: '버전',
    type: 'readonly',
    width: '80px'
  },
  {
    field: 'cpCd',
    header: '거래처코드',
    type: 'readonly',
    width: '120px'
  },
  {
    field: 'number',
    header: '수량',
    type: 'input',
    inputType: 'number',
    width: '100px',
    placeholder: '수량 입력'
  },
  {
    field: 'unit',
    header: '단위',
    type: 'readonly',
    width: '80px'
  },
  {
    field: 'price',
    header: '단가(원)',
    type: 'input',
    inputType: 'number',
    width: '120px',
    placeholder: '단가 입력'
  },
  {
    field: 'totalPrice',
    header: '총액(원)',
    type: 'readonly',
    width: '120px'
  },
  {
    field: 'date',
    header: '납기예정일',
    type: 'calendar',
    width: '140px'
  },
  {
    field: 'memo',
    header: '비고',
    type: 'input',
    width: '200px',
    placeholder: '비고사항'
  }
]);

// 🔍 실제 자재-거래처 통합 검색 데이터 (API 연결!)
const searchModalData = ref({
  materials: [],
  suppliers: []
});

const currentModalRowData = ref(null);

// 🚀 자재-거래처 데이터 로드 (실제 API 호출!)
const loadMaterialsWithSuppliers = async () => {
  try {
    console.log('🔍 자재-거래처 데이터 로드 시작...');

    const response = await getMaterialsWithSuppliers();
    console.log('API 응답:', response.data);

    // 자재별로 그룹화
    const materialsMap = new Map();
    const suppliersMap = new Map();

    response.data.forEach(item => {
      // 자재 데이터 그룹화
      const materialKey = `${item.mcode}_${item.mateVerCd}`;
      if (!materialsMap.has(materialKey)) {
        materialsMap.set(materialKey, {
          mcode: item.mcode,
          mateName: item.mateName,
          mateVerCd: item.mateVerCd,
          mateType: item.mateType,
          unit: item.unit,
          std: item.std,
          safeStock: item.safeStock,
          suppliers: []
        });
      }

      // 해당 자재에 공급업체 추가
      materialsMap.get(materialKey).suppliers.push({
        cpCd: item.cpCd,
        cpName: item.cpName,
        unitPrice: item.unitPrice
      });

      // 거래처 데이터 그룹화
      if (!suppliersMap.has(item.cpCd)) {
        suppliersMap.set(item.cpCd, {
          cpCd: item.cpCd,
          cpName: item.cpName,
          cpType: item.cpType,
          repname: item.repname,
          tel: item.cpTel,
          address: item.cpAddress,
          materials: []
        });
      }

      // 해당 거래처에 자재 추가
      suppliersMap.get(item.cpCd).materials.push({
        mcode: item.mcode,
        mateName: item.mateName,
        unitPrice: item.unitPrice
      });
    });

    // Map을 배열로 변환
    searchModalData.value.materials = Array.from(materialsMap.values());
    searchModalData.value.suppliers = Array.from(suppliersMap.values());

    console.log('✅ 자재-거래처 데이터 로드 완료:', {
      materials: searchModalData.value.materials.length,
      suppliers: searchModalData.value.suppliers.length
    });

  } catch (error) {
    console.error('❌ 자재-거래처 데이터 로드 실패:', error);

    // API 실패 시 기본 데이터 사용
    searchModalData.value.materials = [
      {
        mcode: 'MAT-1001',
        mateName: '김(건조)',
        mateType: '원자재',
        unit: 'kg',
        std: '1kg/포',
        safeStock: 100,
        suppliers: [
          { cpCd: 'CP-001', cpName: '황금쌀농협', unitPrice: 15000 },
          { cpCd: 'CP-002', cpName: '바다김 수산', unitPrice: 14500 }
        ]
      }
    ];

    searchModalData.value.suppliers = [
      {
        cpCd: 'CP-001',
        cpName: '황금쌀농협',
        repname: '김농부',
        tel: '02-1234-5678',
        address: '경기도 이천시 쌀밭로 123',
        materials: [
          { mcode: 'MAT-1001', mateName: '김(건조)', unitPrice: 15000 }
        ]
      },
      {
        cpCd: 'CP-002',
        cpName: '바다김 수산',
        repname: '박해산',
        tel: '061-9999-8888',
        address: '전남 완도군 바다로 456',
        materials: [
          { mcode: 'MAT-1001', mateName: '김(건조)', unitPrice: 14500 },
          { mcode: 'MAT-1002', mateName: '쌀(백미)', unitPrice: 2800 }
        ]
      },
      {
        cpCd: 'CP-003',
        cpName: '프레시 야채',
        repname: '이야채',
        tel: '031-5555-6666',
        address: '경기도 수원시 야채로 789',
        materials: [
          { mcode: 'MAT-1002', mateName: '쌀(백미)', unitPrice: 2900 },
          { mcode: 'MAT-1003', mateName: '참치(캔)', unitPrice: 3000 }
        ]
      },
      {
        cpCd: 'CP-004',
        cpName: '맛있는 소스',
        repname: '최마요',
        tel: '031-7777-8888',
        address: '경기도 안산시 소스동 101',
        materials: [
          { mcode: 'MAT-1003', mateName: '참치(캔)', unitPrice: 3100 },
          { mcode: 'MAT-1004', mateName: '마요네즈', unitPrice: 4500 }
        ]
      },
      {
        cpCd: 'CP-005',
        cpName: '포장재 전문',
        repname: '정포장',
        tel: '02-3333-4444',
        address: '서울특별시 구로구 포장로 202',
        materials: [
          { mcode: 'MAT-1004', mateName: '마요네즈', unitPrice: 4200 },
          { mcode: 'MAT-2001', mateName: '포장지(김밥용)', unitPrice: 25000 }
        ]
      }
    ];

    toast.add({
      severity: 'warn',
      summary: '데이터 로드 실패',
      detail: '기본 데이터를 사용합니다.',
      life: 3000
    });
  }
};

// 🔍 모달 상태 관리
const searchModalVisible = ref(false);
const currentSearchType = ref(''); // 'material' 또는 'supplier'
const currentRowIndex = ref(-1);
const searchModalItems = ref([]);
const searchModalColumns = ref([]);

// 🎯 모달 데이터 설정 (동적 필터링 적용!)
const tableModalDataSets = computed(() => ({
  materialName: {
    // 🔥 거래처가 선택되어 있으면 해당 거래처의 자재만!
    items: currentModalRowData.value?.cpCd 
      ? getMaterialsBySupplier(currentModalRowData.value.cpCd)
      : searchModalData.value.materials,
    columns: [
      { field: 'mcode', header: '자재코드' },
      { field: 'mateVerCd', header: '버전' },
      { field: 'mateName', header: '자재명' },
      { field: 'mateType', header: '유형' },
      { field: 'unit', header: '단위' },
      { field: 'std', header: '규격' },
      { field: 'safeStock', header: '안전재고' }
    ],
    mappingFields: {
      materialName: 'mateName',
      mcode: 'mcode',
      mateVerCd: 'mateVerCd',
      unit: 'unit'
    }
  },
  buyer: {
    // 🔥 자재가 선택되어 있으면 해당 자재를 파는 거래처만!
    items: currentModalRowData.value?.mcode && currentModalRowData.value?.mateVerCd
      ? getSuppliersByMaterial(currentModalRowData.value.mcode, currentModalRowData.value.mateVerCd)
      : searchModalData.value.suppliers,
    columns: [
      { field: 'cpCd', header: '거래처코드' },
      { field: 'cpName', header: '거래처명' },
      { field: 'repname', header: '대표자' },
      { field: 'tel', header: '전화번호' },
      { field: 'address', header: '주소' }
    ],
    mappingFields: {
      buyer: 'cpName',
      cpCd: 'cpCd'
    }
  }
}));

const handleModalOpen = (rowData, fieldName) => {
  console.log('🔍 모달 열림:', fieldName, rowData);
  currentModalRowData.value = { ...rowData };  // 🔥 이게 핵심!
  
  // 디버깅
  if (fieldName === 'materialName' && rowData.cpCd) {
    console.log('자재 모달 - 거래처 필터링 적용:', rowData.cpCd);
  }
  if (fieldName === 'buyer' && rowData.mcode) {
    console.log('거래처 모달 - 자재 필터링 적용:', rowData.mcode);
  }
};

// 🔥 기존 발주 정보 불러오기 (실제 API 연결!)
const loadExistingOrder = async (purcCd) => {
  try {
    if (!purcCd) {
      toast.add({
        severity: 'warn',
        summary: '경고',
        detail: '발주번호를 입력해주세요!',
        life: 3000
      });
      return;
    }

    console.log('🔍 발주정보 조회 시작:', purcCd);

    const response = await getPurcOrderWithDetails(purcCd);
    console.log('API 응답:', response.data);

    if (response.data && response.data.header) {
      const { header, details } = response.data;

      // 🔥 date-fns로 날짜 처리
      orderBasicInfo.value = {
        purcCd: header.purcCd,
        regi: header.regi,
        ordDt: formatDateForInput(header.ordDt) // date-fns 사용
      };

      // 상세정보 설정
      purchaseData.value = details.map((item, index) => ({
        id: index + 1,
        materialName: item.mateName,
        buyer: item.cpName,
        mcode: item.mcode,
        cpCd: item.cpCd,
        number: item.purcQty,
        unit: getUnitText(item.unit),
        price: item.unitPrice,
        totalPrice: item.totalAmount || (item.purcQty * item.unitPrice),
        date: formatDateForInput(item.exDeliDt), // 🔥 date-fns 사용
        memo: item.note || ''
      }));

      toast.add({
        severity: 'success',
        summary: '불러오기 완료!',
        detail: `발주서 ${purcCd}를 성공적으로 불러왔습니다. (${details.length}개 자재)`,
        life: 3000
      });

    } else {
      throw new Error('발주서 데이터가 없습니다.');
    }

  } catch (error) {
    console.error('❌ 발주정보 불러오기 실패:', error);

    if (error.response?.status === 404) {
      toast.add({
        severity: 'warn',
        summary: '발주서 없음',
        detail: `발주번호 ${purcCd}에 해당하는 발주서를 찾을 수 없습니다.`,
        life: 3000
      });
    } else {
      toast.add({
        severity: 'error',
        summary: '불러오기 실패',
        detail: '발주정보를 불러오는 중 오류가 발생했습니다.',
        life: 3000
      });
    }
  }
};

// 💾 발주서 저장 (실제 API 연결!)
const handleSavePurchaseOrder = async (formData) => {
  try {
    console.log('💾 발주서 저장 시작:', formData);

    // 🔥 완벽한 검증!
    const validItems = purchaseData.value.filter(item =>
      item.materialName &&
      item.buyer &&
      item.number > 0 &&
      item.mcode &&         // 🔥 자재코드 필수!
      item.mateVerCd &&     // 🔥 자재버전코드 필수!
      item.cpCd             // 🔥 거래처코드 필수!
    );

    console.log('🔍 검증 결과:');
    purchaseData.value.forEach((item, index) => {
      console.log(`  ${index + 1}행:`, {
        materialName: item.materialName,
        mcode: item.mcode,
        mateVerCd: item.mateVerCd,  // 🔥 버전코드 확인!
        buyer: item.buyer,
        cpCd: item.cpCd,
        number: item.number,
        isValid: item.materialName && item.buyer && item.number > 0 && item.mcode && item.mateVerCd && item.cpCd
      });
    });

    if (validItems.length === 0) {
      toast.add({
        severity: 'warn',
        summary: '경고',
        detail: '자재명과 거래처명을 🔍 버튼으로 검색해서 선택해주세요! (자재코드, 버전코드, 거래처코드가 모두 필요합니다)',
        life: 4000
      });
      return;
    }

    const totalAmount = validItems.reduce((sum, item) => sum + (item.totalPrice || 0), 0);

    const saveData = {
      header: {
        purcCd: '',  // 백엔드에서 자동생성
        ordDt: formatDateForBackend(formData.ordDt),
        regi: formData.regi,
        purcStatus: 'c1',
        ordTotalAmount: totalAmount
      },
      details: validItems.map(item => ({
        cpCd: item.cpCd,
        mcode: item.mcode,
        mateVerCd: item.mateVerCd,  // 🔥 버전코드 추가!
        purcQty: item.number,
        unit: convertUnitToCode(item.unit),
        unitPrice: item.price,
        exDeliDt: formatDateForBackend(item.date),
        note: item.memo || '',
        purcDStatus: 'c1'
      }))
    };

    console.log('📤 완벽한 저장 데이터:', saveData);

    const response = await savePurchaseOrder(saveData);

    if (response.data.success) {
      orderBasicInfo.value.purcCd = response.data.purcCd;

      toast.add({
        severity: 'success',
        summary: '저장 완료!',
        detail: `발주서 ${response.data.purcCd}가 생성되었습니다! (${validItems.length}개 자재, ${totalAmount.toLocaleString()}원)`,
        life: 4000
      });
    }

  } catch (error) {
    console.error('❌ 저장 실패:', error);
    toast.add({
      severity: 'error',
      summary: '저장 실패',
      detail: error.response?.data?.message || '발주서 저장 중 오류가 발생했습니다.',
      life: 3000
    });
  }
};

const getSuppliersByMaterial = (selectedMcode, selectedMateVerCd) => {
  const material = searchModalData.value.materials.find(m =>
    m.mcode === selectedMcode && m.mateVerCd === selectedMateVerCd
  );

  if (material && material.suppliers) {
    return material.suppliers.map(supplier =>
      searchModalData.value.suppliers.find(s => s.cpCd === supplier.cpCd)
    ).filter(Boolean);
  }

  return searchModalData.value.suppliers; // 전체 목록
};

// 🔍 거래처별 자재 필터링 함수 (추가!)
const getMaterialsBySupplier = (selectedCpCd) => {
  const supplier = searchModalData.value.suppliers.find(s => s.cpCd === selectedCpCd);

  if (supplier && supplier.materials) {
    return supplier.materials.map(material =>
      searchModalData.value.materials.find(m => m.mcode === material.mcode)
    ).filter(Boolean);
  }

  return searchModalData.value.materials; // 전체 목록
};

// 🗑️ 초기화
const handleReset = () => {
  orderBasicInfo.value = {
    purcCd: '',
    regi: memberStore.user?.empName || '김김밥',
    ordDt: new Date().toISOString().split('T')[0]
  };

  purchaseData.value = [];

  toast.add({
    severity: 'info',
    summary: '초기화 완료',
    detail: '발주서가 초기화되었습니다.',
    life: 2000
  });
};

// 📥 불러오기 (발주번호로 기존 발주 조회 또는 목록에서 선택)
const handleLoad = async () => {
  const purcCd = orderBasicInfo.value.purcCd;
  if (purcCd) {
    // 발주번호가 입력되어 있으면 직접 조회
    await loadExistingOrder(purcCd);
  } else {
    // 발주번호가 없으면 목록 모달 열기
    await loadOrderList();
    orderListModalVisible.value = true;
  }
};

// 💰 발주 요약 정보
const orderSummary = computed(() => {
  const validItems = purchaseData.value.filter(item => item.materialName);
  const totalAmount = validItems.reduce((sum, item) => sum + (item.totalPrice || 0), 0);
  const uniqueSuppliers = new Set(validItems.map(item => item.buyer).filter(Boolean));

  return {
    itemCount: validItems.length,
    supplierCount: uniqueSuppliers.size,
    totalAmount: totalAmount
  };
});

// 📊 테이블 데이터 변경 (총액 자동계산)
const handleDataChange = (newData) => {
  // 각 행의 총액 자동계산
  newData.forEach(item => {
    const qty = parseFloat(item.number) || 0;
    const price = parseFloat(item.price) || 0;
    item.totalPrice = qty * price;
  });

  purchaseData.value = newData;
  console.log('📊 테이블 데이터 변경됨:', newData.length, '개 항목');
};

// 🎨 버튼 설정
const formButtons = ref({
  save: { show: true, label: '발주서 저장', severity: 'success' },
  reset: { show: true, label: '전체 초기화', severity: 'secondary' },
  load: { show: true, label: '기존 발주 불러오기', severity: 'info' },
  delete: { show: false }
});

const tableButtons = ref({
  save: { show: false },
  reset: { show: false },
  delete: { show: false },
  load: { show: false }
});

// 🎯 컴포넌트 초기화 (실제 API 연결!)
onMounted(async () => {
  // 기본정보 초기값 설정
  orderBasicInfo.value = {
    purcCd: '',
    regi: memberStore.user?.empName || '김김밥',
    ordDt: new Date().toISOString().split('T')[0]
  };

  // 빈 테이블로 시작
  if (purchaseData.value.length === 0) {
    purchaseData.value = [];
  }

  // 🚀 자재-거래처 데이터 로드
  await loadMaterialsWithSuppliers();

  console.log('🚀 MaterialPurchase 컴포넌트 초기화 완료');
});

// 🔧 단위 코드 변환 함수들
const getUnitText = (unitCode) => {
  const unitMap = {
    'g1': 'g',
    'g2': 'kg',
    'g3': 'ml',
    'g4': 'L',
    'g5': 'ea',
    'g6': 'box',
    'g7': 'mm'
  };
  return unitMap[unitCode] || unitCode;
};

const convertUnitToCode = (unitText) => {
  const codeMap = {
    'g': 'g1',
    'kg': 'g2',
    'ml': 'g3',
    'L': 'g4',
    'ea': 'g5',
    'box': 'g6',
    'mm': 'g7'
  };
  return codeMap[unitText] || 'g5';
};

// 📋 발주서 목록 모달 상태 관리
const orderListModalVisible = ref(false);
const orderList = ref([]);

// 🔍 발주서 목록 모달 컬럼 설정
const orderListColumns = [
  { field: 'purcCd', header: '발주번호' },
  { field: 'ordDt', header: '주문일자' },
  { field: 'regi', header: '등록자' },
  { field: 'purcStatus', header: '발주상태' },
  { field: 'ordTotalAmount', header: '총금액(원)' }
];

// 🚀 발주서 목록 로드 (실제 API 호출!)
const loadOrderList = async () => {
  try {
    console.log('📋 발주서 목록 로드 시작...');

    const response = await getPurcOrderList();
    console.log('발주서 목록 API 응답:', response.data);

    // 🔥 date-fns로 날짜 포맷팅
    orderList.value = response.data.map(order => ({
      ...order,
      ordDt: formatDateForInput(order.ordDt), // date-fns 사용
      purcStatus: getPurcStatusText(order.purcStatus),
      ordTotalAmount: order.ordTotalAmount ? order.ordTotalAmount.toLocaleString() : '0'
    }));

    console.log('✅ 발주서 목록 로드 완료:', orderList.value.length, '건');

  } catch (error) {
    console.error('❌ 발주서 목록 로드 실패:', error);

    // API 실패 시 기본 데이터
    orderList.value = [
      {
        purcCd: 'PUOR-202507-0001',
        ordDt: format(new Date('2025-07-25'), 'yyyy-MM-dd'), // 🔥 date-fns 사용
        regi: '김김밥',
        purcStatus: '승인',
        ordTotalAmount: '500,000'
      },
      {
        purcCd: 'PUOR-202507-0002',
        ordDt: format(new Date('2025-07-26'), 'yyyy-MM-dd'), // 🔥 date-fns 사용
        regi: '이발주',
        purcStatus: '요청',
        ordTotalAmount: '350,000'
      }
    ];

    toast.add({
      severity: 'warn',
      summary: '목록 로드 실패',
      detail: '기본 데이터를 사용합니다.',
      life: 3000
    });
  }
};

// 📋 발주서 목록 모달에서 선택
const handleOrderSelect = async (selectedOrder) => {
  try {
    console.log('📋 발주서 선택됨:', selectedOrder);

    orderListModalVisible.value = false;

    // 선택된 발주서 불러오기
    await loadExistingOrder(selectedOrder.purcCd);

  } catch (error) {
    console.error('❌ 발주서 선택 처리 실패:', error);
  }
};

// 🔧 발주 상태 코드를 텍스트로 변환
const getPurcStatusText = (status) => {
  const statusMap = {
    'c1': '요청',
    'c2': '승인',
    'c3': '입고대기',
    'c4': '부분입고',
    'c5': '입고완료',
    'c6': '거절',
    'c7': '반품'
  };
  return statusMap[status] || status;
};

onUnmounted(() => {
  console.log('🧹 MaterialPurchase 컴포넌트 언마운트됨');
});
</script>

<template>
  <div class="p-4">
    <Toast />

    <!-- 👑 페이지 헤더 -->
    <div class="mb-6">
      <h1 class="text-3xl font-bold text-gray-800 mb-2">자재 발주서 작성</h1>
      <div class="flex items-center gap-4 text-sm text-gray-600">
        <span>👤 {{ memberStore.user?.empName || '김김밥' }}</span>
        <span>🏢 {{ memberStore.user?.deptName || '구매팀' }}</span>
        <span class="text-green-600">✅ 발주 권한 있음</span>
      </div>
    </div>

    <!-- 📋 주문 기본정보 (발주번호, 등록자, 주문일자만!) -->
    <div class="mb-6">
      <InputForm :columns="basicInfoColumns" :data="orderBasicInfo" title="📋 주문 기본정보" :buttons="formButtons"
        button-position="top" @update:data="(newData) => { orderBasicInfo = newData }" @submit="handleSavePurchaseOrder"
        @reset="handleReset" @load="handleLoad" />
    </div>

    <!-- 📦 자재 발주 상세 -->
    <div>
      <InputTable title="📦 자재 발주 상세" :scroll-height="'50vh'" :height="'60vh'" :columns="materialColumns"
        :data="purchaseData" :buttons="tableButtons" :enableRowActions="true" :enableSelection="true"
        :modalDataSets="tableModalDataSets" :autoCalculation="{
          enabled: true,
          quantityField: 'number',
          priceField: 'price',
          totalField: 'totalPrice'
        }" :showRowCount="true" dataKey="id" @dataChange="handleDataChange" @modalOpen="handleModalOpen" />
    </div>

    <!-- 📊 발주 요약 (데이터가 있을 때만 표시) -->
    <div v-if="orderSummary.itemCount > 0" class="mt-6 p-4 bg-blue-50 rounded-lg border border-blue-200">
      <h3 class="text-lg font-semibold text-blue-800 mb-2">📊 발주 요약</h3>
      <div class="grid grid-cols-2 md:grid-cols-3 gap-4 text-sm">
        <div>
          <span class="text-gray-600">총 자재 수:</span>
          <span class="font-bold ml-2">{{ orderSummary.itemCount }}개</span>
        </div>
        <div>
          <span class="text-gray-600">거래처 수:</span>
          <span class="font-bold ml-2">{{ orderSummary.supplierCount }}개</span>
        </div>
        <div>
          <span class="text-gray-600">총 발주 금액:</span>
          <span class="font-bold ml-2 text-blue-600">{{ orderSummary.totalAmount.toLocaleString() }}원</span>
        </div>
      </div>
    </div>

    <!-- 💡 사용법 안내 -->
    <div v-if="orderSummary.itemCount === 0" class="mt-6 p-4 bg-green-50 rounded-lg border border-green-200">
      <div class="flex items-start gap-2">
        <i class="pi pi-info-circle text-green-500 mt-1"></i>
        <div class="text-green-700">
          <p class="font-semibold mb-1">💡 사용법</p>
          <ul class="text-sm space-y-1">
            <li>• <strong>새 발주서:</strong> 아래 "행 추가" 버튼으로 자재를 추가하세요</li>
            <li>• <strong>기존 발주:</strong> 발주번호 입력 후 "기존 발주 불러오기" 클릭하거나, 발주번호 없이 클릭하면 목록에서 선택</li>
            <li>• <strong>자재 검색:</strong> 자재명이나 거래처명 옆 🔍 버튼으로 검색 가능</li>
          </ul>
        </div>
      </div>
    </div>

    <!-- 📋 발주서 목록 선택 모달 -->
    <SingleSelectModal v-model:visible="orderListModalVisible" :items="orderList" :columns="orderListColumns"
      :itemKey="'purcCd'" @update:modelValue="handleOrderSelect" header="발주서 목록" />
  </div>
</template>

<style scoped>
:deep(.p-toast) {
  z-index: 9999;
}
</style>