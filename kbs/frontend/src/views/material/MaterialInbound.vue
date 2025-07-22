<script setup>
import { ref } from 'vue'
import LeftAlignTable from '@/components/kimbap/table/LeftAlignTable.vue'
import BasicTable from '@/components/kimbap/table/BasicTable.vue'

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
    { label: '입고창고', field: 'wcode', type: 'text' }
])

const material = ref([
    {
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

</script>

<template>
    <div class="flex justify-end mb-2">
        <button class="p-button p-component" type="button" aria-label="Primary" data-pc-name="button" data-p-disabled="false" pc68="" data-pc-section="root"><!----><span class="p-button-label" data-pc-section="label">입고처리</span><!----></button>
    </div>
    <div class="space-y-4 mb-2" >
        <LeftAlignTable :data="formData" :fields="formFields" />
    </div>
    <div>
        <BasicTable :data="material" :columns="Columns" />
    </div>
</template>