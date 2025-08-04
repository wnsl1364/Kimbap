import AppLayout from '@/layout/AppLayout.vue';
import { createRouter, createWebHistory } from 'vue-router';
import { createPinia } from 'pinia';
import { useMemberStore } from '@/stores/memberStore';
import { storeToRefs } from 'pinia';
import stdRoutes from './standard';
import materialRoutes from './material';
import orderRoutes from './order';
import productionRoutes from './production';
import logisticsRoutes from './logistics';
import paymentRoutes from './payment';
import loginRoutes from './login';
import distributionRoutes from './distribution';

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            component: AppLayout,
            children: [
                {
                    path: '/',
                    name: 'dashboard',
                    component: () => import('@/views/Dashboard.vue'),
                    meta: { requiresAuth: true }
                },
                {
                    path: '/BasicTableCheckOneParent',
                    name: 'BasicTableCheckOneParent',
                    component: () => import('@/views/reperence/BasicTableCheckOneParent.vue'),
                    meta: { requiresAuth: true }
                },
                {
                    path: '/BasicTableParent',
                    name: 'BasicTableParent',
                    component: () => import('@/views/reperence/BasicTableParent.vue'),
                    meta: { requiresAuth: true }
                },
                {
                    path: '/InputTableParent',
                    name: 'InputTableParent',
                    component: () => import('@/views/reperence/InputTableParent.vue'),
                    meta: { requiresAuth: true }
                },
                {
                    path: '/LeftAlignTableParent',
                    name: 'LeftAlignTableParent',
                    component: () => import('@/views/reperence/LeftAlignTableParent.vue'),
                    meta: { requiresAuth: true }
                },
                {
                    path: '/uikit/formlayout',
                    name: 'formlayout',
                    component: () => import('@/views/uikit/FormLayout.vue'),
                    meta: { requiresAuth: true }
                },
                {
                    path: '/uikit/input',
                    name: 'input',
                    component: () => import('@/views/uikit/InputDoc.vue'),
                    meta: { requiresAuth: true }
                },
                {
                    path: '/uikit/button',
                    name: 'button',
                    component: () => import('@/views/uikit/ButtonDoc.vue'),
                    meta: { requiresAuth: true }
                },
                {
                    path: '/uikit/table',
                    name: 'table',
                    component: () => import('@/views/uikit/TableDoc.vue'),
                    meta: { requiresAuth: true }
                },
                {
                    path: '/uikit/list',
                    name: 'list',
                    component: () => import('@/views/uikit/ListDoc.vue'),
                    meta: { requiresAuth: true }
                },
                {
                    path: '/uikit/tree',
                    name: 'tree',
                    component: () => import('@/views/uikit/TreeDoc.vue'),
                    meta: { requiresAuth: true }
                },
                {
                    path: '/uikit/panel',
                    name: 'panel',
                    component: () => import('@/views/uikit/PanelsDoc.vue'),
                    meta: { requiresAuth: true }
                },

                {
                    path: '/uikit/overlay',
                    name: 'overlay',
                    component: () => import('@/views/uikit/OverlayDoc.vue'),
                    meta: { requiresAuth: true }
                },
                {
                    path: '/uikit/media',
                    name: 'media',
                    component: () => import('@/views/uikit/MediaDoc.vue'),
                    meta: { requiresAuth: true }
                },
                {
                    path: '/uikit/message',
                    name: 'message',
                    component: () => import('@/views/uikit/MessagesDoc.vue'),
                    meta: { requiresAuth: true }
                },
                {
                    path: '/uikit/file',
                    name: 'file',
                    component: () => import('@/views/uikit/FileDoc.vue'),
                    meta: { requiresAuth: true }
                },
                {
                    path: '/uikit/menu',
                    name: 'menu',
                    component: () => import('@/views/uikit/MenuDoc.vue'),
                    meta: { requiresAuth: true }
                },
                {
                    path: '/uikit/charts',
                    name: 'charts',
                    component: () => import('@/views/uikit/ChartDoc.vue'),
                    meta: { requiresAuth: true }
                },
                {
                    path: '/uikit/misc',
                    name: 'misc',
                    component: () => import('@/views/uikit/MiscDoc.vue'),
                    meta: { requiresAuth: true }
                },
                {
                    path: '/uikit/timeline',
                    name: 'timeline',
                    component: () => import('@/views/uikit/TimelineDoc.vue'),
                    meta: { requiresAuth: true }
                },
                {
                    path: '/pages/empty',
                    name: 'empty',
                    component: () => import('@/views/pages/Empty.vue'),
                    meta: { requiresAuth: true }
                },
                {
                    path: '/pages/crud',
                    name: 'crud',
                    component: () => import('@/views/pages/Crud.vue'),
                    meta: { requiresAuth: true }
                },
                {
                    path: '/documentation',
                    name: 'documentation',
                    component: () => import('@/views/pages/Documentation.vue'),
                    meta: { requiresAuth: true }
                },
                {
                    path: '/tablecomp',
                    name: 'tablecomp',
                    component: () => import('../components/kimbap/TableComponent.vue'),
                    meta: { requiresAuth: true }
                },
                /** 냉동김밥 경로 */
                ...stdRoutes,
                ...orderRoutes,
                ...materialRoutes,
                ...productionRoutes,
                ...logisticsRoutes,
                ...paymentRoutes,
                ...loginRoutes,
                ...distributionRoutes,
            ]
        },
        {
            path: '/login/loginForm',
            name: 'loginForm',
            component: () => import('@/views/login/LoginForm.vue')
        },
        {
            path: '/landing',
            name: 'landing',
            component: () => import('@/views/pages/Landing.vue'),
            meta: { requiresAuth: true }
        },
        {
            path: '/pages/notfound',
            name: 'notfound',
            component: () => import('@/views/pages/NotFound.vue'),
            meta: { requiresAuth: true }
        },
        {
            path: '/auth/access',
            name: 'accessDenied',
            component: () => import('@/views/pages/auth/Access.vue'),
            meta: { requiresAuth: true }
        },
        {
            path: '/auth/error',
            name: 'error',
            component: () => import('@/views/pages/auth/Error.vue'),
            meta: { requiresAuth: true }
        },
    ]
});

// 라우터 가드 설정
router.beforeEach((to, from, next) => {
    const memberStore = useMemberStore();
    const { isLogin } = storeToRefs(memberStore);

    // 보호된 페이지 접근 시 로그인 여부 체크
    if (to.meta.requiresAuth && !isLogin.value) {
        return next('/login/loginForm');
    }

    // 로그인 상태일 때 로그인 페이지 접근 막기
    if (isLogin.value && to.path === '/login/loginForm') {
        return next('/');
    }

    next();
});


export default router;
