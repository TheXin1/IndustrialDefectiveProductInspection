<template>
  <div class="panel">
    <h2 style="margin-top: 0;">{{ title }}</h2>
    <p class="subtle">该模块待接入后端接口。</p>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const title = computed(() => (route.meta.title as string) || '模块页面')
</script>
