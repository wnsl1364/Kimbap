export default [
    /** @/views/standard/products/ProductTable.vue 이런구조로*/
    // 완제품 입출고 조회
    {
        path: '/distribution/distributionCheck',
        name: 'distributionCheck',
        component: () => import('@/views/distribution/DistributionCheck.vue'),
    },

    // 출고지시서 등록(처리vue)
    {
        path: '/distribution/relOrdAndResult',
        name: 'relOrdAndResult',
        component: () => import('@/views/distribution/RelOrdAndResult.vue'),
    },
    
    // 출고처리 및 조회
    {
        path: '/distribution/relOrdList',
        name: 'relOrdList',
        component: () => import('@/views/distribution/RelOrdList.vue'),
    },
    // 출고처리
    {
        path: '/distribution/relSave',
        name: 'relSave',
        component: () => import('@/views/distribution/RelSave.vue'),
    },
];