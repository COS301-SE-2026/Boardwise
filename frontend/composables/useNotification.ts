import { ref } from 'vue'

export interface Notification {
  id: number
  type: 'success' | 'error' | 'warning' | 'info'
  message: string
  description?: string
  duration?: number
  action?: {
    label: string
    onClick: () => void
  }
}

const notifications = ref<Notification[]>([])
const idCounter = ref(0)

export const useNotification = () => {
  const addNotification = (notification: Omit<Notification, 'id'>) => {
    const id = idCounter.value++
    const newNotification = {
      ...notification,
      id,
      duration: notification.duration || 3000
    }
    notifications.value.push(newNotification)

    setTimeout(() => {
      removeNotification(id)
    }, newNotification.duration)

    return id
  }

  const removeNotification = (id: number) => {
    const index = notifications.value.findIndex(n => n.id === id)
    if (index !== -1) {
      notifications.value.splice(index, 1)
    }
  }

  const clearNotifications = () => {
    notifications.value = []
  }

  const success = (message: string, description?: string) => {
    return addNotification({ type: 'success', message, description })
  }

  const error = (message: string, description?: string) => {
    return addNotification({ type: 'error', message, description })
  }

  const warning = (message: string, description?: string) => {
    return addNotification({ type: 'warning', message, description })
  }

  const info = (message: string, description?: string) => {
    return addNotification({ type: 'info', message, description })
  }

  return {
    notifications,
    addNotification,
    removeNotification,
    clearNotifications,
    success,
    error,
    warning,
    info
  }
}