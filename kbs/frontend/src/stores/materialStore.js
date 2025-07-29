import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { format } from 'date-fns';

export const useMaterialStore = defineStore('material', () => {
  const materials = ref([]);
  const formatDate = (date) => {
    if (!date) return null;
    return format(new Date(date), 'yyyy-MM-dd');
  };

  const setMaterials = (newMaterials) => {
    materials.value = newMaterials;
  };
  const searchColumns = ref([]);
  const purchaseColumns = ref([]);
  const purchaseData = ref([]);
//------------------------------------------------------------------------------------------------------------------------
  const inboundData = ref({});
  const inMaterialColumns = ref([]);
  const warehouseColumns = ref([]);
  const inboundFields = ref([]);
  // 선택된 자재들을 저장할 새로운 필드 추가
  const selectedInboundMaterials = ref([]);
  
  // 선택된 자재들을 설정하는 함수
  const setSelectedInboundMaterials = (materials) => {
    selectedInboundMaterials.value = materials;
  };
  
  // 입고 처리된 자재들을 저장할 필드 (히스토리 관리용)
  const processedInboundMaterials = ref([]);
  const purchaseFormButtons = ref([]);
  const materialTableButtons = ref([]);
  
  // 입고 처리된 자재들을 추가하는 함수
  const addProcessedInboundMaterials = (materials) => {
    processedInboundMaterials.value.push(...materials);
  };

//------------------------------------------------------------------------------------------------------------------------
  // 구매/발주 관련 데이터
  const purchaseOrderData = ref([]);
  const purchaseOrderDetailData = ref([]);
  
  // 내부직원용 구매/발주 컬럼 설정 (수정!)
  const internalPurchaseColumns = ref([
    { field: 'purcCd', header: '발주번호', sortable: true },
    { field: 'purcDCd', header: '발주상세번호', sortable: true },
    { field: 'mateName', header: '자재명', sortable: true },        // materialName → mateName
    { field: 'mateType', header: '자재유형', sortable: true },      // materialType → mateType
    { field: 'purcQty', header: '수량', sortable: true },
    { field: 'unit', header: '단위', sortable: true },
    { field: 'exDeliDt', header: '납기예정일', sortable: true },
    { field: 'deliDt', header: '납기일', sortable: true },          // actualDeliDt → deliDt
    { field: 'purcDStatus', header: '발주상태', sortable: true },
    { field: 'ordDt', header: '주문일자', sortable: true },
    { field: 'note', header: '비고', sortable: false }
  ]);

  // 공급업체직원용 구매/발주 컬럼 설정 (수정!)
  const supplierPurchaseColumns = ref([
    { field: 'purcCd', header: '발주번호', sortable: true },
    { field: 'purcDCd', header: '발주상세번호', sortable: true },
    { field: 'mcode', header: '자재코드', sortable: true },         // 추가!
    { field: 'mateName', header: '자재명', sortable: true },        // materialName → mateName
    { field: 'purcQty', header: '수량', sortable: true },
    { field: 'unit', header: '단위', sortable: true },
    { field: 'exDeliDt', header: '납기일', sortable: true },
    { field: 'purcDStatus', header: '발주상태', sortable: true },
    { field: 'totalAmount', header: '총금액(원)', sortable: true }, // ordTotalAmount → totalAmount
    { field: 'note', header: '비고', sortable: false }
  ]);

  // 구매/발주 상태 옵션 (실제 DB 값에 맞게 수정)
  const purchaseStatusOptions = ref([
    { label: '주문완료', value: 'c1' },
    { label: '제작중', value: 'c2' },
    { label: '배송완료', value: 'c3' },
    { label: '취소', value: 'c4' }
  ]);

  // 내부직원용 구매/발주 검색 컬럼 설정 (수정!)
  const internalPurchaseSearchColumns = computed(() => [
    { 
      key: 'purcDCd', 
      label: '발주상세번호', 
      type: 'text',
      placeholder: '발주상세번호 검색'
    },
    { 
      key: 'mateName',         // materialName → mateName
      label: '자재명', 
      type: 'text',
      placeholder: '자재명 검색'
    },
    { 
      key: 'mateType',         // materialType → mateType
      label: '자재유형', 
      type: 'dropdown',
      options: [
        { label: '전체', value: '' },
        { label: '원자재', value: 'h1' },
        { label: '부자재', value: 'h2' },
        { label: '완제품', value: 'h3' }
      ],
      placeholder: '자재유형 검색'
    },
    { 
      key: 'purcDStatus', 
      label: '발주상태', 
      type: 'dropdown',
      options: [
        { label: '전체', value: '' },
        ...purchaseStatusOptions.value
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
  ]);

  // 공급업체직원용 구매/발주 검색 컬럼 설정 (수정!)
  const supplierPurchaseSearchColumns = computed(() => [
    { 
      key: 'mcode', 
      label: '자재코드', 
      type: 'text',
      placeholder: '자재코드 검색'
    },
    { 
      key: 'mateName',         // materialName → mateName
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
        ...purchaseStatusOptions.value
      ],
      placeholder: '상태 선택'
    }
  ]);

  // 구매/발주 데이터 설정 함수
  const setPurchaseOrderData = (data) => {
    purchaseOrderData.value = data;
  };

  const setPurchaseOrderDetailData = (data) => {
    purchaseOrderDetailData.value = data;
  };

  // 구매/발주 상태 업데이트 함수
  const updatePurchaseStatus = (purcDCd, newStatus) => {
    const item = purchaseOrderDetailData.value.find(item => item.purcDCd === purcDCd);
    if (item) {
      item.purcDStatus = newStatus;
    }
  };

  const materialSupplierCombinations = ref([]);
  
  const setMaterialSupplierCombinations = (data) => {
    materialSupplierCombinations.value = data;
    console.log('Store에 자재-공급업체 조합 저장 완료:', data.length, '건');
  };

  const purchaseModalData = computed(() => ({
    // 자재명 중심 모달
    materialName: {
      items: materialSupplierCombinations.value.map(item => ({
        ...item,
        uniqueKey: `${item.mcode}-${item.mateVerCd}-${item.cpCd}` // 추가!
      })),
      columns: [
        { field: 'mcode', header: '자재코드' },
        { field: 'mateName', header: '자재명' },
        { field: 'cpName', header: '공급업체' },
        { field: 'unitPrice', header: '단가(원)' },
        { field: 'unit', header: '단위' },
        { field: 'ltime', header: '리드타임' }
      ],
      displayField: 'mateName',
      mappingFields: {
        materialName: 'mateName',
        buyer: 'cpName',  // 공급업체로 매핑
        mcode: 'mcode',
        mateVerCd: 'mateVerCd',
        cpCd: 'cpCd',
        unit: 'unit',
        price: 'unitPrice',  // 단가 자동 입력
        uniqueKey: 'uniqueKey'
      }
    },
    // 공급업체명 중심 모달
    buyer: {
      items: materialSupplierCombinations.value.map(item => ({
        ...item, 
        uniqueKey: `${item.mcode}-${item.mateVerCd}-${item.cpCd}` // 같은 키!
      })),
      columns: [
        { field: 'cpName', header: '공급업체' },
        { field: 'mateName', header: '자재명' },
        { field: 'unitPrice', header: '단가(원)' },
        { field: 'ltime', header: '리드타임' },
        { field: 'unit', header: '단위' }
      ],
      displayField: 'cpName',
      mappingFields: {
        buyer: 'cpName',
        materialName: 'mateName',
        mcode: 'mcode',
        mateVerCd: 'mateVerCd', 
        cpCd: 'cpCd',
        unit: 'unit',
        price: 'unitPrice',  // 단가 자동 입력
        uniqueKey: 'uniqueKey'
      }
    }
  }));

  // 모달 데이터 설정 (기존 - 호환성 유지)
  const modalDataSets = computed(() => ({
    buyer: {
      items: [
        { id: 1, code: 'B001', name: '삼성전자', category: '대기업', contact: '02-1234-5678' },
        { id: 2, code: 'B002', name: 'LG전자', category: '대기업', contact: '02-9876-5432' },
        { id: 3, code: 'B003', name: '현대자동차', category: '대기업', contact: '02-5555-1234' },
        { id: 4, code: 'B004', name: '소상공인협회', category: '중소기업', contact: '02-7777-8888' },
        { id: 5, code: 'B005', name: '네이버', category: 'IT기업', contact: '031-1111-2222' }
      ],
      columns: [
        { field: 'code', header: '거래처코드' },
        { field: 'name', header: '거래처명' },
        { field: 'category', header: '분류' },
        { field: 'contact', header: '연락처' }
      ],
      displayField: 'name',
      mappingFields: {
        buyerCode: 'code',
        buyerName: 'name',
        buyerCategory: 'category',
        buyerContact: 'contact'
      }
    },
    unit: {
      items: [
        { id: 1, code: 'U001', name: '개', type: '개수', desc: '낱개 단위' },
        { id: 2, code: 'U002', name: 'kg', type: '무게', desc: '킬로그램' },
        { id: 3, code: 'U003', name: 'box', type: '포장', desc: '박스 단위' },
        { id: 4, code: 'U004', name: 'm', type: '길이', desc: '미터' },
        { id: 5, code: 'U005', name: 'L', type: '용량', desc: '리터' },
        { id: 6, code: 'U006', name: 'ton', type: '무게', desc: '톤 단위' }
      ],
      columns: [
        { field: 'code', header: '단위코드' },
        { field: 'name', header: '단위명' },
        { field: 'type', header: '분류' },
        { field: 'desc', header: '설명' }
      ],
      displayField: 'name',
      mappingFields: {
        unitCode: 'code',
        unitName: 'name',
        unitType: 'type',
        unitDesc: 'desc'
      }
    }
  }));

  return {
    materials,
    setMaterials,
    searchColumns,
    purchaseColumns,
    purchaseData,
    inboundData,
    inMaterialColumns,
    warehouseColumns,
    inboundFields,
    selectedInboundMaterials,
    setSelectedInboundMaterials,
    processedInboundMaterials,
    addProcessedInboundMaterials,
    // 구매/발주 관련 추가
    purchaseOrderData,
    purchaseOrderDetailData,
    internalPurchaseColumns,
    supplierPurchaseColumns,
    internalPurchaseSearchColumns,
    supplierPurchaseSearchColumns,
    purchaseStatusOptions,
    setPurchaseOrderData,
    setPurchaseOrderDetailData,
    updatePurchaseStatus,
    modalDataSets,
    purchaseFormButtons,
    materialTableButtons,
    materialSupplierCombinations,
    setMaterialSupplierCombinations,
    purchaseModalData
  };
});