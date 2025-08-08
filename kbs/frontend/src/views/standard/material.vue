<script setup>
import { ref, onBeforeMount, onMounted, computed } from 'vue';
import { storeToRefs } from 'pinia';
import { format, min } from 'date-fns';
import { useStandardMatStore } from '@/stores/standardMatStore';
import { useCommonStore } from '@/stores/commonStore'
import { useMemberStore } from '@/stores/memberStore';
import { useToast } from 'primevue/usetoast';
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import InputForm from '@/components/kimbap/searchform/inputForm.vue';
import StandardTable from '@/components/kimbap/table/StandardTable.vue';
import InputTable from '@/components/kimbap/table/InputTable.vue';
import BasicModal from '@/components/kimbap/modal/basicModal.vue';

// 오늘 날짜 포맷 (등록일자 default 값에 사용)
const today = format(new Date(), 'yyyy-MM-dd');

// 공통코드 가져오기
const common = useCommonStore()
const { commonCodes } = storeToRefs(common)
const convertedMaterialList = computed(() => convertUnitCodes(materialList.value));
const memberStore = useMemberStore();
const { user } = storeToRefs(memberStore);
const toast = useToast();

const isEmployee = computed(() => user.value?.memType === 'p1');
const isManager = computed(() => user.value?.memType === 'p4');
const isAdmin = computed(() => user.value?.memType === 'p5');


// 공통코드 형변환
const convertUnitCodes = (list) => {
  const mateTypeCodes = common.getCodes('0H'); // 자재유형
  const stoConCodes = common.getCodes('0O');   // 보관조건
  const UnitCodes = common.getCodes('0G');   // 보관조건

  return list.map(item => {
    const matchedMateType = mateTypeCodes.find(code => code.dcd === item.mateType);
    const matchedStoCon = stoConCodes.find(code => code.dcd === item.stoCon);
    const matchedUnit = UnitCodes.find(code => code.dcd === item.unit);

    return {
      ...item,
      mateType: matchedMateType ? matchedMateType.cdInfo : item.mateType,
      stoCon: matchedStoCon ? matchedStoCon.cdInfo : item.stoCon,
      unit: matchedUnit ? matchedUnit.cdInfo : item.unit,
    };
  });
};

// Pinia Store 상태 및 함수 바인딩
const store = useStandardMatStore();
const { materialList, supplierList, formData, supplierData, changeHistory } = storeToRefs(store);
const { fetchMaterials, fetchSuppliers, fetchMaterialDetail, saveMaterial } = store;

// UI 상태 정의
const searchColumns = ref([]); // 검색 컬럼
const inputColumns = ref([]); // 입력 폼 컬럼
const cpColumns = ref([]); // 공급처 테이블 컬럼
const mataerialColumns = ref([]); // 자재목록 테이블 컬럼
const inputFormButtons = ref({}); // 자재 등록 버튼
const rowButtons = ref({}); // 공급처 테이블용 버튼
const selectedMaterial = ref({});
const exportColumns = ref([]);

// 이력조회 모달 관련 상태 및 핸들러
const selectedHistoryItems = ref([]);
const historyModalVisible = ref(false); // 모달 표시 여부
const selectedMcode = ref(''); // 선택된 자재코드

const changeColumns = [
    { field: 'version', header: '버전' },
    { field: 'fieldName', header: '변경항목' },
    { field: 'oldValue', header: '변경 전 값' },
    { field: 'newValue', header: '변경 후 값' },
    { field: 'changeReason', header: '변경사유' },
    { field: 'regDt', header: '등록일자' }
];

// 함수 내용만 교체
const fetchHistoryItems = async () => {
    if (!selectedMcode.value) {
        console.warn('mcode가 비어있습니다');
        return [];
    }

    // API 호출로 이력 데이터 새로 조회
    await store.fetchChangeHistory(selectedMcode.value);

    // store에서 가져온 changeHistory를 selectedHistoryItems에 복사
    selectedHistoryItems.value = changeHistory.value;

    return changeHistory.value;
};

// 테이블에서 "이력조회" 버튼 클릭 시 실행되는 핸들러
const handleViewHistory = async (rowData) => {
    selectedMcode.value = rowData.mcode;
    selectedMaterial.value = { mateName: rowData.mateName, mcode: rowData.mcode }; // ✅ 안전하게 저장
    await store.fetchChangeHistory(rowData.mcode);
    historyModalVisible.value = true;
};

// 📦 6. 공급처 선택용 모달 설정
const modalDataSets = computed(() => ({
    cpCd: {
        items: supplierList.value,
        columns: [
            { field: 'cpCd', header: '거래처코드' },
            { field: 'cpName', header: '거래처명' },
            { field: 'tel', header: '전화번호' }
        ],
        displayField: 'cpCd',
        mappingFields: { cpCd: 'cpCd', cpName: 'cpName' }
    }
}));

