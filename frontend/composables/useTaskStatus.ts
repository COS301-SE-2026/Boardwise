import { computed } from 'vue'

export type BackgroundTaskState =
  | 'queued'
  | 'processing'
  | 'completed'
  | 'failed'

export interface BackgroundTask {
  id: string
  title: string
  description: string
  state: BackgroundTaskState
  progress?: number
  route?: string
  updatedAt: string
  error?: string
}

const STORAGE_KEY = 'boardwise:background-tasks'

export const useTaskStatus = () => {
  const tasks = useState<BackgroundTask[]>(
    'boardwise-background-tasks',
    () => []
  )

  const hydrated = useState(
    'boardwise-background-tasks-hydrated',
    () => false
  )

  const persistTasks = () => {
    if (!import.meta.client) return

    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify(tasks.value)
    )
  }

  const hydrateTasks = () => {
    if (!import.meta.client || hydrated.value) return

    hydrated.value = true

    const storedTasks = localStorage.getItem(STORAGE_KEY)
    if (!storedTasks) return

    try {
      const parsedTasks = JSON.parse(storedTasks)

      if (Array.isArray(parsedTasks)) {
        tasks.value = parsedTasks
      }
    } catch {
      localStorage.removeItem(STORAGE_KEY)
    }
  }

  const upsertTask = (
    task: Omit<BackgroundTask, 'updatedAt'> & {
      updatedAt?: string
    }
  ) => {
    const nextTask: BackgroundTask = {
      ...task,
      updatedAt: task.updatedAt ?? new Date().toISOString()
    }

    const existingIndex = tasks.value.findIndex(
      existingTask => existingTask.id === task.id
    )

    if (existingIndex === -1) {
      tasks.value = [nextTask, ...tasks.value]
    } else {
      tasks.value = tasks.value.map((existingTask, index) =>
        index === existingIndex
          ? { ...existingTask, ...nextTask }
          : existingTask
      )
    }

    persistTasks()
  }

  const updateTask = (
    taskId: string,
    changes: Partial<Omit<BackgroundTask, 'id'>>
  ) => {
    const existingTask = tasks.value.find(
      task => task.id === taskId
    )

    if (!existingTask) return

    upsertTask({
      ...existingTask,
      ...changes,
      id: taskId,
      updatedAt: new Date().toISOString()
    })
  }

  const removeTask = (taskId: string) => {
    tasks.value = tasks.value.filter(
      task => task.id !== taskId
    )

    persistTasks()
  }

  const clearFinishedTasks = () => {
    tasks.value = tasks.value.filter(
      task =>
        task.state === 'queued' ||
        task.state === 'processing'
    )

    persistTasks()
  }

  const activeTasks = computed(() =>
    tasks.value.filter(
      task =>
        task.state === 'queued' ||
        task.state === 'processing'
    )
  )

  const activeCount = computed(
    () => activeTasks.value.length
  )

  const hasFinishedTasks = computed(() =>
    tasks.value.some(
      task =>
        task.state === 'completed' ||
        task.state === 'failed'
    )
  )

  return {
    tasks,
    activeTasks,
    activeCount,
    hasFinishedTasks,
    hydrateTasks,
    upsertTask,
    updateTask,
    removeTask,
    clearFinishedTasks
  }
}