<template>
  <Teleport to="body">
    <div class="notification-container">
      <div
        v-for="notification in notifications"
        :key="notification.id"
        class="notification-toast"
        :class="notification.type"
        @click="removeNotification(notification.id)"
      >
        <div class="notification-icon">
          <v-icon :color="getIconColor(notification.type)">
            {{ getIcon(notification.type) }}
          </v-icon>
        </div>
        <div class="notification-content">
          <div class="notification-title">{{ notification.message }}</div>
          <div v-if="notification.description" class="notification-description">
            {{ notification.description }}
          </div>
        </div>
        <button class="notification-close" @click.stop="removeNotification(notification.id)">
          <v-icon size="16">mdi-close</v-icon>
        </button>
      </div>
    </div>
  </Teleport>
</template>


<script setup>
import { useNotification } from '~/composables/useNotification'

const { notifications, removeNotification } = useNotification()

const getIcon = (type: string) => {
  const icons = {
    success: 'mdi-check-circle',
    error: 'mdi-alert-circle',
    warning: 'mdi-alert',
    info: 'mdi-information'
  }
  return icons[type as keyof typeof icons] || 'mdi-information'
}

const getIconColor = (type: string) => {
  const colors = {
    success: 'success',
    error: 'error',
    warning: 'warning',
    info: 'info'
  }
  return colors[type as keyof typeof colors] || 'info'
}
</script>

<style scoped>
.notification-container {
  position: fixed;
  top: 80px;
  right: 20px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
  max-width: 400px;
  width: 100%;
  pointer-events: none;
}

.notification-toast {
  pointer-events: auto;
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
  padding: var(--space-4);
  background: var(--color-surface);
  border-radius: var(--radius-md);
  border: 1px solid var(--color-border);
  box-shadow: var(--shadow-lg);
  animation: slideIn 0.3s ease;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.notification-toast:hover {
  transform: translateX(-4px);
}

.notification-toast.success {
  border-left: 4px solid var(--color-success);
}

.notification-toast.error {
  border-left: 4px solid var(--color-error);
}

.notification-toast.warning {
  border-left: 4px solid var(--color-warning);
}

.notification-toast.info {
  border-left: 4px solid var(--color-info);
}

.notification-icon {
  flex-shrink: 0;
  margin-top: 2px;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-weight: var(--fw-bold);
  font-size: var(--fs-body);
  color: var(--color-text);
}

.notification-description {
  font-size: var(--fs-small);
  color: var(--color-text-muted);
  margin-top: var(--space-1);
}

.notification-close {
  flex-shrink: 0;
  background: none;
  border: none;
  color: var(--color-text-muted);
  cursor: pointer;
  padding: var(--space-1);
  border-radius: var(--radius-sm);
  transition: background var(--transition-base);
}

.notification-close:hover {
  background: var(--color-surface-alt);
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

@media (max-width: 768px) {
  .notification-container {
    top: 70px;
    right: 10px;
    left: 10px;
    max-width: none;
  }
}
</style>