// UI 구성 정의
onBeforeMount(() => {
    searchColumns.value = [
        { key: 'mcode', label: '자재코드', type: 'text', placeholder: '자재코드를 입력하세요' },
        { key: 'mateName', label: '자재명', type: 'text', placeholder: '자재명을 입력하세요' },
        {
            key: 'mateType',
            label: '자재유형',
            type: 'dropdown',
            options: [
                { label: '원자재', value: 'h1' },
                { label: '부자재', value: 'h2' }
            ]
        },
        {
            key: 'stoCon',
            label: '보관조건',
            type: 'dropdown',
            options: [
                { label: '상온', value: 'o1' },
                { label: '냉장', value: 'o2' },
                { label: '냉동', value: 'o3' }
            ]
        }
    ];

    inputColumns.value = [
        { key: 'mcode', label: '자재코드', type: 'readonly' },
        { key: 'mateName', label: '자재명', type: 'text' },
        {
            key: 'mateType',
            label: '자재유형',
            type: 'dropdown',
            options: [
                { label: '원자재', value: 'h1' },
                { label: '부자재', value: 'h2' }
            ]
        },
        {
            key: 'stoCon',
            label: '보관조건',
            type: 'dropdown',
            options: [
                { label: '상온', value: 'o1' },
                { label: '냉장', value: 'o2' },
                { label: '냉동', value: 'o3' }
            ]
        },
        {
            key: 'unit',
            label: '단위',
            type: 'dropdown',
            options: [
                { label: 'kg', value: 'g2' },
                { label: 'box', value: 'g6' }
            ]
        },
        { key: 'std', label: '규격', type: 'text' },
        {
            key: 'pieceUnit',
            label: '낱개단위',
            type: 'dropdown',
            options: [
                { label: '매', value: '매' },
                { label: '장', value: '장' },
                { label: 'EA', value: 'ea' }
            ],
            disabled: (row) => row.unit !== 'g6'
        },
        {
            key: 'converQty',
            label: '환산수량',
            type: 'number',
            min: 0,
            disabled: (row) => row.unit !== 'g6'
        },
        { key: 'moqty', label: '최소발주단위', type: 'number', min: 0 },
        { key: 'edate', label: '소비기한(일)', type: 'text'  },
        { key: 'safeStock', label: '안전재고', type: 'number',min: 0 },
        { key: 'corigin', label: '원산지', type: 'text' },
        {
            key: 'isUsed',
            label: '사용여부',
            type: 'radio',
            options: [
                { label: '활성화', value: 'f1' },
                { label: '비활성화', value: 'f2' }
            ]
        },
        {
            key: 'chaRea',
            label: '변경사유',
            type: 'text',
            disabled: (row) => !row.mcode
        },
        { key: 'note', label: '비고', type: 'textarea', rows: 1, cols: 20 },
        { key: 'regDt', label: '등록일자', type: 'readonly', defaultValue: today }
    ];

    cpColumns.value = [
        { field: 'cpCd', header: '거래처코드', type: 'inputsearch', width: '100px',align: "left" ,placeholder: '거래처 선택', suffixIcon: 'pi pi-search' },
        { field: 'cpName', header: '거래처명', width: '140px', type: 'input' },
        { field: 'unitPrice', header: '단가(원)', width: '80px', type: 'input',align: "right", inputType: 'number', placeholder: '단가를 입력하세요' },
        { field: 'ltime', header: '리드타임(일)', width: '80px', type: 'input', align: "right",inputType: 'number', placeholder: '리드타임을 입력하세요' }
    ];

    mataerialColumns.value = [
        { field: 'mcode', header: '자재코드' },
        { field: 'mateName', header: '자재명' },
        { field: 'mateType', header: '유형' },
        { field: 'stoCon', header: '보관조건' },
        { field: 'edate', header: '소비기한(일)', align: 'right', slot: true }
    ];

    inputFormButtons.value = {
        save: { show: isAdmin.value || isManager.value, label: '저장', severity: 'success' }
    };
    // 엑셀 다운로드용 컬럼
    exportColumns.value = [
        { field: 'mcode', header: '자재코드' },
        { field: 'mateName', header: '자재명' },
        { field: 'mateType', header: '자재유형' },
        { field: 'stoCon', header: '보관조건' },
        { field: 'unit', header: '단위' },
        { field: 'moqty', header: '최소발주단위' },
        { field: 'edate', header: '소비기한(일)' },
        { field: 'safeStock', header: '안전재고(일)' },
        { field: 'cpCd', header: '거래처코드' },
        { field: 'cpName', header: '거래처명' },
        { field: 'unitPrice', header: '단가' },
        { field: 'ltime', header: '리드타임(일)' },
        
    ]
});

// ⚙️ 8. 데이터 fetch (초기 자재/공급처 목록)
onMounted(async() => {
    await common.fetchCommonCodes('0H')  // 자재유형
    await common.fetchCommonCodes('0O')  // 보관조건
    await common.fetchCommonCodes('0G')  // 단위
    await fetchSuppliers();
    await fetchMaterials();
});

