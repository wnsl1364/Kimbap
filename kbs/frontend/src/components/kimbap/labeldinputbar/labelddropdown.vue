<template>
  <div class="flex flex-col gap-1">
    <label class="text-[16px] font-bold text-gray-800 leading-none mb-2">
      {{ label }}
    </label>
    <select
      :disabled="disabled"
      :value="modelValue"
      @change="$emit('update:modelValue', $event.target.value)"
      class="border border-gray-400 rounded-sm px-2 py-[6px] text-[14px] text-gray-900 bg-white focus:outline-none focus:ring-1 focus:ring-gray-700 focus:border-gray-700"
    >
      <option value="" disabled selected hidden>{{ placeholder }}</option>
      <option
        v-for="(item, index) in options"
        :key="index"
        :value="item.value"
      >
        {{ item.label }}
      </option>
    </select>
  </div>
</template>

<script setup>
defineEmits(['update:modelValue'])

defineProps({
  label: { type: String, required: true },
  modelValue: { type: [String, Number], default: '' },
  options: {
    type: Array,
    required: true,
    // 예시: [{ label: '선택1', value: 'opt1' }]
  },
  placeholder: { type: String, default: '선택하세요' },
  disabled: { type: Boolean, default: false },
})
</script>
