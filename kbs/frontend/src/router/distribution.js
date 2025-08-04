export default [
    /** @/views/standard/products/ProductTable.vue 이런구조로*/
    {
        path: '/distribution/distributionCheck',
        name: 'distributionCheck',
        component: () => import('@/views/distribution/DistributionCheck.vue'),
    },
];