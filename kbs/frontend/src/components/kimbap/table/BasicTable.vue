
<template>
    <div class="card">
        <DataTable :value="products" tableStyle="min-width: 50rem" showGridlines paginator :rows="10" responsiveLayout="scroll" v-model:selection="selectedProducts" dataKey="id" size="large">
            <Column selectionMode="multiple" headerStyle="width: 3rem"></Column>
            <Column v-for="col of columns" :key="col.field" :field="col.field" :header="col.header">
            </Column>
        </DataTable>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ProductService } from '@/service/ProductService';

onMounted(() => {
    ProductService.getProductsMini().then((data) => (products.value = data));
});

const products = ref();
const selectedProducts = ref();
const columns = [
    { field: 'code', header: 'Code' },
    { field: 'name', header: 'Name' },
    { field: 'category', header: 'Category' },
    { field: 'quantity', header: 'Quantity' }
];

</script>
