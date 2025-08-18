// stores/standardMatStore.js
import { defineStore } from 'pinia';
import { ref } from 'vue';
import { format } from 'date-fns';
import { getMaterialList, insertMaterial, getSupplierList, getMaterialDetail, updateMaterial, getChangeHistory   } from '@/api/standard';

export const useStandardMatStore = defineStore('standardMat', () => {
  // 전역 데이터 상태만
  const materialList     = ref([]);    // 자재 목록
  const selectedMaterial = ref(null);  // 선택된 자재
  const supplierList     = ref([]);    // 공급처 목록
  const searchFilter     = ref({});    // 검색 필터
  const formData = ref({});          // 단건 자재 정보
  const supplierData = ref([]);      // 자재별 공급처 정보
  const changeHistory = ref([]);    // 변경 이력 조회
  const defaultSuppliers = () => []; // 항상 배열
  const resetForm = () => {
    formData.value = defaultForm();       // 객체(폼)는 “같은 모양”으로 초기화
    supplierData.value = defaultSuppliers(); // 공급사 리스트는 빈 배열 유지
  };
  
  const defaultForm = () => ({
    mcode: null,
    mateVerCd: null,
    mateName: null,
    unit: null,
    moqty: null,
    safeStock: null,
    std: null,
    pieceUnit: null,
    edate: null,
    converQty: null,
    // 필요 필드 추가
  });

  // 빈문자열 처리함수
  function sanitizeFormData(obj) {
    const result = { ...obj };
    for (const key in result) {
      if (result[key] === '') {
        result[key] = null;
      }
    }
    return result;
  };

  // 비동기 액션
  const fetchMaterials = async () => {
    try {
      const res = await getMaterialList();
      materialList.value = res.data;
    } catch (err) {
      console.error('자재 목록 조회 실패:', err);
    }
  };

  // 자재 등록 
  const addMaterial = async (data) => {
    try {
      const res = await insertMaterial(data);
      if (res.data?.success) {
        await fetchMaterials();
      }
      return res; // ✅ res 전체 반환 (status 포함)
    } catch (err) {
      console.error('자재 등록 실패:', err);
      throw err;
    }
  };

  // 공급사 목록 조회
  const fetchSuppliers = async () => {
    try {
      const res = await getSupplierList();
      supplierList.value = res.data;
    } catch (err) {
      console.error('공급처 조회 실패:', err);
    }
  };

  const setSearchFilter = (filter) => {
    searchFilter.value = filter;
  };

  const selectMaterial = (row) => {
    selectedMaterial.value = row;
  };
  
  // 자재 단건조회 
  const fetchMaterialDetail = async (mcode) => {
    try {
      const { data } = await getMaterialDetail(mcode);
      console.log('API 응답:', data);

      // ✅ 등록일자 포맷 처리
      if (data.material?.regDt) {
        data.material.regDt = format(new Date(data.material.regDt), 'yyyy-MM-dd');
      }

      formData.value = data.material;
      supplierData.value = data.suppliers;
    } catch (err) {
      console.error('자재 단건 조회 실패:', err);
    }
  };

  // 자재등록로직
  const saveMaterial = async () => {
    try {
      const sanitized = sanitizeFormData(formData.value);

      sanitized.moqty = sanitized.moqty !== null ? Number(sanitized.moqty) : null;
      sanitized.safeStock = sanitized.safeStock !== null ? Number(sanitized.safeStock) : null;
      sanitized.edate = sanitized.edate !== null ? Number(sanitized.edate) : null;
      sanitized.converQty = sanitized.converQty !== null ? Number(sanitized.converQty) : null;

      sanitized.suppliers = supplierData.value.map((s) => ({
        cpCd: s.cpCd,
        unitPrice: s.unitPrice != null && s.unitPrice !== '' ? Number(s.unitPrice) : null,
        ltime: s.ltime != null && s.ltime !== '' ? Number(s.ltime) : null
      }));

      // 수정 시 mateVerCd 제거 (백엔드에서 처리)
      if (sanitized.mcode) {
        delete sanitized.mateVerCd;
      } else {
        sanitized.mateVerCd = 'V001';
        sanitized.regi = 'admin'; // TODO: 실제 사용자 ID로 대체
      }

      let res;
      if (!sanitized.mcode) {
        res = await insertMaterial(sanitized);
      } else {
        res = await updateMaterial(sanitized);
      }

      if (res?.status === 200 && res.data?.success) {
        supplierData.value = [];  
        formData.value = {};
        // ✅ 목록 자동 갱신
        await fetchMaterials();

        return sanitized.mcode ? '수정 성공' : '등록 성공';
      } else {
        return sanitized.mcode ? '수정 실패' : '등록 실패';
      }
    } catch (err) {
      console.error('❌ 저장 실패:', err);
      return '예외 발생';
    }
  };

  // 변경이력조회 
  const fetchChangeHistory = async (mcode) => {
    try {
      const res = await getChangeHistory(mcode);
      changeHistory.value = res.data.map(item => ({
        ...item,
        regDt: format(new Date(item.regDt), 'yyyy-MM-dd'),
      }));
    } catch (error) {
      console.error('변경이력 조회 실패:', error);
    }
  };
  

  return {
    materialList,
    selectedMaterial,
    supplierList,
    searchFilter,
    formData,
    supplierData,
    changeHistory,
    defaultForm,
    resetForm,
    fetchMaterials,
    fetchSuppliers,
    addMaterial,
    setSearchFilter,
    selectMaterial,
    fetchMaterialDetail,
    saveMaterial,
    fetchChangeHistory
  };
});
