<template>
  <div class="detection-result">
    <div class="result-header">
      <h3>
        <span class="material-icons">analytics</span>
        检测结果
      </h3>
      <button @click="$emit('clear')" class="clear-result-btn">
        <span class="material-icons">refresh</span>
        重新检测
      </button>
    </div>

    <div class="result-content">
      <!-- 图片显示区域 -->
      <div class="image-section">
        <div class="image-container">
          <img 
            :src="imageUrl" 
            alt="检测图片"
            class="result-image"
          >
        </div>
        <div class="image-info">
          <p class="image-note">
            <span class="material-icons">info</span>
            图片已自动标注检测到的缺陷位置
          </p>
        </div>
      </div>

      <!-- 检测统计 -->
      <div class="summary-section">
        <div class="summary-card">
          <div class="summary-item">
            <span class="material-icons summary-icon defect">error</span>
            <div>
              <div class="summary-number">{{ result.summary.total_defects }}</div>
              <div class="summary-label">总缺陷数</div>
            </div>
          </div>
        </div>

        <!-- 缺陷类型分布 -->
        <div v-if="defectTypes.length > 0" class="defect-types">
          <h4>缺陷类型分布</h4>
          <div class="defect-list">
            <div 
              v-for="[type, count] in defectTypes" 
              :key="type"
              class="defect-item"
            >
              <span class="defect-name">{{ type }}</span>
              <span class="defect-count">{{ count }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 详细检测结果 -->
      <div class="details-section">
        <h4>详细检测结果</h4>
        <div v-if="result.detection_results.length === 0" class="no-defects">
          <span class="material-icons">check_circle</span>
          <p>未检测到缺陷</p>
        </div>
        <div v-else class="detection-list">
          <div 
            v-for="(detection, index) in result.detection_results" 
            :key="index"
            class="detection-item"
          >
            <div class="detection-header">
              <span class="detection-class">{{ detection.class }}</span>
              <span class="detection-confidence">
                置信度: {{ (detection.confidence * 100).toFixed(1) }}%
              </span>
            </div>
            <div class="detection-details">
              <div class="detection-bbox">
                <span class="material-icons">crop_free</span>
                位置: [{{ detection.bbox.map(v => Math.round(v)).join(', ') }}]
              </div>
              <div class="confidence-bar">
                <div class="confidence-fill" :style="{ width: (detection.confidence * 100) + '%' }"></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 时间戳 -->
      <div class="timestamp">
        <span class="material-icons">schedule</span>
        检测时间: {{ formatTimestamp(result.timestamp) }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { InferenceResult } from '../services/pcbApi';

interface Props {
  result: InferenceResult;
  imageUrl: string;
}

interface Emits {
  (e: 'clear'): void;
}

const props = defineProps<Props>();
defineEmits<Emits>();

const defectTypes = computed(() => {
  return Object.entries(props.result.summary.defect_types);
});

const formatTimestamp = (timestamp: string) => {
  return new Date(timestamp).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  });
};
</script>

<style scoped>
.detection-result {
  background: var(--surface-color);
  border-radius: 12px;
  padding: 1.5rem;
  margin: 1rem 0;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 1rem;
}

.result-header h3 {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin: 0;
  color: var(--text-color);
}

.clear-result-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: transparent;
  color: var(--text-secondary);
  border: 1px solid var(--border-color);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.clear-result-btn:hover {
  background: var(--surface-hover-bg);
  color: var(--text-color);
}

.result-content {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1.5rem;
}

@media (min-width: 768px) {
  .result-content {
    grid-template-columns: 2fr 1fr;
  }
}

.image-section {
  order: 1;
}

.image-container {
  position: relative;
  display: inline-block;
  width: 100%;
}

.result-image {
  width: 100%;
  height: auto;
  max-height: 500px;
  object-fit: contain;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.image-info {
  margin-top: 1rem;
  text-align: center;
}

.image-note {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  color: var(--text-secondary);
  font-size: 0.9rem;
  margin: 0;
  padding: 0.5rem;
  background: var(--background-color);
  border-radius: 6px;
  border: 1px solid var(--border-color);
}

.image-note .material-icons {
  font-size: 1rem;
  color: var(--primary-color);
}

.summary-section {
  order: 2;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.summary-card {
  background: var(--background-color);
  border-radius: 8px;
  padding: 1rem;
  border: 1px solid var(--border-color);
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.summary-icon {
  font-size: 2rem;
}

.summary-icon.defect {
  color: #e74c3c;
}

.summary-number {
  font-size: 1.8rem;
  font-weight: bold;
  color: var(--text-color);
}

.summary-label {
  font-size: 0.9rem;
  color: var(--text-secondary);
}

.defect-types h4 {
  margin: 0 0 0.5rem 0;
  color: var(--text-color);
}

.defect-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.defect-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem;
  background: var(--background-color);
  border-radius: 6px;
  border: 1px solid var(--border-color);
}

.defect-name {
  font-weight: 500;
  color: var(--text-color);
}

.defect-count {
  background: var(--primary-color);
  color: white;
  padding: 0.2rem 0.5rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: bold;
}

.details-section {
  order: 3;
  grid-column: 1 / -1;
}

.details-section h4 {
  margin: 0 0 1rem 0;
  color: var(--text-color);
}

.no-defects {
  text-align: center;
  padding: 2rem;
  color: var(--text-secondary);
}

.no-defects .material-icons {
  font-size: 3rem;
  color: #27ae60;
  margin-bottom: 0.5rem;
  display: block;
}

.detection-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.detection-item {
  padding: 1.5rem;
  background: var(--background-color);
  border-radius: 8px;
  border: 1px solid var(--border-color);
  transition: all 0.3s ease;
}

.detection-item:hover {
  background: var(--surface-hover-bg);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.detection-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.detection-class {
  font-weight: bold;
  color: var(--text-color);
  font-size: 1.1rem;
  background: var(--primary-color);
  color: white;
  padding: 0.3rem 0.8rem;
  border-radius: 12px;
  font-size: 0.9rem;
}

.detection-confidence {
  color: var(--text-secondary);
  font-size: 0.9rem;
  font-weight: 500;
}

.detection-details {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
}

.detection-bbox {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  color: var(--text-secondary);
  font-family: monospace;
}

.detection-bbox .material-icons {
  font-size: 1rem;
  color: var(--primary-color);
}

.confidence-bar {
  width: 100%;
  height: 6px;
  background: var(--border-color);
  border-radius: 3px;
  overflow: hidden;
}

.confidence-fill {
  height: 100%;
  background: linear-gradient(90deg, #e74c3c 0%, #f39c12 50%, #27ae60 100%);
  border-radius: 3px;
  transition: width 0.3s ease;
}

.timestamp {
  order: 4;
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--text-secondary);
  font-size: 0.9rem;
  padding-top: 1rem;
  border-top: 1px solid var(--border-color);
}

@media (max-width: 767px) {
  .summary-section {
    order: 2;
  }
  
  .details-section {
    order: 3;
  }
  
  .timestamp {
    order: 4;
  }
}
</style>
