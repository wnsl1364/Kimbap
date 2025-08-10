<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue';
import { useRoute } from 'vue-router';
import { useToast } from 'primevue/usetoast';
import { useMaterialStore } from '@/stores/materialStore';
import { useMemberStore } from '@/stores/memberStore';
import { useCommonStore } from '@/stores/commonStore';
import InputForm from '@/components/kimbap/searchform/inputForm.vue';
import InputTable from '@/components/kimbap/table/InputTable.vue';
import SingleSelectModal from '@/components/kimbap/modal/singleselect.vue';
import { storeToRefs } from 'pinia';
import Toast from 'primevue/toast';
import { format, parseISO, isValid, formatISO } from 'date-fns';

// 실제 API 함수들 import
import {
  getPurcOrderList,
  getPurcOrderWithDetails,
  savePurchaseOrder,
  generatePurchaseCode,
  getMaterialsWithSuppliers,
  getMaterials,
  getSuppliers,
  getMaterialsBySupplier,
  getSuppliersByMaterial
} from '@/api/materials';

// Store 활용하기!
const materialStore = useMaterialStore();
const memberStore = useMemberStore();
const common = useCommonStore();
const { commonCodes } = storeToRefs(common)
const toast = useToast();
const route = useRoute();
const convertedMaterialList = computed(() => {
  if (!purchaseData.value || !Array.isArray(purchaseData.value)) {
    console.warn('convertedMaterialList: purchaseData가 배열이 아님:', typeof purchaseData.value);
    return [];
  }
  return convertUnitCodes(purchaseData.value);
});
// Store에서 데이터 가져오기 (진짜 필요한 것만!)
const { purchaseData } = storeToRefs(materialStore);

const convertUnitCodes = (list) => {
  // 🛡️ 방어 코드 추가!
  if (!list || !Array.isArray(list)) {
    console.warn('convertUnitCodes: list가 배열이 아님:', typeof list, list);
    return [];
  }

  const mateTypeCodes = common.getCodes('0H'); // 자재유형
  const stoConCodes = common.getCodes('0G');   // 단위코드

  return list.map(item => {
    const matchedMateType = mateTypeCodes.find(code => code.dcd === item.mateType);
    const matchedStoCon = stoConCodes.find(code => code.dcd === item.unit); // 🔥 unit 필드 매핑

    return {
      ...item,
      mateType: matchedMateType ? matchedMateType.cdInfo : item.mateType,
      unit: matchedStoCon ? matchedStoCon.cdInfo : item.unit, // 🔥 unit으로 변경
    };
  });
};

// 주문 기본정보 (3개 항목만!)
const orderBasicInfo = ref({
  purcCd: '',  // 발주번호
  regi: memberStore.user?.empCd || 'EMP-10001',    // 등록자
  ordDt: ''    // 주문일자
});

// 기본정보 컬럼 (3개만!)
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

    // Date 객체 자체를 반환 (String 아님!)
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

