import { createApp } from 'vue';
import App from './App.vue';
import router from './router';

import stdRouter from './router/standard';
import materialRouter from './router/material';
import orderRouter from './router/order';
import productionRouter from './router/production';
import logisticsRouter from './router/logistics';
import paymentRouter from './router/payment';

import Aura from '@primeuix/themes/aura';
import PrimeVue from 'primevue/config';
import ConfirmationService from 'primevue/confirmationservice';
import ToastService from 'primevue/toastservice';

import '@/assets/styles.scss';

const app = createApp(App);

app.use(router);
app.use(stdRouter);
app.use(materialRouter);
app.use(orderRouter);
app.use(productionRouter);  
app.use(logisticsRouter);
app.use(paymentRouter);

app.use(PrimeVue, {
    theme: {
        preset: Aura,
        options: {
            darkModeSelector: '.app-dark'
        }
    }
});
app.use(ToastService);
app.use(ConfirmationService);

app.mount('#app');
