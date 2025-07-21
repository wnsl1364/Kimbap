export default [
    /** @/views/standard/products/ProductTable.vue 이런구조로*/
    {
        path: '/material/materialStockView',
        name: 'materialStockView',
        component: () => import('@/views/material/MaterialStockView.vue'),
    },
];