// 자재 테이블 컬럼 (공급업체로 변경!)
const materialColumns = ref([
  {
    field: 'materialName',
    header: '자재명',
    type: 'inputsearch',
    width: '180px',
    suffixIcon: 'pi pi-search',
    placeholder: '자재명 검색',
    readonly: false
  },
  {
    field: 'buyer',
    header: '공급업체',
    type: 'inputsearch',
    width: '180px',
    suffixIcon: 'pi pi-search',
    placeholder: '공급업체 검색',
    readonly: false
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
    width: '100px',
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

// Store에서 모달 데이터 가져오기
const tableModalDataSets = computed(() => materialStore.purchaseModalData);
const convertedModalDataSets = computed(() => {
  const modalData = tableModalDataSets.value;

  if (!modalData || typeof modalData !== 'object') {
    console.warn('convertedModalDataSets: modalData가 객체가 아님:', typeof modalData);
    return {
      materialName: { items: [], columns: [] },
      buyer: { items: [], columns: [] }
    };
  }

  return {
    materialName: {
      ...modalData.materialName,
      items: convertUnitCodes(modalData.materialName?.items || [])
    },
    buyer: {
      ...modalData.buyer,
      items: convertUnitCodes(modalData.buyer?.items || [])
    }
  };
});

// 자재-공급업체 데이터 로드 (실제 API 호출!)
const loadMaterialSupplierCombinations = async () => {
  try {
    console.log('🔍 자재-공급업체 조합 데이터 로드 시작...');

    const response = await getMaterialsWithSuppliers();
    console.log('API 응답:', response.data);

    // Store에 데이터 저장하면 computed에서 자동으로 모달 데이터 업데이트됨
    materialStore.setMaterialSupplierCombinations(response.data);

    console.log('✅ 자재-공급업체 조합 데이터 로드 완료:', response.data.length, '건');

  } catch (error) {
    console.error('❌ 자재-공급업체 데이터 로드 실패:', error);

    // API 실패 시 기본 데이터 사용
    const sampleData = [
      {
        mcode: 'MAT-1001',
        mateName: '김(건조)',
        mateVerCd: 'V1',
        mateType: 'h1',
        unit: 'kg',
        std: '1kg/포',
        safeStock: 100,
        cpCd: 'CP-001',
        cpName: '황금쌀농협',
        unitPrice: 15000,
        ltime: 3
      },
      {
        mcode: 'MAT-1001',
        mateName: '김(건조)',
        mateVerCd: 'V1',
        mateType: 'h1',
        unit: 'kg',
        std: '1kg/포',
        safeStock: 100,
        cpCd: 'CP-002',
        cpName: '바다김수산',
        unitPrice: 14500,
        ltime: 4
      },
      {
        mcode: 'MAT-1002',
        mateName: '쌀(백미)',
        mateVerCd: 'V1',
        mateType: 'h1',
        unit: 'kg',
        std: '20kg/포',
        safeStock: 50,
        cpCd: 'CP-001',
        cpName: '황금쌀농협',
        unitPrice: 2800,
        ltime: 5
      },
      {
        mcode: 'MAT-1003',
        mateName: '참치(캔)',
        mateVerCd: 'V1',
        mateType: 'h1',
        unit: 'ea',
        std: '150g/캔',
        safeStock: 200,
        cpCd: 'CP-003',
        cpName: '프레시야채',
        unitPrice: 3000,
        ltime: 2
      }
    ];

    materialStore.setMaterialSupplierCombinations(sampleData);

    toast.add({
      severity: 'warn',
      summary: '데이터 로드 실패',
      detail: '기본 데이터를 사용합니다.',
      life: 3000
    });
  }
};

// 기존 발주 정보 불러오기 (실제 API 연결!)
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

    console.log('발주정보 조회 시작:', purcCd);

    const response = await getPurcOrderWithDetails(purcCd);
    console.log('API 응답:', response.data);

    if (response.data && response.data.header) {
      const { header, details } = response.data;

      // date-fns로 날짜 처리
      orderBasicInfo.value = {
        purcCd: header.purcCd,
        regi: header.regiName || header.regi,
        ordDt: formatDateForInput(header.ordDt) // date-fns 사용
      };

      // 상세정보 설정
      purchaseData.value = details.map((item, index) => ({
        id: index + 1,
        materialName: item.mateName,
        buyer: item.cpName,
        mcode: item.mcode,
        mateVerCd: item.mateVerCd,
        cpCd: item.cpCd,
        number: item.purcQty,
        unit: getUnitText(item.unit),
        price: item.unitPrice,
        totalPrice: item.totalAmount || (item.purcQty * item.unitPrice),
        date: formatDateForInput(item.exDeliDt), // date-fns 사용
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

// 발주서 저장 (실제 API 연결!)
const handleSavePurchaseOrder = async (formData) => {
  try {
    console.log('💾 발주서 저장 시작:', formData);

    const validItems = purchaseData.value.filter(item =>
      item.materialName &&
      item.buyer &&
      item.number > 0 &&
      item.mcode &&
      item.mateVerCd &&
      item.cpCd
    );

    console.log('검증 결과:');
    purchaseData.value.forEach((item, index) => {
      console.log(`  ${index + 1}행:`, {
        materialName: item.materialName,
        mcode: item.mcode,
        mateVerCd: item.mateVerCd,
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
        detail: '자재명과 공급업체명을 🔍 버튼으로 검색해서 선택해주세요! (자재코드, 버전코드, 거래처코드가 모두 필요합니다)',
        life: 4000
      });
      return;
    }

    const totalAmount = validItems.reduce((sum, item) => sum + (item.totalPrice || 0), 0);

    const saveData = {
      header: {
        purcCd: formData.purcCd || '',  // 백엔드에서 자동생성
        ordDt: formatDateForBackend(formData.ordDt),
        regi: memberStore.user?.empCd || 'EMP-10001',
        purcStatus: 'c1',
        ordTotalAmount: totalAmount
      },
      details: validItems.map(item => ({
        cpCd: item.cpCd,
        mcode: item.mcode,
        mateVerCd: item.mateVerCd,  // 버전코드 추가
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
      // 저장 성공시 데이터 비우기
      purchaseData.value = [];
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

const loadSuppliersByMaterial = async (selectedMcode, selectedMateVerCd) => {
  try {
    console.log('🔍 특정 자재의 공급업체 조회:', selectedMcode, selectedMateVerCd);

    // 1) mateVerCd가 유효하면 버전 기반 API 우선 시도
    if (selectedMateVerCd && selectedMateVerCd !== 'undefined') {
      try {
        const response = await getSuppliersByMaterial(selectedMcode, selectedMateVerCd);
        const list = response.data || [];
        if (list.length > 0) {
          return list.map(item => ({
            cpCd: item.cpCd,
            cpName: item.cpName,
            repname: item.repname,
            tel: item.cpTel,
            unitPrice: item.unitPrice,
            ltime: item.ltime
          }));
        }
        // 결과 0건이면 mcode만으로 재조회
        console.warn('⚠️ 버전기반 조회 0건 → mcode만으로 재조회');
      } catch (e) {
        console.warn('버전기반 조회 실패 → mcode만으로 재조회', e);
      }
    }

    // 2) 버전정보 없거나 결과가 0이면 mcode-only 조합 API로 대체
    const msResp = await getMaterialsWithSuppliers({ mcode: selectedMcode });
    const combos = Array.isArray(msResp.data) ? msResp.data : (msResp.data?.data || []);
    const suppliers = combos
      .filter(it => it.mcode === selectedMcode)
      .map(it => ({
        cpCd: it.cpCd,
        cpName: it.cpName,
        repname: it.repname,
        tel: it.cpTel || it.tel,
        unitPrice: it.unitPrice,
        ltime: it.ltime,
        mateVerCd: it.mateVerCd
      }));

    return suppliers;
  } catch (error) {
    console.error('❌ 특정 자재의 공급업체 조회 실패(최종):', error);
    // 마지막 fallback: 스토어에 적재된 조합에서 mcode만 필터
    return materialStore.materialSupplierCombinations
      .filter(it => it.mcode === selectedMcode)
      .map(it => ({
        cpCd: it.cpCd,
        cpName: it.cpName,
        repname: it.repname,
        tel: it.cpTel || it.tel,
        unitPrice: it.unitPrice,
        ltime: it.ltime,
        mateVerCd: it.mateVerCd
      }));
  }
};

// 🔍 거래처별 자재 필터링 함수 (추가!)
const loadMaterialsBySupplier = async (selectedCpCd) => {
  try {
    console.log('🔍 특정 거래처의 자재 조회:', selectedCpCd);
    const response = await getMaterialsBySupplier(selectedCpCd); // 🔥 API 함수 호출

    return response.data.map(item => ({
      mcode: item.mcode,
      mateName: item.mateName,
      mateVerCd: item.mateVerCd,
      mateType: item.mateType,
      unit: getUnitText(item.unit),
      unitPrice: item.unitPrice,
      ltime: item.ltime
    }));
  } catch (error) {
    console.error('❌ 특정 거래처의 자재 조회 실패:', error);
    return materialStore.materialSupplierCombinations;
  }
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
  console.log('📊 발주 요약 정보:', validItems.length, '개 항목');
  console.log('전체 목록 validItems : ', validItems);
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
  try {
    // 🔥 공통코드 먼저 로드!
    console.log('📋 공통코드 로드 시작...');
    await Promise.all([
      common.fetchCommonCodes('0H'), // 자재유형
      common.fetchCommonCodes('0G')  // 단위코드
    ]);
    console.log('✅ 공통코드 로드 완료');

    // 기본정보 초기값 설정
    orderBasicInfo.value = {
      purcCd: '',
      regi: memberStore.user?.empName || '김김밥',
      ordDt: new Date().toISOString().split('T')[0]
    };

    // 자재-공급업체 조합 데이터 로드
    await loadMaterialSupplierCombinations();

    console.log('🚀 MaterialPurchase 컴포넌트 초기화 완료');

  // ✅ 재고 화면에서 넘어온 쿼리로 초기 행 채우기 (쿼리 또는 스토어)
    const q = route.query || {};
  if (q.mcode) {
      const qty = Number(q.qty) || 0;
      const row = {
        id: Date.now(),
        mcode: q.mcode,
        mateVerCd: q.mateVerCd || '',
        materialName: q.mateName || '',
        buyer: '',
        cpCd: '',
        number: qty,
        unit: q.unit || '',
        price: 0,
        totalPrice: 0,
        date: new Date().toISOString().split('T')[0],
        memo: '재고부족 자동생성'
      };

      purchaseData.value = [row];

      toast.add({
        severity: 'info',
        summary: '부족 자재 추가',
        detail: `${row.materialName || row.mcode} 부족수량 ${qty}를 발주서에 추가했습니다. 공급업체를 선택하세요.`,
        life: 3500
      });

      // 🔎 공급업체 자동 선택/단가 채우기
      try {
        // cpCd가 쿼리에 이미 있으면 우선 적용
        if (q.cpCd) {
          const suppliers = await loadSuppliersByMaterial(q.mcode, q.mateVerCd || undefined);
          const target = suppliers.find(s => s.cpCd === q.cpCd);
          if (target) {
            purchaseData.value[0].buyer = target.cpName;
            purchaseData.value[0].cpCd = target.cpCd;
            purchaseData.value[0].price = Number(target.unitPrice) || 0;
            purchaseData.value[0].totalPrice = (Number(purchaseData.value[0].number) || 0) * (Number(target.unitPrice) || 0);

            toast.add({
              severity: 'success',
              summary: '공급업체 적용',
              detail: `${target.cpName} (단가: ${(Number(target.unitPrice) || 0).toLocaleString()}원)`,
              life: 2500
            });
          } else {
            toast.add({
              severity: 'warn',
              summary: '공급업체 확인 필요',
              detail: '전달된 공급업체가 이 자재와 매칭되지 않았습니다. 직접 선택해주세요.',
              life: 3000
            });
          }
        } else {
          // 공급업체 조회 -> 1건이면 자동, 여러건이면 최저가 자동 선택
          const suppliers = await loadSuppliersByMaterial(q.mcode, q.mateVerCd || undefined);
          if (suppliers && suppliers.length > 0) {
            let chosen = null;
            if (suppliers.length === 1) {
              chosen = suppliers[0];
              toast.add({ severity: 'info', summary: '공급업체 자동 선택', detail: `${chosen.cpName} 1건`, life: 2200 });
            } else {
              chosen = suppliers.reduce((min, s) => (Number(s.unitPrice) < Number(min.unitPrice) ? s : min), suppliers[0]);
              toast.add({ severity: 'info', summary: '최저가 자동 선택', detail: `${chosen.cpName} (단가 ${Number(chosen.unitPrice).toLocaleString()}원)`, life: 2600 });
            }

            purchaseData.value[0].buyer = chosen.cpName;
            purchaseData.value[0].cpCd = chosen.cpCd;
            purchaseData.value[0].price = Number(chosen.unitPrice) || 0;
            purchaseData.value[0].totalPrice = (Number(purchaseData.value[0].number) || 0) * (Number(chosen.unitPrice) || 0);
          } else {
            toast.add({ severity: 'warn', summary: '공급업체 없음', detail: '이 자재의 공급업체를 찾지 못했습니다. 직접 선택해주세요.', life: 3000 });
          }
        }
      } catch (e) {
        console.error('공급업체 자동 선택 실패:', e);
      }
    }

    // 🧩 스토어에 사전 채운 데이터가 있는 경우(여러 건) → 공급업체 자동 배정/단가 세팅
    if (purchaseData.value && purchaseData.value.length > 0) {
      try {
        for (let i = 0; i < purchaseData.value.length; i++) {
          const item = purchaseData.value[i];
          if (!item.mcode) continue;

          // cpCd가 이미 있으면 스킵
          if (item.cpCd) continue;

          const suppliers = await loadSuppliersByMaterial(item.mcode, item.mateVerCd || undefined);
          if (suppliers && suppliers.length > 0) {
            let chosen = null;
            if (suppliers.length === 1) {
              chosen = suppliers[0];
            } else {
              chosen = suppliers.reduce((min, s) => (Number(s.unitPrice) < Number(min.unitPrice) ? s : min), suppliers[0]);
            }

            item.buyer = chosen.cpName;
            item.cpCd = chosen.cpCd;
            item.price = Number(chosen.unitPrice) || 0;
            item.totalPrice = (Number(item.number) || 0) * (Number(chosen.unitPrice) || 0);
          }
        }
      } catch (e) {
        console.error('다중 항목 공급업체 자동 배정 실패:', e);
      }
    }

  } catch (error) {
    console.error('❌ 초기화 중 오류:', error);
    toast.add({
      severity: 'warn',
      summary: '공통코드 로드 실패',
      detail: '일부 기능이 제한될 수 있습니다.',
      life: 3000
    });
  }
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

// 발주서 목록 로드 (실제 API 호출)
const loadOrderList = async () => {
  try {
    console.log('📋 발주서 목록 로드 시작...');

    const response = await getPurcOrderList();
    console.log('발주서 목록 API 응답:', response.data);

    // date-fns로 날짜 포맷팅
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
  console.log('🧹 MaterialPurchase 컴포넌트 언마운트됨: 상태 초기화');
  // 발주 상세 데이터 초기화 (다음 진입 시 새로 채우도록)
  purchaseData.value = [];
  // 모달/목록 상태 초기화
  orderListModalVisible.value = false;
  orderList.value = [];
  // 기본정보는 마운트 시 다시 세팅되므로 명시 초기화는 생략
});
</script>

<template>
  <div class="p-4">
    <Toast />

    <!-- 페이지 헤더 -->
    <div class="mb-6 flex flex-col-2">
    <div class="flex-1">
      <h1 class="text-3xl font-bold text-gray-800 mb-2">자재 발주서 작성</h1>
    </div>
      <div class="flex items-center gap-4 text-sm text-gray-600">
        <span>👤 {{ memberStore.user?.empName || '김김밥1' }}</span>
        <span>🏢 {{ memberStore.user?.deptName || '구매팀' }}</span>
      </div>
    </div>

    <!-- 주문 기본정보 (발주번호, 등록자, 주문일자만!) -->
    <div class="mb-6">
      <InputForm :columns="basicInfoColumns" :data="orderBasicInfo" title="주문 기본정보" :buttons="formButtons"
        button-position="top" @update:data="(newData) => { orderBasicInfo = newData }" @submit="handleSavePurchaseOrder"
        @reset="handleReset" @load="handleLoad" />
    </div>

    <!-- 자재 발주 상세-->
    <div>
      <InputTable title="자재 발주 상세" :scroll-height="'40vh'" :height="'50vh'" :columns="materialColumns"
        :data="convertedMaterialList" :buttons="tableButtons" :enableRowActions="true" :enableSelection="true"
        :modalDataSets="convertedModalDataSets" :autoCalculation="{
          enabled: true,
          quantityField: 'number',
          priceField: 'price',
          totalField: 'totalPrice'
        }" :showRowCount="true" dataKey="mcode" @dataChange="handleDataChange" />
    </div>

    <!-- 발주 요약 (데이터가 있을 때만 표시) -->
    <div v-if="orderSummary.itemCount > 0" class="mt-6 p-4 bg-blue-50 rounded-lg border border-blue-200">
      <h3 class="text-lg font-semibold text-blue-800 mb-2">발주 요약</h3>
      <div class="grid grid-cols-2 md:grid-cols-3 gap-4 text-sm">
        <div>
          <span class="text-gray-600">총 자재 수:</span>
          <span class="font-bold ml-2">{{ orderSummary.itemCount }}개</span>
        </div>
        <div>
          <span class="text-gray-600">공급업체 수:</span>
          <span class="font-bold ml-2">{{ orderSummary.supplierCount }}개</span>
        </div>
        <div>
          <span class="text-gray-600">총 발주 금액:</span>
          <span class="font-bold ml-2 text-blue-600">{{ orderSummary.totalAmount.toLocaleString() }}원</span>
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