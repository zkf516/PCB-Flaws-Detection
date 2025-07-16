<script setup lang="ts">
import Nav from './components/Nav.vue';
import { onMounted, onUnmounted } from 'vue';
import { useThemeStore } from './stores/theme';
import NotiBubble from './components/NotiBubbles.vue';

const themeStore = useThemeStore();

// Initialize theme and mobile detection on app mount
onMounted(() => {
  themeStore.initializeMobileDetection();
});

// Cleanup mobile detection on unmount
onUnmounted(() => {
  themeStore.cleanupMobileDetection();
});
</script>

<template>
  <Nav />
  <NotiBubble />
  <div class="router-div">
    <router-view />
  </div>
</template>

<style scoped>
.router-div {
  height: calc(100vh - 5em);
  margin-bottom: 3em;
  margin-top: 2em;
  width: 100%;
  overflow: auto;
}


button {
  margin: 20px;
  padding: 10px 20px;
  cursor: pointer;
}
</style>

