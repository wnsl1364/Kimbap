<script setup>
import SearchForm from '@/components/kimbap/searchform/SearchForm.vue';
import InputTable from '@/components/kimbap/table/InputTable.vue';
import { useMaterialStore } from '@/stores/materialStore';
import { useMemberStore } from '@/stores/memberStore';
import { useCommonStore } from '@/stores/commonStore';
import { ref, computed, onMounted, watch, nextTick } from 'vue';
import { useToast } from 'primevue/usetoast';
import { useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import { format, isValid } from 'date-fns';

const materialStore = useMaterialStore();
const memberStore = useMemberStore();
const common = useCommonStore();
const toast = useToast();
const router = useRouter();

const { materialList } = storeToRefs(materialStore);

const loadMaterialList = async (params) => {
    try {
        const response = await materialStore.getMaterialList(params);
        if (response.success) {
            materialStore.setMaterialList(response.data);
        } else {
            toast.add({ severity: 'error', summary: 'Error', detail: response.message });
        }
    } catch (error) {
        toast.add({ severity: 'error', summary: 'Error', detail: 'Failed to load material list' });
    }
};

const displayCode = computed(() => {
    if(!materialList.value) return [];
    
    const statusCodes = common.getCodes('0E');
    const unitCodes = common.getCodes('0G');
    const typeCodes = common.getCodes('0H');
    
    return materialList.value.map(item => ({
        ...item,
        statCodes: getCodeName(statusCodes, item.statCodes) || item.statCodes,
        unitCodes: getCodeName(unitCodes, item.unitCodes) || item.unitCodes,
        typeCodes: getCodeName(typeCodes, item.typeCodes) || item.typeCodes
    }));
});

// 그리고 getCodeName 함수도 확인해봐!
const getCodeName = (codes, value) => {
    return codes?.find(code => code.dcd === value)?.cdInfo;
};

const searchColumns = materialStore.materialSearchColumns;

onMounted(async () => {
    await Promise.all([
    common.fetchCommonCodes('0G'),
    common.fetchCommonCodes('0C'),
    common.fetchCommonCodes('0H')
  ]);
});
</script>
<template>
    <SearchForm 
    :columns="searchColumns"
    :gridColumns="4"
    @search="onSearch"
    @reset="onReset"
    />
    <InputTable />
</template>