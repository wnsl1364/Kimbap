<script setup>
import { ref, computed } from 'vue';
import { storeToRefs } from 'pinia';
import { useMemberStore } from '@/stores/memberStore'
import AppMenuItem from './AppMenuItem.vue';

// 로그인 정보 가져오기
const memberStore = useMemberStore()
const { role, team } = storeToRefs(memberStore)   // role.value, team.value 바로 사용
const userRole = role
const userTeam = team

// p1 : 사원
// p2 : 매출업체
// p3 : 공급업체
// p4 : 담당자
// p5 : 관리자

// DEPT-1-1 영업팀
// DEPT-1-2 구매팀
// DEPT-2-1 생산팀
// DEPT-3-1 회계팀
// DEPT-4-1 자재팀
// DEPT-4-2 물류팀
// DEPT-5-1 창고1팀
// DEPT-5-2 창고2팀



const allowed = (item, parentRoles, parentTeams) => {
    const roles = item?.roles ?? parentRoles
    const teams = item?.teams ?? parentTeams

    const r = userRole.value
    const t = userTeam.value

    // 관리자 전체보기 옵션 (원치 않으면 이 줄 삭제)
    if (r === 'p5') return true

    const okRole = !roles || (Array.isArray(roles) ? roles.includes(r) : roles === r)
    const okTeam = !teams || (Array.isArray(teams) ? teams.includes(t) : teams === t)
    return okRole && okTeam
}

const filterMenu = (list, parentRoles = null, parentTeams = null) => {
    return (list || [])
        .map(node => {
            const thisRoles = node.roles ?? parentRoles
            const thisTeams = node.teams ?? parentTeams

            const children = node.items
                ? filterMenu(node.items, thisRoles, thisTeams) // ← 재귀
                : []

            const selfVisible = allowed(node, parentRoles, parentTeams)
            if (!selfVisible && children.length === 0) return null

            //   return { ...node, items: children } 주석풀면 펼침기능으로 변해버림
            const out = { ...node }
            if (children.length > 0) out.items = children
            else delete out.items                // 🔥 자식이 없으면 items 삭제!
            return out
        })
        .filter(Boolean)
}

const filteredModel = computed(() => filterMenu(model.value))