// 💾 9. 자재 등록 처리
const handleSaveMaterial = async () => {
    if (!isAdmin.value && !isManager.value) {
    toast.add({
      severity: 'error',
      summary: '등록 실패',
      detail: '등록 권한이 없습니다.',
      life: 3000
    });
    return;
    }
    if (!user.value?.empCd) {
        toast.add({
            severity: 'warn',
            summary: '경고',
            detail: '로그인 정보가 없습니다.',
            life: 3000
        });
        return;
    }
    // 신규 등록이면 regi, 수정이면 modi 설정
    if (!formData.value.mcode) {
        formData.value.regi = user.value.empCd;
    } else {
        formData.value.modi = user.value.empCd;
    }
    const result = await saveMaterial();

    if (result === '등록 성공' || result === '수정 성공') {
        toast.add({
        severity: 'success',
        summary: result,
        detail: `자재가 정상적으로 ${result.replace('성공', '')}되었습니다.`,
        life: 3000
        });
    } else {
        toast.add({
        severity: 'error',
        summary: result.includes('예외') ? '예외 발생' : '저장 실패',
        detail: result,
        life: 3000
        });
    }
};

// 📄 10. 자재 단건 조회 처리
const handleSelectMaterial = async (selectedRow) => {
    await fetchMaterialDetail(selectedRow.mcode);
};
const clearForm = () => {
    formData.value = {}; // 또는 필요한 초기화 방식으로
    supplierData.value = [];
};
// 검색
const handleSearch = async (searchData) => {
  await fetchMaterials(); // 최신 데이터 가져오기

  // 조건 키: mcode, mateName, mateType, stoCon
  materialList.value = materialList.value.filter((item) => {
    const matchMcode     = !searchData.mcode     || item.mcode?.toLowerCase().includes(searchData.mcode);
    const matchMateName  = !searchData.mateName  || item.mateName?.includes(searchData.mateName);
    const matchMateType  = !searchData.mateType  || item.mateType === searchData.mateType;
    const matchStoCon    = !searchData.stoCon    || item.stoCon === searchData.stoCon;

    return matchMcode && matchMateName && matchMateType && matchStoCon;
  });

  if (materialList.value.length === 0) {
    toast.add({
      severity: 'info',
      summary: '검색 결과 없음',
      detail: '조건에 해당하는 자재가 없습니다.',
      life: 3000
    });
  } else {
    toast.add({
      severity: 'success',
      summary: '검색 성공',
      detail: `총 ${materialList.value.length}건의 자재가 검색되었습니다.`,
      life: 3000
    });
  }
};

const handleReset = async () => {
  await fetchMaterials(); // 전체 목록 다시 조회
    toast.add({
        severity: 'info',
        summary: '초기화 완료 ✨',
        detail: '검색 조건이 초기화되고 전체 목록을 조회했습니다.',
        life: 3000
    });
};

const mergedExportData = computed(() => {
  return convertedMaterialList.value.flatMap(material => {
    const relatedSuppliers = supplierData.value.filter(s => s.mcode === material.mcode);

    return relatedSuppliers.length > 0
      ? relatedSuppliers.map(supplier => ({
          ...material,
          ...supplier
        }))
      : [{
          ...material,
          cpCd: '',
          cpName: '',
          unitPrice: '',
          ltime: ''
        }];
  });
});

</script>

<template>
    <SearchForm :columns="searchColumns" @search="handleSearch" @reset="handleReset" />

    <div class="flex flex-col md:flex-row gap-4 mt-6">
        <div class="w-full md:basis-[55%]">
            <StandardTable
                title="자재 목록"
                :data="convertedMaterialList"
                dataKey="mcode"
                :columns="mataerialColumns"
                @view-history="handleViewHistory"
                @row-select="handleSelectMaterial"
                @clear-selection="clearForm"
                :scrollable="true"
                scrollHeight="230px"
                height="320px"
                :showRowCount="true"
                :showExcelDownload="true"
                :exportColumns="exportColumns"
                :exportData="mergedExportData" 
                class="mb-2"
            />
            <InputTable title="자재별 공급처" v-model:data="supplierData" :columns="cpColumns" :buttons="rowButtons" dataKey="cpCd" :modalDataSets="modalDataSets" button-position="top" scrollHeight="205px" height="300px" />
        </div>

        <div class="w-full md:basis-[45%]">
            <InputForm title="자재정보" :columns="inputColumns" v-model:data="formData" :buttons="inputFormButtons" @submit="handleSaveMaterial" />
        </div>
        <BasicModal v-model:visible="historyModalVisible" :items="changeHistory" :columns="changeColumns" :itemKey="'version'" :fetchItems="fetchHistoryItems"
        :selectedItem="selectedMaterial" :titleName="selectedMaterial.mateName" :titleCode="selectedMaterial.mcode"  />
    </div>
</template>


