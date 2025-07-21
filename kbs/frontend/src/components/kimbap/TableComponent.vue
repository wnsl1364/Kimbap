<script setup>
import { CustomerService } from '@/service/CustomerService';
import { onBeforeMount, ref } from 'vue';
import SearchFrom from '@/components/kimbap/searchform/SearchFrom.vue';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';

const customers2 = ref(null);
const balanceFrozen = ref(false);

function formatCurrency(value) {
    return value.toLocaleString('en-US', { style: 'currency', currency: 'USD' });
}

onBeforeMount(() => {
    CustomerService.getCustomersLarge().then((data) => (customers2.value = data));
});
</script>

<template>          
    <div class="flex flex-col gap-8">
        <!-- 검색 폼 컴포넌트 -->
        <SearchFrom />
        
        <!-- 테이블 영역 -->
        <div class="card">
            <div class="font-semibold text-xl mb-4">Frozen Columns</div>
            <ToggleButton v-model="balanceFrozen" onIcon="pi pi-lock" offIcon="pi pi-lock-open" onLabel="Balance" offLabel="Balance" />

            <DataTable :value="customers2" scrollable scrollHeight="400px" class="mt-6">
                <Column field="name" header="Name" style="min-width: 200px" frozen class="font-bold"></Column>
                <Column field="id" header="Id" style="min-width: 100px"></Column>
                <Column field="name" header="Name" style="min-width: 200px"></Column>
                <Column field="country.name" header="Country" style="min-width: 200px"></Column>
                <Column field="date" header="Date" style="min-width: 200px"></Column>
                <Column field="company" header="Company" style="min-width: 200px"></Column>
                <Column field="status" header="Status" style="min-width: 200px"></Column>
                <Column field="activity" header="Activity" style="min-width: 200px"></Column>
                <Column field="representative.name" header="Representative" style="min-width: 200px"></Column>
                <Column field="balance" header="Balance" style="min-width: 200px" alignFrozen="right" :frozen="balanceFrozen">
                    <template #body="{ data }">
                        <span class="font-bold">{{ formatCurrency(data.balance) }}</span>
                    </template>
                </Column>
            </DataTable>
        </div>
    </div>
  </template>

<style scoped lang="scss">
:deep(.p-datatable-frozen-tbody) {
    font-weight: bold;
}

:deep(.p-datatable-scrollable .p-frozen-column) {
    font-weight: bold;
}
</style>
