<script setup>
import { ref } from 'vue';

import AppMenuItem from './AppMenuItem.vue';

const model = ref([
    {
        label: 'Home',
        items: [
            { label: 'Dashboard', icon: 'pi pi-fw pi-home', to: '/' },
            // { label: '사원계정 등록(관리자만 보이게할예정)', to: '/login/MemberAdd' },
            { label: '거래처계정 등록(관리자만 보이게할예정)', to: '/login/cpMemberAdd' }
        ]
    },
    
    {
        label: '기준정보',
        items: [
            {
                label: '자재 기준정보',
                to: '/standard/material'
            },
            {
                label: '제품 기준정보',
                to: '/standard/product'
            },
            {
                label: '거래처 기준정보',
                to: '/standard/company'
            },
            {
                label: '공장 기준정보',
                to: '/standard/factory'
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
                label: '거래내역',
                to: '/order/transactionDetails'
            },
            {
                label: '거래처원장',
                to: '/order/orderLedger'
            },
            {
                label: '반품관리',
                to: '/order/orderRefund'
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
                to: '/material/materialInbound'
            },
            {
                label: '자재 적재 대기 목록',
                to: '/material/materialLoadingWaitingList'
            },
            {
                label: '자재 이동 요청 등록',
                to: '/material/transferRequestForm'
            },
            {
                label: '자재 이동 요청 목록',
                to: '/material/transferRequestList'
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
                to: '/material/materialPurchaseConfirm'
            },
            {
                label: '자재 출고',
                to: '/material/materialOutbound'
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
            {
                label: '자재 재고 조회',
                to: '/material/materialStockView'
            },
        ]
    },
    {
        label: '결제',
        items: [
            {
                label: '자재 재고 조회',
                to: '/material/materialStockView'
            },
        ]
    },
    {
        label: 'UI Components',
        items: [
            { label: 'Form Layout', icon: 'pi pi-fw pi-id-card', to: '/uikit/formlayout' },
            { label: '싱글체크테이블', to: '/BasicTableCheckOneParent' },
            { label: '더블체크테이블', to: '/BasicTableParent' },
            { label: '등록테이블', to: '/InputTableParent' },
            { label: '가로?테이블?', to: '/LeftAlignTableParent' },
            { label: 'Input', icon: 'pi pi-fw pi-check-square', to: '/uikit/input' },
            { label: 'Button', icon: 'pi pi-fw pi-mobile', to: '/uikit/button', class: 'rotated-icon' },
            { label: 'Table', icon: 'pi pi-fw pi-table', to: '/uikit/table' },
            { label: 'List', icon: 'pi pi-fw pi-list', to: '/uikit/list' },
            { label: 'Tree', icon: 'pi pi-fw pi-share-alt', to: '/uikit/tree' },
            { label: 'Panel', icon: 'pi pi-fw pi-tablet', to: '/uikit/panel' },
            { label: 'Overlay', icon: 'pi pi-fw pi-clone', to: '/uikit/overlay' },
            { label: 'Media', icon: 'pi pi-fw pi-image', to: '/uikit/media' },
            { label: 'Menu', icon: 'pi pi-fw pi-bars', to: '/uikit/menu' },
            { label: 'Message', icon: 'pi pi-fw pi-comment', to: '/uikit/message' },
            { label: 'File', icon: 'pi pi-fw pi-file', to: '/uikit/file' },
            { label: 'Chart', icon: 'pi pi-fw pi-chart-bar', to: '/uikit/charts' },
            { label: 'Timeline', icon: 'pi pi-fw pi-calendar', to: '/uikit/timeline' },
            { label: 'Misc', icon: 'pi pi-fw pi-circle', to: '/uikit/misc' }
        ]
    },
    {
        label: 'Pages',
        icon: 'pi pi-fw pi-briefcase',
        to: '/pages',
        items: [
            {
                label: 'Landing',
                icon: 'pi pi-fw pi-globe',
                to: '/landing'
            },
            {
                label: 'Auth',
                icon: 'pi pi-fw pi-user',
                items: [
                    {
                        label: 'Login',
                        icon: 'pi pi-fw pi-sign-in',
                        to: '/auth/login'
                    },
                    {
                        label: 'Error',
                        icon: 'pi pi-fw pi-times-circle',
                        to: '/auth/error'
                    },
                    {
                        label: 'Access Denied',
                        icon: 'pi pi-fw pi-lock',
                        to: '/auth/access'
                    }
                ]
            },
            {
                label: 'Crud',
                icon: 'pi pi-fw pi-pencil',
                to: '/pages/crud'
            },
            {
                label: 'Not Found',
                icon: 'pi pi-fw pi-exclamation-circle',
                to: '/pages/notfound'
            },
            {
                label: 'Empty',
                icon: 'pi pi-fw pi-circle-off',
                to: '/pages/empty'
            }
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
    {
        label: 'Get Started',
        items: [
            {
                label: 'Documentation',
                icon: 'pi pi-fw pi-book',
                to: '/documentation'
            },
            {
                label: 'View Source',
                icon: 'pi pi-fw pi-github',
                url: 'https://github.com/primefaces/sakai-vue',
                target: '_blank'
            }
        ]
    }
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
