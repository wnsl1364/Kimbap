<script setup>
import { ref } from 'vue';

import AppMenuItem from './AppMenuItem.vue';

const model = ref([
    {
        label: 'Home',
        items: [
            { label: 'Dashboard', icon: 'pi pi-fw pi-home', to: '/' },
        ]
    },
    {
        label: '기준정보',
        items: [
            {
                label: '자재 관리',
                to: '/standard/material'
            },
            {
                label: '제품 관리',
                to: '/standard/product'
            },
            {
                label: '거래처 관리',
                to: '/standard/company'
            },
            {
                label: '공장 관리',
                to: '/standard/factory'
            },
            {
                label: '창고 관리',
                to: '/standard/warehouse'
            },
        ]
    },
    {
        label: '주문',
        items: [
            {
                label: '주문등록',
                to: '/order/orderRegister'
            },
            {
                label: '주문검토',
                to: '/order/orderReview'
            },
            {
                label: '주문목록',
                to: '/order/orderList'
            },
            {
                label: '거래처원장',
                to: '/order/orderLedger'
            },
            {
                label: '반품관리',
                to: '/order/returnManage'
            },
        ]
    },
    {
        label: '자재',
        items: [
            {
                label: '자재 재고 조회',
                to: '/material/materialStockView'
            },
            {
                label: '자재 입고',
                to: '/material/MaterialPurchaseView?from=inbound'
            },
            {
                label: '자재 적재 대기 목록',
                to: '/material/mateLoading'
            },
            {
                label: '자재 이동 요청 등록',
                to: '/material/stockMovementRegister'
            },
            {
                label: '자재 이동 요청 목록',
                to: '/material/stockMovementList'
            },
            {
                label: '자재 입출고 내역',
                to: '/material/materialInOutHistory'
            },
            {
                label: '자재 발주',
                to: '/material/materialPurchase'
            },
            {
                label: '자재 발주 조회',
                to: '/material/materialPurchaseView'
            },
            {
                label: '자재 발주 승인',
                to: '/material/MaterialPurchaseApproval'
            },
            {
                label: '자재 출고',
                to: '/material/materialOutbound'
            },
            {
                label: '창고 테스트',
                to: '/material/testStore'
            }
        ]
    },
    {
        label: '생산',
        items: [
            {
                label: '생산 계획 등록',
                to: '/production/productionPlan'
            },
            {
                label: '생산 계획 조회',
                to: '/production/productionPlanList'
            },
            {
                label: '생산요청서 등록',
                to: '/production/productionRequest'
            },
            {
                label: '생산 요청 조회',
                to: '/production/productionRequestList'
            },
            {
                label: '제품 적재 대기',
                to: '/production/productInbound'
            },
        ]
    },
    {
        label: '물류',
        items: [
            // {
            //     label: '자재 재고 조회',
            //     to: '/material/materialStockView'
            // },
            {
                label: '출고지시서 조회',
                to: '/distribution/relOrdList'
            },
            {
                label: '출고 지시서 등록',
                to: '/distribution/relOrdAndResult'
            },
            {
                label: '출고 처리',
                to: '/distribution/relSave'
            },
            {
                label: '완제품 입출고 조회',
                to: '/distribution/distributionCheck'
            },
        ]
    },
    {
        label: '결제',
        items: [
            {
                label: '입출금 관리',
                to: '/payment/cashflow'
            },
            {
                label: '미수금 정산',
                to: '/payment/unpaidallocation'
            },
        ]
    },
    {
        label: 'Hierarchy',
        items: [
            {
                label: 'Submenu 1',
                icon: 'pi pi-fw pi-bookmark',
                items: [
                    {
                        label: 'Submenu 1.1',
                        icon: 'pi pi-fw pi-bookmark',
                        items: [
                            { label: 'Submenu 1.1.1', icon: 'pi pi-fw pi-bookmark' },
                            { label: 'Submenu 1.1.2', icon: 'pi pi-fw pi-bookmark' },
                            { label: 'Submenu 1.1.3', icon: 'pi pi-fw pi-bookmark' }
                        ]
                    },
                    {
                        label: 'Submenu 1.2',
                        icon: 'pi pi-fw pi-bookmark',
                        items: [{ label: 'Submenu 1.2.1', icon: 'pi pi-fw pi-bookmark' }]
                    }
                ]
            },
            {
                label: 'Submenu 2',
                icon: 'pi pi-fw pi-bookmark',
                items: [
                    {
                        label: 'Submenu 2.1',
                        icon: 'pi pi-fw pi-bookmark',
                        items: [
                            { label: 'Submenu 2.1.1', icon: 'pi pi-fw pi-bookmark' },
                            { label: 'Submenu 2.1.2', icon: 'pi pi-fw pi-bookmark' }
                        ]
                    },
                    {
                        label: 'Submenu 2.2',
                        icon: 'pi pi-fw pi-bookmark',
                        items: [{ label: 'Submenu 2.2.1', icon: 'pi pi-fw pi-bookmark' }]
                    }
                ]
            }
        ]
    },
]);
</script>

<template>
    <ul class="layout-menu">
        <template v-for="(item, i) in model" :key="item">
            <app-menu-item v-if="!item.separator" :item="item" :index="i"></app-menu-item>
            <li v-if="item.separator" class="menu-separator"></li>
        </template>
    </ul>
</template>

<style lang="scss" scoped></style>
