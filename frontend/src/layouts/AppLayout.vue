<template>
  <div class="app-shell">
    <aside class="app-sidebar">
      <div>
        <div style="font-weight: 700; font-size: 18px;">工业残次品检测</div>
        <div class="subtle">Cloud + Edge 智能质检</div>
      </div>
      <SidebarMenu />
    </aside>

    <header class="app-header">
      <TopBar />
    </header>

    <main class="app-main">
      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import { RouterView } from 'vue-router'
import SidebarMenu from '../components/SidebarMenu.vue'
import TopBar from '../components/TopBar.vue'
</script>
