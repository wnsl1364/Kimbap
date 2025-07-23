<script setup>
import { ref, onMounted } from 'vue';
import { getOrderList } from '@/api/order';
import StandardTable from '@/components/kimbap/table/StandardTable.vue';

// 주문 목록 컬럼 정의
const orderColumns = [
    { field: 'ordCd', header: '주문번호' },
    { field: 'cpCd', header: '거래처코드' },
    { field: 'ordDt', header: '주문일자' },
    { field: 'regi', header: '등록자' },
    { field: 'regDt', header: '등록일자' }
];

// 주문 목록 데이터
const orders = ref([]);

onMounted(async () => {
  try {
    const res = await getOrderList();
    console.log('응답 타입:', typeof res.data);
    console.log('실제 응답 내용:', res.data);
    orders.value = res.data;
  } catch (err) {
    console.error('목록 조회 실패:', err);
  }
});
</script>
<template>
  <StandardTable :data="orders" dataKey="ordCd" :columns="orderColumns" />
</template>