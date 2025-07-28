export default [
    /** @/views/standard/products/ProductTable.vue 이런구조로*/
    {
        path: '/standard/products',
        name: 'stdProd',
        component: () => import('@/views/standard/material.vue'),
    },
];
