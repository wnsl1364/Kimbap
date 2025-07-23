import { createApp } from 'vue';
import { definePreset } from '@primeuix/themes'
import { createPinia } from 'pinia';
import { createPersistedState } from 'pinia-plugin-persistedstate';
import App from './App.vue';
import router from './router';

import stdRouter from './router/standard';
import materialRouter from './router/material';
import orderRouter from './router/order';
import productionRouter from './router/production';
import logisticsRouter from './router/logistics';
import paymentRouter from './router/payment';
import Lara from '@primeuix/themes/lara'
import PrimeVue from 'primevue/config';
import ConfirmationService from 'primevue/confirmationservice';
import ToastService from 'primevue/toastservice';

import '@/assets/styles.scss';

const app = createApp(App);

const pinia = createPinia();
pinia.use(createPersistedState()) // localStorage 기본 사용
app.use(pinia);

app.use(router);
app.use(stdRouter);
app.use(materialRouter);
app.use(orderRouter);
app.use(productionRouter);  
app.use(logisticsRouter);
app.use(paymentRouter);

const CustomTheme = definePreset(Lara, {
  semantic: {
    primary: {
      100: '#f5832cff',
      200: '#ebb082ff',
      300: '#F47D22',
      400: '#F47D22',
      500: '#F47D22', // ✅ 여기에 원하는 색상
      600: '#ea580c',
      700: '#c2410c',
      800: '#9a3412',
      900: '#7c2d12'
    },
  }
})

app.use(PrimeVue, {
  theme: {
    preset: CustomTheme,
    options: {
      darkModeSelector: '.app-dark'
    }
  }
})
app.use(ToastService);
app.use(ConfirmationService);

app.mount('#app');
