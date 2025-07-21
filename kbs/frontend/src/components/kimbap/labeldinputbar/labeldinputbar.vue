<template>
  <div class="flex flex-col gap-1">
    <label class="text-[16px] font-bold text-gray-800 tracking-tight leading-none mb-2">
      {{ label }}
    </label>
    <input
    :type="type"
    :placeholder="placeholder"
    :disabled="disabled"
    :readonly="readonly"
    :value="modelValue"
    @input="$emit('update:modelValue', $event.target.value)"
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