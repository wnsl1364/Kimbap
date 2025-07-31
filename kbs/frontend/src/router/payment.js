export default [
    /** @/views/standard/products/ProductTable.vue 이런구조로*/
    {
        path: '/payment/cashflow',
        name: 'payCash',
        component: () => import('@/views/payment/cashflow.vue'),
    },
    {
        path: '/payment/unpaidallocation',
        name: 'payunpaid',
        component: () => import('@/views/payment/unpaidallocation.vue'),
    },
];
