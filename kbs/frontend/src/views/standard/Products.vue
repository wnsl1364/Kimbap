<template>
  <div class="space-y-4">
    <Labeldinputbar
      label="이름"
      v-model="name"
      placeholder="이름을 입력하세요"
    />

    <Labelddropdown
    label="구분"
    v-model="category"
    :options="[
      { label: '일반', value: 'normal' },
      { label: '관리자', value: 'admin' },
      { label: '시스템', value: 'system' }
    ]"
    placeholder="구분 선택">
    </Labelddropdown>
    <Labelddatepicker 
    label="날자선택"
    v-model="regdate"
    placeholder="날자를선택해주세요"/>

    <Labeldtextarea
        label="비고"
        v-model="comment"
        placeholder="내용을 입력하세요"
        :autoResize="true"
    />
    <Button label="상품 선택" @click="show = true" />
    <Singleselect
      v-model="selectedItem"
      :visible="show"
      @update:visible="show = $event"
    />
    <div class="mt-2 text-sm text-gray-700">
      선택된 상품: {{ selectedProduct?.name || '없음' }}
    </div>
    <BasicTable
      :data="products"
      :columns="productColumns"/>
</template>
<script setup>
import BasicTable from '@/components/kimbap/table/BasicTable.vue'
    
    <Dependentselectbox v-model="selectedCode"
      :typeOptions="[
        { label: '공급업체', value: 'supplier' },
        { label: '매출업체', value: 'customer' }
      ]"
    />
  </div>
</template>
<script setup>
import { ref } from 'vue'
import Labelddropdown from '@/components/kimbap/labeldinputbar/labelddropdown.vue';
import Labeldinputbar from '@/components/kimbap/labeldinputbar/labeldinputbar.vue';
import Labelddatepicker from '@/components/kimbap/labeldinputbar/labelddatepicker.vue';
import Labeldtextarea from '@/components/kimbap/labeldinputbar/labeldtextarea.vue';
import Singleselect from '@/components/kimbap/modal/singleselect.vue';
import Dependentselectbox from '@/components/kimbap/labeldinputbar/dependentselectbox.vue';

const category = ref('')
const comment = ref('')
const selectedCode = ref('')

const show = ref(false)
const selectedItem = ref(null)

</script>