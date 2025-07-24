export default [
    /** @/views/standard/products/ProductTable.vue 이런구조로*/
    {
        path: '/login/loginForm',
        name: 'loginForm',
        component: () => import('@/views/login/LoginForm.vue'),
    },
    {
        path: '/login/memberAdd',
        name: 'memberAdd',
        component: () => import('@/views/login/MemberAdd.vue'),
    },
];