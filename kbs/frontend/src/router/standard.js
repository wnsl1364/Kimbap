export default [
    /** @/views/standard/products/ProductTable.vue 이런구조로*/
    {
        path: '/standard/material',
        name: 'stdMat',
        component: () => import('@/views/standard/material.vue'),
    },
    {
        path: '/standard/product',
        name: 'stdPro',
        component: () => import('@/views/standard/product.vue'),
    },
    {
        path: '/standard/company',
        name: 'stdCp',
        component: () => import('@/views/standard/company.vue'),
    },
    {
        path: '/standard/factory',
        name: 'stdFac',
        component: () => import('@/views/standard/factory.vue'),
    },
    {
        path: '/standard/warehouse',
        name: 'stdWh',
        component: () => import('@/views/standard/warehouse.vue'),
    },
];
