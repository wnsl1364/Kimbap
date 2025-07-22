<template>
  <div class="flex flex-col gap-1">
    <label class="text-[16px] font-bold text-gray-800 leading-none mb-2">
      {{ label }}
    </label>
    <input
      type="date"
      :disabled="disabled"
      :value="modelValue"
      @input="$emit('update:modelValue', $event.target.value)"
      class="border border-gray-300 rounded-xl px-3 py-2 text-[14px] text-gray-800 bg-white resize-none 
         focus:outline-none focus:ring-2 focus:ring-blue-400 focus:border-blue-400"
    />
  </div>
</template>

<script setup>
defineEmits(['update:modelValue'])

defineProps({
  label: { type: String, required: true },
  modelValue: { type: String, default: '' }, // 'YYYY-MM-DD' 형식
  disabled: { type: Boolean, default: false },
})
</script>
