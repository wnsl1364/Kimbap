export default [
    /** @/views/standard/products/ProductTable.vue 이런구조로*/
    // 생산계획
    {
        path: '/production/productionPlan',
        name: 'productionPlan',
        component: () => import('@/views/production/ProductionPlan.vue'),
    },
    // 생산계획목록
    {
        path: '/production/productionPlanList',
        name: 'productionPlanList',
        component: () => import('@/views/production/ProductionPlanList.vue'),
    },
    // 생산요청
    {
        path: '/production/productionRequest',
        name: 'productionRequest',
        component: () => import('@/views/production/ProductionRequest.vue'),
    },
    // 생산요청목록
    {
        path: '/production/productionRequestList',
        name: 'productionRequestList',
        component: () => import('@/views/production/ProductionRequestList.vue'),
    },
    // 제품입고대기
    {
        path: '/production/productInbound',
        name: 'productInbound',
        component: () => import('@/views/production/ProductInbound.vue'),
    },
    
];
