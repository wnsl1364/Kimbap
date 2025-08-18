<script setup>
import { useLayout } from '@/layout/composables/layout';
import { onMounted, ref, watch, computed } from 'vue';
import StandartTable from '@/components/kimbap/table/StandardTable.vue'
import { dashboardTopData, dashboardPieData, dashboardBarData, dashboardOrderData } from '@/api/dashboard';
import { useCommonStore } from '@/stores/commonStore'
import { storeToRefs } from 'pinia';
const { getPrimary, getSurface, isDarkTheme } = useLayout();

// 🔥 공통코드 store 추가
const commonStore = useCommonStore();

// 월 변환
const currentMonthLabel = ref('');
const now = new Date();
currentMonthLabel.value = `${now.getMonth() + 1}월`;

// 파이차트 데이터
const pieData = ref(null);
const pieOptions = ref(null);

// 바 차트 데이터
const barData = ref(null);
const barOptions = ref(null);

// 대쉬보드 상단 데이터
const prodInboCount = ref(0);     // 생산완료
const prodReturnCount = ref(0);   // 반품접수
const releaseOrdCount = ref(0);   // 출고대기
const prodRelCount = ref(0);      // 출고완료

// 🔥 원본 데이터와 변환된 데이터 분리
const rawOrderData = ref([]);

// 🔥 공통코드 형변환 함수
const convertOrderStatusCodes = (list) => {
  const statusCodes = commonStore.getCodes('0S'); // 주문상세상태 코드

  return list.map((item, index) => {
    const matchedStatus = statusCodes.find(code => code.dcd === item.ordDStatus);

    return {
      ...item,
      index: index + 1,
      ordDStatus: matchedStatus ? matchedStatus.cdInfo : item.ordDStatus,
    };
  });
};

// 🔥 변환된 주문 데이터 computed
const condProdPlanList = computed(() => {
  const dataArray = Array.isArray(rawOrderData.value) ? rawOrderData.value : [];
  return convertOrderStatusCodes(dataArray);
});

onMounted(async () => {
    setColorOptions();
    
    // 🔥 공통코드 로드
    await commonStore.fetchCommonCodes('0S'); // 주문상세상태 코드
    
    await fetchDashboardCounts(); // 상단 데이터
    await fetchDashboardPieData(); // 파이차트 데이터
    await fetchDashboardBarData(); // 바 차트 데이터
    await fetchDashboardOrderData(); // 금일 요청주문 데이터
});

// 대시보드 상단 함수
async function fetchDashboardCounts() {
    try {
        const res = await dashboardTopData();
        console.log('dashboardTopData 응답:', res.data);

        const data = res.data;

        prodInboCount.value = data.prodInbo ?? 0;
        prodReturnCount.value = data.prodReturn ?? 0;
        releaseOrdCount.value = data.releaseOrd ?? 0;
        prodRelCount.value = data.prodRel ?? 0;
    } catch (err) {
        console.error('대시보드 건수 조회 실패:', err);
    }
}

// 파이차트 함수
async function fetchDashboardPieData() {
    try {
        const res = await dashboardPieData();
        console.log('dashboardPieData 응답:', res.data);

        const raw = res.data;

        // 값이 없을 경우 초기화
        if (!Array.isArray(raw) || raw.length === 0) {
            pieData.value = {
                labels: [],
                datasets: [{
                    data: [],
                    backgroundColor: [],
                    hoverBackgroundColor: []
                }]
            };
            return;
        }

        // 데이터 변환
        const productNameMap = {
            'PROD-1001': '아삭아삭야채김밥',
            'PROD-1002': '듬뿍꾸덕참치마요김밥',
            'PROD-1003': '탱글소세지김밥',
            'PROD-1007': '매콤참치볶음김치김밥',
            'PROD-1008': '진미채김밥',
            'PROD-1009': '치즈돈까스김밥',
            'PROD-1010': '연어아보카도김밥'
        };
        const labels = raw.map(item => productNameMap[item.prodName] || item.prodName);
        const data = raw.map(item => item.pieTotalQty);

        const documentStyle = getComputedStyle(document.documentElement);
        const baseColors = [
            documentStyle.getPropertyValue('--p-indigo-500'),
            documentStyle.getPropertyValue('--p-purple-500'),
            documentStyle.getPropertyValue('--p-teal-500'),
            documentStyle.getPropertyValue('--p-orange-500'),
            documentStyle.getPropertyValue('--p-cyan-500'),
            documentStyle.getPropertyValue('--p-pink-500'),
        ];
        const hoverColors = [
            documentStyle.getPropertyValue('--p-indigo-400'),
            documentStyle.getPropertyValue('--p-purple-400'),
            documentStyle.getPropertyValue('--p-teal-400'),
            documentStyle.getPropertyValue('--p-orange-400'),
            documentStyle.getPropertyValue('--p-cyan-400'),
            documentStyle.getPropertyValue('--p-pink-400'),
        ];

        pieData.value = {
            labels,
            datasets: [{
                data,
                backgroundColor: baseColors.slice(0, data.length),
                hoverBackgroundColor: hoverColors.slice(0, data.length)
            }]
        };

    } catch (err) {
        console.error('파이차트 데이터 조회 실패:', err);
    }
}

