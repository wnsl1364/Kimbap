나의 말:
<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { getOrderList } from '@/api/order';
import BasicTable from '@/components/kimbap/table/BasicTable.vue';
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import { useCommonStore } from '@/stores/commonStore'
import { storeToRefs } from 'pinia';

// 공통코드 가져오기
const common = useCommonStore()
const { commonCodes } = storeToRefs(common)

// 공통코드 형변환
const ordStatusCodes = (list) => {
  const unitCodes = common.getCodes('0S');

  return list.map(item => {
    const matched = unitCodes.find(code => code.dcd === item.ordStatus);
    return {
      ...item,
      ordStatus: matched ? matched.cdInfo : item.ordStatus
    };
  });
};

// 주문 목록 컬럼 정의
const orderColumns = [
    { field: 'ordCd', header: '주문코드' },
    { field: 'pName', header: '제품명' },
    { field: 'totalQty', header: '주문수량(BOX)' },
    { field: 'returnQty', header: '반품수량(BOX)' },
    { field: 'totalAmount', header: '총금액(원)' },
    { field: 'ordDt', header: '주문일자' },
    { field: 'dueDate', header: '납기일자' },
    { field: 'note', header: '비고' },    
    { field: 'ordStatus', header: '상태' }
];

// 주문 목록 데이터
const orders = ref([]);

// 주문 목록 조회
onMounted(async () => {
  try {
    const res = await getOrderList();
    await common.fetchCommonCodes('0S');
    orders.value = ordStatusCodes(res.data.data);
    console.log('응답 타입:', typeof res.data);
    console.log('실제 응답 내용:', res.data);
  } catch (err) {
    console.error('목록 조회 실패:', err);
  }
});

// 동적 검색 컬럼 설정
const searchColumns = ref([
    {
        key: 'location',
        label: '주문코드',
        type: 'text',
        placeholder: '주문코드를 입력하세요'
    },
    {
        key: 'materialName',
        label: '주문일자',
        type: 'dateRange'
    },    
    {
        key: 'materialName',
        label: '납기일자',
        type: 'dateRange'
    },
    {
        key: 'status',
        label: '상태',
        type: 'dropdown',
        options: [
            { label: '반품취소', value: 'cancel' },
            { label: '주문완료', value: 'ordComplete' },
            { label: '배송완료', value: 'delComplete' }
        ]
    }
]);

// 검색 이벤트 핸들러
const handleSearch = (searchData) => {
    console.log('테이블 컴포넌트에서 받은 검색 데이터:', searchData);
    // 여기에 검색 로직 구현
    props.searchColumns.forEach(column => {
        column.value = searchData[column.key] || '';
    });
};

// 리셋 이벤트 핸들러
const handleReset = () => {
    console.log('검색 조건이 리셋되었습니다');
    // 여기에 리셋 로직 구현
    props.searchColumns.forEach(column => {
        column.value = '';
    });
};
</script>
<template>
  <!-- 검색 폼 컴포넌트 -->
  <div class="space-y-4">
    <SearchForm :columns="searchColumns" @search="handleSearch" @reset="handleReset" />
  </div>
  <!-- 주문 목록 테이블 -->
  <div class="space-y-4 mt-8">
    <BasicTable :data="orders" dataKey="ordCd" :columns="orderColumns" />
  </div>
</template>