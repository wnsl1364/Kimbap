<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { getOrderList } from '@/api/order';
import InputTable from '@/components/kimbap/table/InputTable.vue';
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import { useMemberStore } from '@/stores/memberStore'
import { useCommonStore } from '@/stores/commonStore'
import { storeToRefs } from 'pinia';
import { useRouter } from 'vue-router';

// 라우터 설정
const router = useRouter();

// 로그인 정보 가져오기
const memberStore = useMemberStore()
const { user } = storeToRefs(memberStore)

const isEmployee = computed(() => user.value?.memType === 'p1')       // 사원
const isCustomer = computed(() => user.value?.memType === 'p2')       // 매출업체
const isSupplier = computed(() => user.value?.memType === 'p3')       // 공급업체
const isManager = computed(() => user.value?.memType === 'p4')        // 담당자
const isAdmin = computed(() => user.value?.memType === 'p5')          // 시스템 관리자

console.log('현재 사용자 권한:', user.value)

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

// 버튼 설정
const infoFormButtons = computed(() => {
  if (user.value?.memType === 'p2') { // 매출업체
    return {
      refund: { show: true, label: '반품요청', severity: 'help' }
    }
  } else {
    return {} // 버튼 없음
  }
})

// 주문 목록 컬럼 정의
const orderColumns = computed(() => {
  if (user.value?.memType === 'p2') { // 매출업체
    return [
      { field: 'ordCd', header: '주문코드', type: 'clickable' },
      { field: 'prodName', header: '제품명', type: 'readonly' },
      { field: 'totalQty', header: '주문수량(BOX)', type: 'readonly' },
      { field: 'returnQty', header: '반품수량(BOX)', type: 'readonly' },
      { field: 'totalAmount', header: '총금액(원)', type: 'readonly' },
      { field: 'ordDt', header: '주문일자', type: 'readonly' },
      { field: 'deliReqDt', header: '납기일자', type: 'readonly' },
      { field: 'note', header: '비고', type: 'readonly' },
      { field: 'ordStatus', header: '상태', type: 'readonly' }
    ]
  } else {
    // 사원 또는 관리자
    return [
      { field: 'ordCd', header: '주문코드', type: 'clickable' },
      { field: 'prodName', header: '제품명', type: 'readonly' },
      { field: 'cpName', header: '거래처명', type: 'readonly' },
      { field: 'totalAmount', header: '총금액(원)', type: 'readonly' },
      { field: 'ordDt', header: '주문일자', type: 'readonly' },
      { field: 'deliReqDt', header: '납기일자', type: 'readonly' },
      { field: 'note', header: '비고', type: 'readonly' }
    ]
  }
})

// 주문 목록 데이터
const orders = ref([]);

// 주문 목록 조회
onMounted(async () => {
  try {
    const res = await getOrderList({
      id: user.value.id,
      memType: user.value.memType
    });
    await common.fetchCommonCodes('0S');
    orders.value = ordStatusCodes(res.data.data);
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
      key: 'ordDt',
      label: '주문일자',
      type: 'dateRange'
    },
    {
      key: 'deliReqDt',
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

const handleRowClick = (rowData) => {
  console.log('[OrderList.vue] 라우터 이동 대상:', rowData)
  const ordCd = rowData.ordCd
  const memType = user.value?.memType

  if (!ordCd) return;

  if (memType === 'p2') {
    // 매출업체는 주문등록(수정) 페이지로
    router.push({ path: '/order/orderRegister', query: { ordCd } })
  } else if (['p1', 'p4', 'p5'].includes(memType)) {
    // 사원/관리자/물류는 주문검토 페이지로
    router.push({ path: '/order/orderReview', query: { ordCd } })
  } else {
    console.warn('지원되지 않는 사용자 유형입니다:', memType)
  }
}
</script>
<template>
  <!-- 검색 폼 컴포넌트 -->
  <div class="space-y-4">
    <SearchForm :columns="searchColumns" @search="handleSearch" @reset="handleReset" />
  </div>
  <!-- 주문 목록 테이블 -->
  <div class="space-y-4 mt-8">
    <InputTable
      v-model:data="orders"
      :dataKey="'ordCd'"
      :columns="orderColumns"
      :enableRowActions="false"
      :enableSelection="true"
      scrollHeight="400px"
      :selectionMode="'single'"
      :showRowCount="true"
      :buttons="infoFormButtons"
      :dateFields="['ordDt', 'deliReqDt']"
      :enableRowClick="true"
       @rowClick="handleRowClick"
    />
  </div>
</template>