<script setup>
import { useLayout } from '@/layout/composables/layout';
import { onMounted, ref, watch } from 'vue';
import StandartTable from '@/components/kimbap/table/StandardTable.vue'

const { getPrimary, getSurface, isDarkTheme } = useLayout();
const pieData = ref(null);
const barData = ref(null);
const pieOptions = ref(null);
const barOptions = ref(null);

onMounted(() => {
    setColorOptions();
});

function setColorOptions() {
    const documentStyle = getComputedStyle(document.documentElement);
    const textColor = documentStyle.getPropertyValue('--text-color');
    const textColorSecondary = documentStyle.getPropertyValue('--text-color-secondary');
    const surfaceBorder = documentStyle.getPropertyValue('--surface-border');

    barData.value = {
        labels: ['1월', '2월', '3월', '4월', '5월', '6월', '7월', '8월', '9월', '10월', '11월', '12월'],
        datasets: [
            {
                label: '매출현황',
                backgroundColor: documentStyle.getPropertyValue('--p-primary-500'),
                borderColor: documentStyle.getPropertyValue('--p-primary-500'),
                data: [6, 59, 80, 81, 56, 55, 40]
            },
        ]
    };
    barOptions.value = {
        plugins: {
            legend: {
                labels: {
                    fontColor: textColor
                }
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

    pieData.value = {
        labels: ['A', 'B', 'C'],
        datasets: [
            {
                data: [540, 325, 702],
                backgroundColor: [documentStyle.getPropertyValue('--p-indigo-500'), documentStyle.getPropertyValue('--p-purple-500'), documentStyle.getPropertyValue('--p-teal-500')],
                hoverBackgroundColor: [documentStyle.getPropertyValue('--p-indigo-400'), documentStyle.getPropertyValue('--p-purple-400'), documentStyle.getPropertyValue('--p-teal-400')]
            }
        ]
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
    { field: '', header: '순번' },
    { field: '', header: '거래처명' },
    { field: '', header: '주문수량' },
    { field: '', header: '주문일자' },
    { field: '', header: '납기가능일자' },
    { field: '', header: '주문상세상태' }
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
                        <div class="text-surface-900 dark:text-surface-0 font-medium text-xl">152건</div>
                    </div>
                </div><span class="text-primary font-medium">24 new </span>
                <span class="text-muted-color">since last visit</span>
            </div>
        </div>
        <div>
            <div class="card mb-0">
                <div class="flex justify-between mb-4">
                    <div><span class="block text-muted-color font-medium mb-4">출고대기</span>
                        <div class="text-surface-900 dark:text-surface-0 font-medium text-xl">152건</div>
                    </div>
                </div><span class="text-primary font-medium">24 new </span>
                <span class="text-muted-color">since last visit</span>
            </div>
        </div>
        <div>
            <div class="card mb-0">
                <div class="flex justify-between mb-4">
                    <div><span class="block text-muted-color font-medium mb-4">출고완료</span>
                        <div class="text-surface-900 dark:text-surface-0 font-medium text-xl">152건</div>
                    </div>
                </div><span class="text-primary font-medium">24 new </span>
                <span class="text-muted-color">since last visit</span>
            </div>
        </div>
        <div>
            <div class="card mb-0">
                <div class="flex justify-between mb-4">
                    <div><span class="block text-muted-color font-medium mb-4">반품접수</span>
                        <div class="text-surface-900 dark:text-surface-0 font-medium text-xl">152건</div>
                    </div>
                </div><span class="text-primary font-medium">24 new </span>
                <span class="text-muted-color">since last visit</span>
            </div>
        </div>
    </div>


    <div class="flex flex-col md:flex-row gap-4 mt-6">
        <div class="w-full md:basis-[50%]">
            <div class="col-span-12 xl:col-span-6 mb-3">
                <div class="card flex flex-col items-center min-h-[430px]">
                    <div class="font-semibold text-xl mb-4">8월 판매현황</div>
                    <Chart type="pie" :data="pieData" :options="pieOptions"></Chart>
                </div>
            </div>
        </div>
        <div class="w-full md:basis-[50%]">
        <div class="col-span-12 xl:col-span-6 mb-3">
            <div class="card flex flex-col items-center min-h-[430px]">
                <div class="font-semibold text-xl mb-4">8월 매출현황</div>
                <Chart type="bar" :data="barData" :options="barOptions" style="width: 100%; height: 250px"></Chart>
            </div>
        </div>
        </div>
    </div>



    <div class="w-full">
        <StandartTable :title="'금일 주문목록'" :data="condProdPlanList" :columns="prodPlanColumns"
            dataKey="produPlanCd" scrollHeight="55vh" :selectable="false" :showHistoryButton="false"
            :hoverable="true" @row-click="row => openDetailModal(row.produPlanCd)" />
    </div>
</template>
