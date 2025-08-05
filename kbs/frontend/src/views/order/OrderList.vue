<script setup>
import { ref, onMounted, onUnmounted, computed, watch } from 'vue';
import { getOrderList } from '@/api/order';
import InputTable from '@/components/kimbap/table/InputTable.vue';
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import { useMemberStore } from '@/stores/memberStore'
import { useCommonStore } from '@/stores/commonStore'
import { storeToRefs } from 'pinia';
import { useRouter } from 'vue-router';
import { useRoute } from 'vue-router';

// 라우터 설정
const router = useRouter();
const route = useRoute();

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
  const custCodes = [
    ...common.getCodes('0S'),  // 주문상태
    ...common.getCodes('0V')   // 반품상태 (v1, v2)
  ];
  const internalCodes = common.getCodes('0A');

  return list.map(item => {
    let dcd;
    let matched;

    if (user.value?.memType === 'p2') {
      dcd = item.ordStatusCustomer;
      matched = custCodes.find(code => code.dcd === dcd);
    } else {
      dcd = item.ordStatusInternal;
      matched = internalCodes.find(code => code.dcd === dcd);
    }

    return {
      ...item,
      ordStatus: matched ? matched.cdInfo : dcd
    };
  });
};

// 선택된 행
const selectedRows = ref([]);

// 버튼 설정
const infoFormButtons = computed(() => {
  if (user.value?.memType === 'p2') { // 매출업체
    return {
      refund: { show: true, label: '반품관리', severity: 'help' }
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
      { field: 'deliReqDt', header: '납기요청일자', type: 'readonly' },
      { field: 'note', header: '비고', type: 'readonly' },
      { field: 'ordStatus', header: '상태', type: 'readonly' }
    ]
  }
})

// 주문 목록 데이터
const orders = ref([]);

// 주문 목록 조회
onMounted(async () => {
  try {
    await Promise.all([
      common.fetchCommonCodes('0S'),
      common.fetchCommonCodes('0A'),
      common.fetchCommonCodes('0V')
    ]);

    // 조건 분기: p2만 cpCd를 함께 보냄
    const params = {
      id: user.value.id,
      memType: user.value.memType
    };

    if (user.value.memType === 'p2') {
      params.cpCd = user.value.cpCd;
    }

    const res = await getOrderList(params);

    orders.value = ordStatusCodes(res.data.data);
    console.log('실제 응답 내용:', res.data);
  } catch (err) {
    console.error('목록 조회 실패:', err);
  }
});

// 동적 검색 컬럼 설정
const searchColumns = computed(() => {
  if (user.value?.memType === 'p2') {
    // 매출업체용
    return [
      {
        key: 'ordCd',
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
        key: 'ordStatus',
        label: '상태',
        type: 'dropdown',
        options: [
          ...common.getCodes('0S').map(code => ({
            label: code.cdInfo,
            value: code.dcd
          })),
          ...common.getCodes('0V').map(code => ({
            label: code.cdInfo,
            value: code.dcd
          }))
        ]
      }
    ];
  } else {
    // 사원, 관리자용
    return [
      {
        key: 'ordCd',
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
        key: 'cpName',
        label: '거래처',
        type: 'text',
        placeholder: '거래처명을 입력하세요'
      }
    ];
  }
});

// 검색 이벤트 핸들러
const handleSearch = (searchData) => {
  console.log('테이블 컴포넌트에서 받은 검색 데이터:', searchData);

  const params = {
    id: user.value.id,
    memType: user.value.memType,
    ...searchData, // 전달받은 검색 조건 포함
  };

  if (user.value.memType === 'p2') {
    params.cpCd = user.value.cpCd;
  }

  getOrderList(params)
    .then(res => {
      orders.value = ordStatusCodes(res.data.data);
    })
    .catch(err => {
      console.error('검색 실패:', err);
    });
};

// 리셋 이벤트 핸들러
const handleReset = () => {
    console.log('검색 조건이 리셋되었습니다');
    handleSearch({});
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

const handleRefundRequest = () => {
  const selected = selectedRows.value;

  if (!selected || !selected.ordCd) {
    alert('주문을 선택하세요.');
    return;
  }

  if (selected.ordStatus !== '출고완료' && selected.ordStatus !== '부분반품') {
    alert('출고완료, 부분반품 상태인 주문만 반품 요청이 가능합니다.');
    return;
  }

  router.push(`/order/returnRequest/${selected.ordCd}`);
};

watch(selectedRows, (newVal) => {
  console.log('선택된 주문:', newVal)
  if (newVal && newVal.ordStatusCustomer) {
    console.log('원본 상태코드:', newVal.ordStatusCustomer)
  }
  if (newVal && newVal.ordStatus) {
    console.log('화면 표시 상태:', newVal.ordStatus)
  }
});

watch(() => route.query.refresh, (newVal) => {
  if (newVal) {
    console.log('목록 재조회');
    handleSearch({});
  }
});
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
      v-model:selection="selectedRows"
      :dataKey="'ordCd'"
      :columns="orderColumns"
      :enableRowActions="false"
      :enableSelection="true"
      :scroll-height="'55vh'" :height="'65vh'"
      :selectionMode="'single'"
      :showRowCount="true"
      :buttons="infoFormButtons"
      :dateFields="['ordDt', 'deliReqDt']"
      :enableRowClick="true"
      @rowClick="handleRowClick"
      @refund="handleRefundRequest"
    />
  </div>
</template>