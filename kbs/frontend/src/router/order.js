export default [
    /** @/views/standard/products/ProductTable.vue 이런구조로*/
    /**주문 등록 */
    {
        path: '/order/orderRegister',
        name: 'ordRegister',
        component: () => import('@/views/order/OrderRegisters.vue'),
    },
    /**주문 검토 */
    {
        path: '/order/orderReview',
        name: 'ordReview',
        component: () => import('@/views/order/OrderReview.vue'),
    },
    /**주문 목록 */
    {
        path: '/order/orderList',
        name: 'ordList',
        component: () => import('@/views/order/OrderList.vue'),
    },
    /**거래내역 */
    {
        path: '/order/transactionDetails',
        name: 'ordTransactionDetails',
        component: () => import('@/views/order/TransactionDetails.vue'),
    },
    /**거래처원장 */
    {
        path: '/order/orderLedger',
        name: 'ordLedger',
        component: () => import('@/views/order/CustomerOutstanding.vue'),
    },
    /**반품관리 */
    {
        path: '/order/returnManage',
        name: 'returnManage',
        component: () => import('@/views/order/ReturnManage.vue'),
    },
    /**반품요청 */
    {
        path: '/order/returnRequest/:ordCd',
        name: 'ordReturnRequest',
        component: () => import('@/views/order/ReturnRequest.vue'),
    }
];
