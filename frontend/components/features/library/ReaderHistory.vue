<template>
    <v-navigation-drawer
        :model-value="modelValue"
        location="right"
        temporary
        width="420"
        scrollable
        @update:model-value="$emit('update:modelValue', $event)"
    >
        <div class="pa-6">
            <div class="d-flex justify-space-between align-center mb-6">
                <h2>Edit History</h2>
                <v-btn icon variant="text" @click="$emit('update:modelValue', false)">
                <v-icon>mdi-close</v-icon>
                </v-btn>
            </div>

            <v-alert
                v-if="error"
                type="error"
                variant="tonal"
                class="mb-4 text-caption"
                closable
            >
                {{ error }}
            </v-alert>

            <div v-if="isLoading" class="d-flex justify-center my-6">
                <v-progress-circular indeterminate color="primary"></v-progress-circular>
            </div>

            <div v-if="edits.length" class="d-flex flex-column ga-4">
                <div
                    v-for="edit in edits"
                    :key="edit.id"
                    class="history-entry card pa-4"
                >
                    <div class="d-flex justify-space-between align-center mb-2">
                        <div class="d-flex align-center ga-2">
                            <v-chip
                                size="x-small"
                                :color="editTypeColor(edit.editType)"
                                variant="tonal"
                            >
                                {{ edit.editType }}
                            </v-chip>
                            <span class="text-body-2 font-weight-bold">@{{ edit.editor }}</span>
                        </div>
                        <span class="text-caption text-medium-emphasis">
                            {{ formatDate(edit.committedAt) }}
                        </span>
                    </div>

                    <p class="text-caption text-medium-emphasis mb-0">
                        Section {{ edit.chunkId }}
                    </p>

                    <div class="diff-block mt-3">
                        <p v-if="edit.previousContent" class="diff-old text-caption mb-0">
                            {{ edit.previousContent }}
                        </p>

                        <p v-if="edit.newContent" class="diff-new text-caption mb-0">
                            {{ edit.newContent }}
                        </p>
                    </div>
                </div>
            </div>

            <BaseEmptyState
            v-else-if="!error"
            title="No edits yet"
            message="Changes made to this rulebook will appear here" 
            />
        </div>
    </v-navigation-drawer>
</template>

<script setup>
import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'

defineProps({
  modelValue: { type: Boolean, default: false },
  edits: { type: Array, default: () => [] },
  isLoading: {type: Boolean, deflault: false},
  error: {type: String, default: ''}
})

defineEmits(['update:modelValue'])

const editTypeColor = (type) => {
  if (type === 'INSERT') return 'success'
  if (type === 'DELETE') return 'error'
  if (type === 'UPDATE') return 'primary'
  return 'grey'
}

const formatDate = (iso) => {
  if (!iso) return ''
  return new Date(iso).toLocaleString([], {
    month: 'short', day: 'numeric',
    hour: '2-digit', minute: '2-digit'
  })
}
</script>

<style scoped>
.history-entry {
  border-left: 3px solid var(--color-border);
}

.diff-block {
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.diff-old {
  background: #ffeaea;
  color: var(--color-error);
  text-decoration: line-through;
  padding: var(--space-2) var(--space-3);
}

.diff-new {
  background: #eaffea;
  color: var(--color-success);
  padding: var(--space-2) var(--space-3);
}
</style>