<template>
  <div class="image-upload">
    <div class="upload-area" @click="triggerFileInput" @drop="handleDrop" @dragover.prevent @dragenter.prevent>
      <input 
        ref="fileInput" 
        type="file" 
        accept="image/jpeg,image/jpg,image/png,image/bmp" 
        @change="handleFileSelect" 
        style="display: none;"
      >
      
      <div v-if="!previewUrl" class="upload-placeholder">
        <span class="material-icons upload-icon">cloud_upload</span>
        <p>点击或拖拽图片到此处</p>
        <p class="upload-hint">支持 JPG、PNG、BMP 格式，最大10MB</p>
      </div>
      
      <div v-else class="preview-container">
        <img :src="previewUrl" alt="预览图片" class="preview-image">
        <button @click.stop="clearImage" class="clear-btn">
          <span class="material-icons">close</span>
        </button>
      </div>
    </div>
    
    <button 
      v-if="selectedFile" 
      @click="uploadImage" 
      :disabled="isUploading"
      class="upload-btn"
    >
      <span v-if="isUploading" class="material-icons spinning">hourglass_empty</span>
      <span v-else class="material-icons">send</span>
      {{ isUploading ? '检测中...' : '开始检测' }}
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useNotificationStore } from '../stores/notification';

const notificationStore = useNotificationStore();

interface Props {
  isUploading?: boolean;
}

interface Emits {
  (e: 'upload', file: File): void;
}

const { isUploading = false } = defineProps<Props>();

const emit = defineEmits<Emits>();

const fileInput = ref<HTMLInputElement>();
const selectedFile = ref<File | null>(null);
const previewUrl = ref<string>('');

const triggerFileInput = () => {
  fileInput.value?.click();
};

const handleFileSelect = (event: Event) => {
  const target = event.target as HTMLInputElement;
  const file = target.files?.[0];
  if (file) {
    if (validateFile(file)) {
      setSelectedFile(file);
    }
  }
};

const handleDrop = (event: DragEvent) => {
  event.preventDefault();
  const file = event.dataTransfer?.files[0];
  if (file && validateFile(file)) {
    setSelectedFile(file);
  }
};

const validateFile = (file: File): boolean => {
  // 检查文件类型
  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/bmp'];
  if (!allowedTypes.includes(file.type)) {
    notificationStore.showNotification('请选择支持的图片格式：JPG、PNG、BMP', 'error');
    return false;
  }
  
  // 检查文件大小 (最大10MB)
  const maxSize = 10 * 1024 * 1024;
  if (file.size > maxSize) {
    notificationStore.showNotification('图片文件大小不能超过10MB', 'error');
    return false;
  }
  
  return true;
};

const setSelectedFile = (file: File) => {
  selectedFile.value = file;
  
  // 创建预览URL
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value);
  }
  previewUrl.value = URL.createObjectURL(file);
};

const clearImage = () => {
  selectedFile.value = null;
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value);
    previewUrl.value = '';
  }
  if (fileInput.value) {
    fileInput.value.value = '';
  }
};

const uploadImage = () => {
  if (selectedFile.value) {
    emit('upload', selectedFile.value);
  }
};

// 清理函数
const cleanup = () => {
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value);
  }
};

// 在组件卸载时清理
import { onUnmounted } from 'vue';
onUnmounted(cleanup);
</script>

<style scoped>
.image-upload {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  max-width: 500px;
  margin: 0 auto;
}

.upload-area {
  border: 2px dashed var(--border-color);
  border-radius: 12px;
  padding: 2rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  min-height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--surface-color);
}

.upload-area:hover {
  border-color: var(--primary-color);
  background: var(--surface-hover-bg);
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  color: var(--text-secondary);
}

.upload-icon {
  font-size: 3rem;
  color: var(--primary-color);
}

.upload-hint {
  font-size: 0.9rem;
  opacity: 0.7;
  margin: 0;
}

.preview-container {
  position: relative;
  width: 100%;
  max-width: 400px;
  margin: 0 auto;
}

.preview-image {
  width: 100%;
  height: auto;
  max-height: 300px;
  object-fit: contain;
  border-radius: 8px;
}

.clear-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s ease;
}

.clear-btn:hover {
  background: rgba(0, 0, 0, 0.8);
}

.clear-btn .material-icons {
  font-size: 18px;
}

.upload-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 1rem 2rem;
  background: var(--primary-color);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.upload-btn:hover:not(:disabled) {
  background: var(--primary-hover);
  transform: translateY(-2px);
}

.upload-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
}

.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
