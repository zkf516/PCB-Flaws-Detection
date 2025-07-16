<script setup lang="ts">
import { ref, watch, onMounted } from 'vue';
import { Capacitor } from '@capacitor/core';
import { useThemeStore } from '../stores/theme';
import { useNotificationStore } from '../stores/notification';
import ThemeDisplay from '../components/ThemeDisplay.vue';

const themeStore = useThemeStore();
const notificationStore = useNotificationStore();

const isMstChecked = ref(themeStore.isAutoDark);
const apiUrl = ref('');

// 根据平台获取默认API地址
const getDefaultApiUrl = (): string => {
  if (Capacitor.isNativePlatform()) {
    return 'http://192.168.3.6:5001/api/v1';
  } else {
    return 'http://localhost:5001/api/v1';
  }
};

// 从localStorage获取自定义API地址，如果没有则使用默认值
const getStoredApiUrl = () => {
  return localStorage.getItem('custom_api_url') || getDefaultApiUrl();
};

onMounted(() => {
  apiUrl.value = getStoredApiUrl();
});

function mstChecked() {
  if (isMstChecked.value === true) {
    themeStore.useDeviceTheme();
  } else {
    themeStore.setLightDark(themeStore.isDark ? 'dark' : 'light');
  }
}

function saveApiUrl() {
  if (apiUrl.value.trim()) {
    // 确保URL格式正确
    let url = apiUrl.value.trim();
    if (!url.startsWith('http://') && !url.startsWith('https://')) {
      url = 'http://' + url;
    }
    
    // 移除末尾的斜杠
    url = url.replace(/\/$/, '');
    
    localStorage.setItem('custom_api_url', url);
    notificationStore.showNotification('API地址已保存，请刷新页面使设置生效', 'check');
  }
}

function resetApiUrl() {
  const defaultUrl = getDefaultApiUrl();
  apiUrl.value = defaultUrl;
  localStorage.setItem('custom_api_url', defaultUrl);
  notificationStore.showNotification('API地址已重置为默认值', 'check');
}

function clearLocalStorage() {
  localStorage.clear();
  window.location.reload();
}

watch(() => themeStore.isAutoDark, (newValue) => {
  isMstChecked.value = newValue;
});
</script>

<template>
  <h1>Settings</h1>
  <div class="main-div">
    <div class="cards-div">
      <div class="card">
        <h2>API 配置</h2>
        <div style="display: flex; flex-direction: column; gap: 1rem; width: 100%; max-width: 400px;">
          <label>服务器地址:</label>
          <input 
            v-model="apiUrl" 
            type="text" 
            placeholder="例: http://192.168.1.100:5001/api/v1"
            style="padding: 0.5rem; border: 1px solid var(--border-color); border-radius: 4px;"
          />
          <div style="display: flex; gap: 0.5rem;">
            <button @click="saveApiUrl" style="padding: 0.5rem 1rem; background: var(--primary-color); color: white; border: none; border-radius: 4px; cursor: pointer;">
              保存
            </button>
            <button @click="resetApiUrl" style="padding: 0.5rem 1rem; background: #f59e0b; color: white; border: none; border-radius: 4px; cursor: pointer;">
              重置
            </button>
          </div>
          <p style="font-size: 0.8rem; color: var(--text-secondary); margin: 0;">
            * 修改后需要刷新页面才能生效。Android设备请使用实际IP地址而非localhost
          </p>
        </div>
      </div>

      <div class="card">
        <h2>Theme Settings</h2>
        <ThemeDisplay v-for="theme in themeStore.colorThemes" :key="theme.name" :theme="theme"
          @click="themeStore.setColorTheme(theme.name)"
          :class="{ 'selected': theme.name === themeStore.currentColorTheme }" />

        <button @click="themeStore.toggleLightDark" class="icons">
          {{ themeStore.isDark ? 'light_mode' : 'dark_mode' }}</button>
        <label class="checkbox-label">
          <input type="checkbox" v-model="isMstChecked" @change="mstChecked" />
          Match System Theme
        </label>
      </div>

      <div class="card">
        <h2>Local Data</h2>
        <button @click="clearLocalStorage"><span class="iconspan">delete_forever</span>Clear Local Storage</button>
        <p>*This will remove all locally stored data, including your preferences and sign in status, and cannot be
          undone.
        </p>
      </div>
    </div>
  </div>
</template>


<style scoped>
.card {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.cards-div {
  max-width: 600px;
  width: 90%;
}

.checkbox-label {
  display: flex;
  align-items: center;
  margin-top: 10px;
  cursor: pointer;
}

.checkbox-label input[type="checkbox"] {
  margin-right: 8px;
  width: 18px;
  height: 18px;
  cursor: pointer;
}

/* 自定义checkbox样式，确保在所有浏览器上都能正确显示 */
.checkbox-label input[type="checkbox"] {
  appearance: none;
  -webkit-appearance: none;
  -moz-appearance: none;
  width: 18px;
  height: 18px;
  border: 2px solid var(--border-color);
  border-radius: 3px;
  background-color: var(--background-color);
  margin-right: 8px;
  position: relative;
  cursor: pointer;
  transition: all 0.3s ease;
}

.checkbox-label input[type="checkbox"]:checked {
  background-color: var(--primary-color);
  border-color: var(--primary-color);
}

.checkbox-label input[type="checkbox"]:checked::after {
  content: '✓';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: white;
  font-size: 12px;
  font-weight: bold;
}

.checkbox-label input[type="checkbox"]:hover {
  border-color: var(--primary-color);
}
</style>