const model = ref([
    {
        items: [
            { label: 'Dashboard', icon: 'pi pi-fw pi-home', to: '/' },
        ]
    },
    {
        teams: ['DEPT-1-1', 'DEPT-1-2', 'DEPT-2-1', 'DEPT-3-1', 'DEPT-4-1', 'DEPT-4-2', 'DEPT-5-1', 'DEPT-5-2'], // 매출이랑 공급만 안보이게
        items: [{
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
        }]
    },
    {
        teams: ['DEPT-1-1', 'DEPT-1-2', 'DEPT-2-1', 'DEPT-3-1', 'DEPT-4-1', 'DEPT-4-2', 'DEPT-5-1', 'DEPT-5-2'],
        roles: ['p1', 'p2', 'p4'], // 영업팀 사원, 매출업체, 담당자''],
        items: [{
            label: '주문',
            items: [
                {
                    label: '주문등록', // 매출업체만
                    to: '/order/orderRegister',
                    roles: ['p1', 'p2', 'p4'], // 사원, 매출업체, 담당자
                    teams: ['DEPT-1-1', ''] // 영업팀, 구매팀
                },
                {
                    label: '주문검토', // 영업
                    to: '/order/orderReview',
                    teams: ['DEPT-1-1']
                },
                {
                    label: '주문목록', // 매출업체, 영업
                    to: '/order/orderList',
                    roles: ['p1', 'p2', 'p4'], // 사원, 매출업체, 담당자
                    teams: ['DEPT-1-1', ''] // 영업팀, 구매팀,
                },
                {
                    label: '거래처원장', // 영업
                    to: '/order/orderLedger',
                    teams: ['DEPT-1-1']
                },
                {
                    label: '반품관리', // 영업
                    to: '/order/returnManage',
                    teams: ['DEPT-1-1']
                },
            ]
        }]
    },
    {
        teams: ['DEPT-1-1', 'DEPT-1-2', 'DEPT-2-1', 'DEPT-3-1', 'DEPT-4-1', 'DEPT-4-2', 'DEPT-5-1', 'DEPT-5-2',],
        roles: ['p1', 'p3', 'p4'],
        items: [{
            label: '자재',
            items: [
                {
                    label: '자재 재고 조회', // 자재팀 , 구매팀
                    to: '/material/materialStockView',
                },
                {
                    label: '자재 발주 조회', // 공급업체 자재팀
                    to: '/material/materialPurchaseView',
                    teams: ['DEPT-1-2', 'DEPT-4-1', ''],
                    roles: ['p1', 'p3', 'p4'], // 사원, 공급업체, 담당자
                },
                {
                    label: '자재 발주',
                    to: '/material/materialPurchase',
                    teams: ['DEPT-4-1', 'DEPT-1-2'],
                },
                {
                    label: '자재 발주 승인', // 공급업체 자재팀
                    to: '/material/MaterialPurchaseApproval',
                    teams: ['DEPT-4-1', 'DEPT-1-2', ''],
                    roles: ['p1', 'p3', 'p4'],
                },
                {
                    label: '자재 출고', // 공급업체만
                    to: '/material/materialOutbound',
                    teams: [''],
                    roles: ['p3'],
                },
                {
                    label: '자재 입고',
                    to: '/material/MaterialPurchaseView?from=inbound',
                    teams: ['DEPT-4-1', 'DEPT-1-2'],
                },
                {
                    label: '자재 적재 대기 목록',
                    to: '/material/mateLoading',
                    teams: ['DEPT-4-1', 'DEPT-1-2']
                },
                {
                    label: '자재 이동 요청 등록',
                    to: '/material/stockMovementRegister',
                    teams: ['DEPT-4-1', 'DEPT-1-2'],
                },
                {
                    label: '자재 이동 요청 목록',
                    to: '/material/stockMovementList',
                    teams: ['DEPT-4-1', 'DEPT-1-2']
                },
                {
                    label: '자재 입출고 내역',
                    to: '/material/materialInOutHistory',
                },
            ]
        }]
    },
    { // 조회페이지는 매추 공급 빼고 다가능
        teams: ['DEPT-1-1', 'DEPT-1-2', 'DEPT-2-1', 'DEPT-3-1', 'DEPT-4-1', 'DEPT-4-2', 'DEPT-5-1', 'DEPT-5-2'],
        items: [{
            label: '생산',
            items: [
                {
                    label: '생산 계획 등록', // 등록 요청 생산만
                    to: '/production/productionPlan',
                    teams: ['DEPT-2-1'],
                },
                {
                    label: '생산 계획 조회',
                    to: '/production/productionPlanList'
                },
                {
                    label: '생산요청서 등록', // 생산만
                    to: '/production/productionRequest',
                    teams: ['DEPT-2-1'],
                },
                {
                    label: '생산 요청 조회',
                    to: '/production/productionRequestList'
                },
                {
                    label: '제품 적재 대기', // 물류 창고1,2
                    to: '/production/productInbound',
                    teams: ['DEPT-4-2', 'DEPT-5-1', 'DEPT-5-2'],
                },
            ]
        }]
    },
    {
        teams: ['DEPT-1-1', 'DEPT-1-2', 'DEPT-2-1', 'DEPT-3-1', 'DEPT-4-1', 'DEPT-4-2', 'DEPT-5-1', 'DEPT-5-2'],
        items: [{
            label: '물류',
            items: [
                {
                    label: '출고지시서 조회',
                    to: '/distribution/relOrdList',
                },
                {
                    label: '출고 지시서 등록', // 물류
                    to: '/distribution/relOrdAndResult',
                    teams: ['DEPT-4-2'],
                },
                {
                    label: '출고 처리', // 창고1,2
                    to: '/distribution/relSave',
                    teams: ['DEPT-5-1', 'DEPT-5-2'],
                },
                {
                    label: '완제품 입출고 조회', 
                    to: '/distribution/distributionCheck'
                },
            ]
        }]
    },
    {
        teams: ['DEPT-3-1'],
        items: [{
            label: '결제',
            items: [
                {
                    label: '입출금 관리', // 회계
                    to: '/payment/cashflow'
                },
                {
                    label: '미수금 정산', // 회계
                    to: '/payment/unpaidallocation'
                },
            ]
        }]
    },
]);
</script>

<template>
    <ul class="layout-menu">
        <template v-for="(item, i) in filteredModel" :key="i">
            <app-menu-item v-if="!item.separator" :item="item" :index="i" />
            <li v-else class="menu-separator"></li>
        </template>
    </ul>
</template>

<style lang="scss" scoped></style>
