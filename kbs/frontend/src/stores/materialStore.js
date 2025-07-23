import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

export const useMaterialStore = defineStore('material', () => {
  const materials = ref([]);

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
  
  // 입고 처리된 자재들을 추가하는 함수
  const addProcessedInboundMaterials = (materials) => {
    processedInboundMaterials.value.push(...materials);
  };

  // 모달 데이터 설정 (computed로 반응형 처리)
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
    modalDataSets // 모달 데이터셋 추가!!
  };
});