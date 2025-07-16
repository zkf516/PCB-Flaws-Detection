<script setup lang="ts">
import { ref } from 'vue';
import { usePCBStore } from '../stores/pcb';
import CameraUpload from '../components/CameraUpload.vue';
import DetectionResult from '../components/DetectionResult.vue';

const pcbStore = usePCBStore()

const uploadedImageUrl = ref<string>('')

const handleImageUpload = async (file: File) => {
  try {
    // 创建图片预览URL
    if (uploadedImageUrl.value) {
      URL.revokeObjectURL(uploadedImageUrl.value)
    }
    uploadedImageUrl.value = URL.createObjectURL(file)

    // 上传图片进行检测
    await pcbStore.uploadImage(file)

    // 检测成功后，清理blob URL并使用标注后的图片URL
    if (pcbStore.currentResult) {
      if (uploadedImageUrl.value.startsWith('blob:')) {
        URL.revokeObjectURL(uploadedImageUrl.value)
      }
      uploadedImageUrl.value = pcbStore.currentResult.annotated_image_url
    }
  } catch (error) {
    console.error('上传失败:', error)
    // 清理预览URL
    if (uploadedImageUrl.value && uploadedImageUrl.value.startsWith('blob:')) {
      URL.revokeObjectURL(uploadedImageUrl.value)
    }
    uploadedImageUrl.value = ''
  }
}

const handleClearResult = () => {
  pcbStore.clearCurrentResult();
  if (uploadedImageUrl.value) {
    // 只有当URL是blob时才需要释放
    if (uploadedImageUrl.value.startsWith('blob:')) {
      URL.revokeObjectURL(uploadedImageUrl.value);
    }
    uploadedImageUrl.value = '';
  }
};
</script>

<template>
  <div class="home-container">
    <div class="header-section">
      <h1>
        <span class="material-icons">precision_manufacturing</span>
        PCB 缺陷检测
      </h1>
      <p class="subtitle">拍照或上传PCB图片，智能检测电路板缺陷</p>
    </div>

    <!-- 上传区域 -->
    <div v-if="!pcbStore.hasResult" class="upload-section">
      <CameraUpload :isUploading="pcbStore.isLoading" @upload="handleImageUpload" />
    </div>

    <!-- 检测结果 -->
    <div v-if="pcbStore.hasResult && pcbStore.currentResult" class="result-section">
      <DetectionResult :result="pcbStore.currentResult" :imageUrl="uploadedImageUrl" @clear="handleClearResult" />
    </div> <!-- 加载状态 -->
    <div v-if="pcbStore.isLoading" class="loading-overlay">
      <div class="loading-content">
        <span class="material-icons spinning">hourglass_empty</span>
        <p>正在分析PCB图片...</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home-container {
  margin: 0 auto;
  padding: 2rem 1rem;
  padding-bottom: 4rem;
}

.header-section {
  text-align: center;
  margin-bottom: 3rem;
}

.header-section h1 {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  margin: 0 0 1rem 0;
  color: var(--text-color);
  font-size: 2.5rem;
  font-weight: 700;
}

@media (max-width: 768px) {
  .header-section h1 {
    font-size: 2rem;
  }
}

.header-section h1 .material-icons {
  font-size: 2.5rem;
  color: var(--primary-color);
}

@media (max-width: 768px) {
  .header-section h1 .material-icons {
    font-size: 2rem;
  }
}

.subtitle {
  color: var(--text-secondary);
  font-size: 1.1rem;
  margin: 0;
  max-width: 500px;
  margin: 0 auto;
}

.error-message {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  background: #fee2e2;
  color: #dc2626;
  padding: 1.5rem;
  border-radius: 8px;
  margin-bottom: 2rem;
  border: 1px solid #fecaca;
}

.error-message .material-icons {
  font-size: 1.5rem;
  margin-top: 0.2rem;
  flex-shrink: 0;
}

.error-content {
  flex: 1;
}

.error-content strong {
  display: block;
  margin-bottom: 0.5rem;
  font-size: 1.1rem;
}

.error-content p {
  margin: 0 0 1rem 0;
  line-height: 1.5;
}

.upload-section {
  margin-bottom: 2rem;
}

.result-section {
  margin-bottom: 2rem;
}

.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.loading-content {
  background: var(--surface-color);
  padding: 2rem;
  border-radius: 12px;
  text-align: center;
  max-width: 300px;
  margin: 0 1rem;
}

.loading-content .material-icons {
  font-size: 3rem;
  color: var(--primary-color);
  margin-bottom: 1rem;
  display: block;
}

.loading-content p {
  margin: 0;
  color: var(--text-color);
  font-size: 1.1rem;
}

.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }

  to {
    transform: rotate(360deg);
  }
}
</style>
