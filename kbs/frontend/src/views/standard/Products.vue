<template>
    <div class="product-view">
        <h1>Standard Product View</h1>
        <p>This is the standard product view page.</p>
        <!-- Add your product-related content here -->
    </div>
    <BasicTable
      :data="products"
      :columns="productColumns"/>
</template>
<script setup>
import BasicTable from '@/components/kimbap/table/BasicTable.vue'

</script>