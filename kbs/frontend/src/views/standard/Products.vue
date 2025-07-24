<script setup>
import { ref, onBeforeMount, onMounted, computed } from 'vue';
import { storeToRefs } from 'pinia';
import { useStandardMatStore } from '@/stores/standardMatStore';
import { format } from 'date-fns';
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import InputForm from '@/components/kimbap/searchform/inputForm.vue';
import StandardTable from '@/components/kimbap/table/StandardTable.vue';
import InputTable from '@/components/kimbap/table/InputTable.vue';

// 오늘 날짜 포맷
const today = format(new Date(), 'yyyy-MM-dd');

// Pinia store
const store = useStandardMatStore();
const { materialList, supplierList, formData, supplierData } = storeToRefs(store);
const { fetchMaterials, fetchSuppliers, fetchMaterialDetail, saveMaterial  } = store;

// UI 설정용 ref
const searchColumns = ref([]);
const inputColumns = ref([]);
const cpColumns = ref([]);
const productColumns = ref([]);
const inputFormButtons = ref({});
const rowButtons = ref({});
// 공급처 모달 데이터셋
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

onBeforeMount(() => {
  // 검색 컬럼
  searchColumns.value = [
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

  // 자재 입력 폼 컬럼
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
      disabled: (row) => row.unit !== 'g6'
    },
    { key: 'moqty', label: '최소발주단위', type: 'number' },
    { key: 'edate', label: '소비기한(일)', type: 'text' },
    { key: 'safeStock', label: '안전재고', type: 'number' },
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

  // 공급처 테이블 컬럼
  cpColumns.value = [
    { field: 'cpCd', header: '거래처코드', type: 'inputsearch', width: '250px', placeholder: '거래처 선택', suffixIcon: 'pi pi-search' },
    { field: 'cpName', header: '거래처명', width: '100px', type: 'input' },
    { field: 'unitPrice', header: '단가(원)', width: '100px', type: 'input', inputType: 'number', placeholder: '단가를 입력하세요' },
    { field: 'ltime', header: '리드타임(일)', width: '100px', type: 'input', inputType: 'number', placeholder: '리드타임을 입력하세요' }
  ];

  // 자재 목록 컬럼
  productColumns.value = [
    { field: 'mcode', header: '자재코드' },
    { field: 'mateName', header: '자재명' },
    { field: 'mateType', header: '유형' },
    { field: 'stoCon', header: '보관조건' },
    { field: 'edate', header: '소비기한' }
  ];

  // 버튼
  inputFormButtons.value = {
    save: { show: true, label: '저장', severity: 'success' }
  };
});

onMounted(() => {
  fetchSuppliers();
  fetchMaterials();
});

// 자재기준정보 등록
const handleSaveMaterial = async () => {
  const result = await store.saveMaterial();

  if (result === '등록 성공') {
    alert('등록 성공');
  } else {
    alert(result);
  }
};

// 자재 단건조회
const handleSelectMaterial = async (selectedRow) => {
  await fetchMaterialDetail(selectedRow.mcode);  // 🔥 핵심
  console.log('formData.value:', formData.value);          // ✅ 자재 정보 확인
  console.log('supplierData.value:', supplierData.value);  // ✅ 공급사 목록 확인
};

</script>
<template>
    <SearchForm :columns="searchColumns" @search="handleSearch" @reset="handleReset" />

    <div class="flex flex-col md:flex-row gap-4 mt-6">
        <div class="w-full md:basis-[55%]">
            <StandardTable title="자재기준정보 목록" :data="materialList" dataKey="mcode" :columns="productColumns" @view-history="handleViewHistory" @row-select="handleSelectMaterial" :scrollable="true" scrollHeight="230px" height="320px" class="mb-2" />
            <InputTable title="자재별 공급처" v-model:data="supplierData"  :columns="cpColumns"  :buttons="rowButtons" dataKey="cpCd" :modalDataSets="modalDataSets" button-position="top" scrollHeight="205px" height="300px" />
        </div>

        <div class="w-full md:basis-[45%]">
            <InputForm title="자재정보" :columns="inputColumns" v-model:data="formData" :buttons="inputFormButtons" @submit="handleSaveMaterial" />
        </div>
    </div>
</template>
