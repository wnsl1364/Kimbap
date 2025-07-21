<template>
  <div class="flex flex-col gap-1">
    <label class="text-[13px] font-medium text-gray-800 tracking-tight leading-none">
      {{ label }}
    </label>
    <InputText
      :type="type"
      :placeholder="placeholder"
      :disabled="disabled"
      :readonly="readonly"
      :model-value="modelValue"
      @update:modelValue="$emit('update:modelValue', $event)"
      class="border border-gray-400 rounded-sm px-2 py-[6px] text-[14px] text-gray-900 focus:outline-none focus:ring-1 focus:ring-gray-700 focus:border-gray-700"
    />
  </div>
</template>



<script setup>

defineEmits(['update:modelValue'])

defineProps({
  label: { type: String, required: true },
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '' },
  readonly: { type: Boolean, default: false }, 
  disabled: { type: Boolean, default: false },
  type: { type: String, default: 'text' },
})

</script>