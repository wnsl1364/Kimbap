<script setup>
import { ref } from 'vue'
import LeftAlignTable from '@/components/kimbap/table/LeftAlignTable.vue'
import BasicTable from '@/components/kimbap/table/BasicTable.vue'
import Singleselect from '@/components/kimbap/modal/singleselect.vue'

const formData = ref({
    purcCd: '자동생성',
    status: '입고대기',
    ordDt: '주문일자', 
    inboDt: '당일 일자',
    mname: '로그인된 사용자',
    wcode: ''
})

const formFields = ref([
    { label: '입고번호', field: 'purcCd', type: 'text', readonly: true },
    { label: '상태', field: 'status', type: 'text', readonly: true },
    { label: '주문일자', field: 'ordDt', type: 'date', readonly: true },
    { label: '입고일자', field: 'inboDt', type: 'calendar', readonly: false },
    { label: '등록자', field: 'mname', type: 'text', readonly: true },
    { label: '입고창고', field: 'wcode', type: 'input', readonly: true, suffixIcon: 'pi pi-search', suffixEvent: 'openWarehouseModal'}
])

const material = ref([
    {   id: 1,
        mateName: '김밥용 김',
        cpName: '광동식자재',
        purcQty: 1000,
        unit: '장',
        total_qty: 980,
        ex_deli_dt: '2025-07-24',
        deli_dt: '2025-07-22',
        note: '부분 납품됨'
    },
    {   
        id: 2,
        mateName: '햄',
        cpName: '한성푸드',
        purcQty: 500,
        unit: '개',
        total_qty: 500,
        ex_deli_dt: '2025-07-24',
        deli_dt: '2025-07-22',
        note: ''
    },
    {   
        id: 3,
        mateName: '단무지',
        cpName: '맛있는식자재',
        purcQty: 300,
        unit: 'kg',
        total_qty: 300,
        ex_deli_dt: '2025-07-24',
        deli_dt: '2025-07-22',
        note: '정상 입고'
    }
])

const Columns = ref([
    { field: 'mateName', header: '자재명'},
    { field: 'cpName', header: '거래처명' },
    { field: 'purcQty', header: '입고요청수량' },
    { field: 'unit', header: '단위' },
    { field: 'total_qty', header: '입고수량' },
    { field: 'ex_deli_dt', header: '납기예정일'},
    { field: 'deli_dt', header: '납기일'},
    { field: 'note', header: '비고' }
])

const wcolumns = ref([
    { field: '', header: '' },
    { field: '', header: '' },
    { field: '', header: '' },
    { field: '', header: '' }
])

const warehouseList = ref([
    { name: 'A', category: 'A', type: '냉동', quantity: '김냉동' },
    { name: 'A', category: 'B', type: '냉장', quantity: '이냉장' },
    { name: 'A', category: 'C', type: '실온', quantity: '박실온' }, 
    { name: 'B', category: 'A', type: '냉동', quantity: '최냉동' },
    { name: 'B', category: 'B', type: '냉장', quantity: '곽냉장' },
    { name: 'B', category: 'C', type: '실온', quantity: '이실온' },    
])

const isWarehouseModalOpen = ref(false)
const selectedWarehouse = ref(null)

const openWarehouseModal = () => {
  isWarehouseModalOpen.value = true
}

const onConfirmWarehouse = (value) => {
  if (value){
    selectedWarehouse.value = value;
    formData.value.wcode = value.name;
  } else {
    selectedWarehouse.value = null;
    formData.value.wcode = ''; 
  }
  isWarehouseModalOpen.value = false;
}

</script>

<template>
    <div class="flex justify-end mb-2">
        <button class="p-button p-component" type="button" aria-label="Primary" data-pc-name="button" data-p-disabled="false" pc68="" data-pc-section="root"><!----><span class="p-button-label" data-pc-section="label">입고처리</span><!----></button>
    </div>
    <div class="space-y-4 mb-2" >
        <LeftAlignTable :data="formData" :fields="formFields" @openWarehouseModal="openWarehouseModal"/>
    </div>
    <div>
        <BasicTable :data="material" :columns="Columns" />
    </div>

    <Singleselect
        v-model:visible="isWarehouseModalOpen"
        v-model:modelValue="selectedWarehouse"
        :items="warehouseList"
        itemKey="code"
        :columns="wcolumns"
        @update:modelValue="onConfirmWarehouse"
    />
</template>