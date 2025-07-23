<script setup>
import { ref, computed, onMounted, onBeforeMount } from 'vue';
import { defineProps } from 'vue';
import { useStandardMatStore } from '@/stores/standardMatStore';
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import InputForm from '@/components/kimbap/searchform/inputForm.vue';
import StandardTable from '@/components/kimbap/table/StandardTable.vue';
import InputTable from '@/components/kimbap/table/InputTable.vue';

const matStore = useStandardMatStore();

onMounted(() => {
  matStore.fetchMaterials();
});

onBeforeMount(() => {
  matStore.fetchCustomers(); // ✅ 거래처 목록도 스토어에서 관리
});

const products = computed(() => matStore.materialList);
const customers2 = computed(() => matStore.customerList); // ✅ 바인딩된 거래처 목록
const rowButtons = ref({});

const handleViewHistory = (rowData) => {
  matStore.selectMaterial(rowData);
  console.log('이력조회 클릭됨:', rowData);
};

const handleSearch = (searchData) => {
  matStore.setSearchFilter(searchData); // ✅ 검색 조건 Pinia에 저장
  console.log('검색 조건:', searchData);
};

const handleReset = () => {
  matStore.setSearchFilter({});
  console.log('검색 조건 초기화');
};

const handleSaveMaterial = async (formData) => {
  const res = await matStore.addMaterial(formData);
  if (res.success) {
    alert('자재가 등록되었습니다.');
  } else {
    alert('등록 실패: ' + res.message);
  }
};

const cpColumns = ref([
  { field: 'cpCd', header: '거래처', type: 'input', placeholder: '거래처를 입력하세요' },
  { field: 'unitPrice', header: '단가(원)', type: 'input', inputType: 'number', placeholder: '단가를 입력하세요' },
  { field: 'ltime', header: '리드타임(일)', type: 'input', inputType: 'number', placeholder: '리드타임을 입력하세요' }
]);

const productColumns = [
  { field: 'mcode', header: '자재코드' },
  { field: 'mateName', header: '자재명' },
  { field: 'mateType', header: '유형' },
  { field: 'stoCon', header: '보관조건' },
  { field: 'edate', header: '소비기한' }
];

const props = defineProps({
  searchColumns: {
    type: Array,
    default: () => [
      { key: 'mateName', label: '자재명', type: 'text', placeholder: '이름을 입력하세요' },
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
    ]
  },
  inputColumns: {
    type: Array,
    default: () => [
      { key: 'mcode', label: '자재코드', type: 'disabled', defaultValue: '자동생성' },
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
        key: 'deliveryUnit',
        label: '낱개단위',
        type: 'dropdown',
        options: [
          { label: '매', value: '매' },
          { label: '장', value: '장' },
          { label: 'EA', value: 'ea' }
        ]
      },
      { key: 'converQty', label: '환산수량', type: 'number' },
      { key: 'moqty', label: '최소발주단위', type: 'text' },
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
      { key: 'chaRea', label: '변경사유', type: 'text' },
      { key: 'note', label: '비고', type: 'textarea', rows: 1, cols: 20, placeholder: '특이사항을 입력하세요' },
      { key: 'regDt', label: '등록일자', type: 'disabled', defaultValue: new Date().toISOString().slice(0, 10) }
    ]
  }
});

const inputFormButtons = ref({
  save: { show: true, label: '저장', severity: 'success' }
});



</script>

<template>
    <div>
        <SearchForm :columns="searchColumns" @search="handleSearch" @reset="handleReset" />
    </div>

    <div class="flex flex-col md:flex-row gap-4 mt-6">
        <div class="w-full md:basis-[55%]">
            <StandardTable title="자재기준정보 목록" :data="products" dataKey="mcode" :columns="productColumns" @view-history="handleViewHistory" :scrollable="true" scrollHeight="300px" height="300px" class="mb-2" />
            <InputTable title="자재별 공급처" :columns="cpColumns" :buttons="rowButtons" button-position="top" scrollHeight="205px" height="300px" />
        </div>

        <div class="w-full md:basis-[45%]">
            <InputForm title="자재정보" :columns="inputColumns" :buttons="inputFormButtons" @submit="handleSaveMaterial" />
        </div>
    </div>
</template>
