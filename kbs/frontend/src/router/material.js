export default [
    /** @/views/standard/products/ProductTable.vue 이런구조로*/
    {
        path: '/material/materialStockView',
        name: 'materialStockView',
        component: () => import('@/views/material/MaterialStockView.vue'),
    },

    //------------------------------------------------------------------------
    {
        path: '/material/materialInbound',
        name: 'materialInbound',
        component: () => import('@/views/material/MaterialInbound.vue'),
    },
    {
        path: '/material/materialLoadingWaitingList',
        name: 'materialLoadingWaitingList',
        component: () => import('@/views/material/MaterialLoadingWaitingList.vue'),
    },
    {
        path: '/material/transferRequestForm',
        name: 'transferRequestForm',
        component: () => import('@/views/material/TransferRequestForm.vue'),
    },
    {
        path: '/material/transferRequestList',
        name: 'transferRequestList',
        component: () => import('@/views/material/TransferRequestList.vue'),
    },
    {
        path: '/material/materialInOutHistory',
        name: 'materialInOutHistory',
        component: () => import('@/views/material/MaterialInOutHistory.vue'),
    },
];