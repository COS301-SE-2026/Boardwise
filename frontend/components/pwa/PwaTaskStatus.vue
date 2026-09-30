<template>
  <div v-if="tasks.length" class="pwa-task-status">
    <button
      type="button"
      class="pwa-task-status__trigger"
      :aria-label="triggerLabel"
      @click="showTasks = true"
    >
      <v-progress-circular
        v-if="activeCount"
        indeterminate
        :size="20"
        :width="2"
        color="primary"
        aria-hidden="true"
      />

      <v-icon
        v-else
        icon="mdi-check-circle-outline"
        aria-hidden="true"
      />

      <span>
        {{
          activeCount
            ? `${activeCount} task${activeCount === 1 ? '' : 's'} in progress`
            : 'Tasks completed'
        }}
      </span>
    </button>

    <v-bottom-sheet v-model="showTasks">
      <BaseCard class="pwa-task-status__sheet">
        <header class="pwa-task-status__header">
          <div>
            <h2>Task status</h2>
            <p>
              Long-running Boardwise activity continues here.
            </p>
          </div>

          <button
            type="button"
            class="pwa-task-status__close"
            aria-label="Close task status"
            @click="showTasks = false"
          >
            <v-icon icon="mdi-close" aria-hidden="true" />
          </button>
        </header>

        <ul class="pwa-task-status__list">
          <li
            v-for="task in tasks"
            :key="task.id"
            class="pwa-task-status__item"
          >
            <div class="pwa-task-status__item-header">
              <v-icon
                :icon="statusIcon(task.state)"
                :color="statusColour(task.state)"
                aria-hidden="true"
              />

              <div class="pwa-task-status__item-content">
                <strong>{{ task.title }}</strong>
                <span>{{ task.description }}</span>
              </div>

              <button
                v-if="
                  task.state === 'completed' ||
                  task.state === 'failed'
                "
                type="button"
                class="pwa-task-status__remove"
                :aria-label="`Remove ${task.title}`"
                @click="removeTask(task.id)"
              >
                <v-icon
                  icon="mdi-close"
                  size="small"
                  aria-hidden="true"
                />
              </button>
            </div>

            <v-progress-linear
              v-if="task.state === 'processing'"
              :model-value="task.progress"
              :indeterminate="task.progress === undefined"
              color="primary"
              rounded
              height="6"
              :aria-label="progressLabel(task)"
            />

            <p
              v-if="task.error"
              class="pwa-task-status__error"
              role="alert"
            >
              {{ task.error }}
            </p>

            <NuxtLink
              v-if="task.route"
              :to="task.route"
              class="pwa-task-status__link"
              @click="showTasks = false"
            >
              View task
            </NuxtLink>
          </li>
        </ul>

        <footer
          v-if="hasFinishedTasks"
          class="pwa-task-status__footer"
        >
          <BaseButton
            variant="secondary"
            @click="clearFinishedTasks"
          >
            Clear finished
          </BaseButton>
        </footer>
      </BaseCard>
    </v-bottom-sheet>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'

import BaseButton from '~/components/ui/BaseButton.vue'
import BaseCard from '~/components/ui/BaseCard.vue'
import type {
  BackgroundTask,
  BackgroundTaskState
} from '~/composables/useTaskStatus'

const showTasks = ref(false)

const {
  tasks,
  activeCount,
  hasFinishedTasks,
  hydrateTasks,
  removeTask,
  clearFinishedTasks
} = useTaskStatus()

onMounted(hydrateTasks)

const triggerLabel = computed(() =>
  activeCount.value
    ? `Open task status. ${activeCount.value} tasks in progress.`
    : 'Open completed task status.'
)

const statusIcon = (state: BackgroundTaskState) => {
  const icons: Record<BackgroundTaskState, string> = {
    queued: 'mdi-clock-outline',
    processing: 'mdi-progress-clock',
    completed: 'mdi-check-circle-outline',
    failed: 'mdi-alert-circle-outline'
  }

  return icons[state]
}

const statusColour = (state: BackgroundTaskState) => {
  const colours: Record<BackgroundTaskState, string> = {
    queued: 'secondary',
    processing: 'primary',
    completed: 'success',
    failed: 'error'
  }

  return colours[state]
}

const progressLabel = (task: BackgroundTask) =>
  task.progress === undefined
    ? `${task.title} is processing`
    : `${task.title} is ${task.progress} percent complete`
</script>