<script setup>
import { ref } from 'vue';
import LeftAlignTable from '@/components/kimbap/table/LeftAlignTable.vue';
import InputTable from '@/components/kimbap/table/InputTable.vue';
</script>

<template>
    <div class="space-y-4">
        <!-- 기본정보 영역 -->
        <LeftAlignTable/>
    </div>
    <div class="space-y-4 mt-8">
        <InputTable/>
    </div>
</template>
