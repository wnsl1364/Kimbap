<script setup>
import { useCounterStore } from '@/stores/piniaStore'

const counter = useCounterStore()
</script>

<template>
  <div>
    <p>{{ counter.count }}</p>
    <button @click="counter.increment">+</button>
  </div>
</template>