// 바 차트 데이터
async function fetchDashboardBarData() {
    try {
        const res = await dashboardBarData();
        console.log('dashboardBarData 응답:', res.data);

        const raw = res.data;

        const parsed = raw.map(item => {
            const monthNum = parseInt(item.month?.split('-')[1]); // '2025-07' → 7
            return {
                ...item,
                month: `${monthNum}월`
            };
        });

        const monthLabels = Array.from({ length: 12 }, (_, i) => `${i + 1}월`);
        const salesMap = new Map(parsed.map(item => [item.month, item.totalSales]));
        const data = monthLabels.map(month => salesMap.get(month) ?? 0);

        const documentStyle = getComputedStyle(document.documentElement);
        const barColor = documentStyle.getPropertyValue('--p-primary-500');

        barData.value = {
            labels: monthLabels,
            datasets: [{
                label: '매출현황',
                backgroundColor: barColor,
                borderColor: barColor,
                data
            }]
        };
    } catch (err) {
        console.error('바차트 데이터 조회 실패:', err);
    }
}

// 🔥 수정된 금일 요청주문 데이터 함수
async function fetchDashboardOrderData() {
    try {
        const res = await dashboardOrderData();
        console.log('dashboardOrderData 응답:', res.data);

        // 🔥 원본 데이터만 저장 (변환은 computed에서 처리)
        rawOrderData.value = Array.isArray(res.data) ? res.data : [];
        
    } catch (err) {
        console.error('금일 요청주문 데이터 조회 실패:', err);
        rawOrderData.value = [];
    }
}

function setColorOptions() {
    const documentStyle = getComputedStyle(document.documentElement);
    const textColor = documentStyle.getPropertyValue('--text-color');
    const textColorSecondary = documentStyle.getPropertyValue('--text-color-secondary');
    const surfaceBorder = documentStyle.getPropertyValue('--surface-border');

    barOptions.value = {
        plugins: {
            legend: {
                display: false
            }
        },
        scales: {
            x: {
                ticks: {
                    color: textColorSecondary,
                    font: {
                        weight: 500
                    }
                },
                grid: {
                    display: false,
                    drawBorder: false
                }
            },
            y: {
                ticks: {
                    color: textColorSecondary
                },
                grid: {
                    color: surfaceBorder,
                    drawBorder: false
                }
            }
        }
    };

    pieOptions.value = {
        plugins: {
            legend: {
                labels: {
                    usePointStyle: true,
                    color: textColor
                }
            }
        }
    };
}

const prodPlanColumns = [
    { field: 'index', header: '순번' },
    { field: 'cpName', header: '거래처명' },
    { field: 'ordTotalQty', header: '주문수량' },
    { field: 'ordDt', header: '주문일자' },
    { field: 'deliAvailDt', header: '납기가능일자' },
    { field: 'ordDStatus', header: '주문상세상태' }
]

watch(
    [getPrimary, getSurface, isDarkTheme],
    () => {
        setColorOptions();
    },
    { immediate: true }
);
</script>

<template>
    <div class="grid grid-cols-4 gap-4">
        <div>
            <div class="card mb-0">
                <div class="flex justify-between mb-4">
                    <div><span class="block text-muted-color font-medium mb-4">생산완료</span>
                        <div class="text-surface-900 dark:text-surface-0 font-medium text-xl">{{ prodInboCount }}건</div>
                    </div>
                </div>
            </div>
        </div>
        <div>
            <div class="card mb-0">
                <div class="flex justify-between mb-4">
                    <div><span class="block text-muted-color font-medium mb-4">출고대기</span>
                        <div class="text-surface-900 dark:text-surface-0 font-medium text-xl">{{ prodRelCount }}건
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div>
            <div class="card mb-0">
                <div class="flex justify-between mb-4">
                    <div><span class="block text-muted-color font-medium mb-4">출고완료</span>
                        <div class="text-surface-900 dark:text-surface-0 font-medium text-xl">{{ releaseOrdCount }}건
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div>
            <div class="card mb-0">
                <div class="flex justify-between mb-4">
                    <div><span class="block text-muted-color font-medium mb-4">반품접수</span>
                        <div class="text-surface-900 dark:text-surface-0 font-medium text-xl">{{ prodReturnCount }}건</div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="flex flex-col md:flex-row gap-4 mt-6">
        <div class="w-full md:basis-[50%]">
            <div class="col-span-12 xl:col-span-6 mb-3">
                <div class="card flex flex-col items-center min-h-[430px]">
                    <div class="font-semibold text-xl mb-4">{{ currentMonthLabel }} 판매현황</div>
                    <Chart type="pie" :data="pieData" :options="pieOptions"></Chart>
                </div>
            </div>
        </div>
        <div class="w-full md:basis-[50%]">
            <div class="col-span-12 xl:col-span-6 mb-3">
                <div class="card flex flex-col items-center min-h-[430px]">
                    <div class="font-semibold text-xl mb-4">{{ currentMonthLabel }} 매출현황</div>
                    <Chart type="bar" :data="barData" :options="barOptions" style="width: 100%; height: 250px"></Chart>
                </div>
            </div>
        </div>
    </div>
    <div class="w-full">
        <!-- 🔥 변환된 데이터 사용 -->
        <StandartTable :title="'금일 요청주문'" :data="condProdPlanList" :columns="prodPlanColumns" dataKey="index"
            scrollHeight="45vh" :selectable="false" :showHistoryButton="false" />
    </div>
</template>