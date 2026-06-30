<template>
  <BaseModal v-model="open">
    <div class="content">
      <h2>Create Event</h2>

      <div class="form">
        <div class="input-group">
          <label for="event-name">Event Name</label>
          <BaseInput id="event-name" v-model="name" placeholder="e.g. Catan Night" />
        </div>

        <div class="input-group">
          <label for="event-game">Game</label>
          <BaseInput id="event-game" v-model="game" placeholder="e.g. Catan" />
        </div>

        <div class="input-group">
          <label for="event-date">Date</label>
          <input id="event-date" type="date" v-model="date" class="date-input" />
        </div>

        <div class="input-group">
          <label for="event-time">Time</label>
          <input id="event-time" type="time" v-model="time" class="date-input" />
        </div>

        <div class="input-group">
          <label for="event-location">Location</label>
          <BaseInput id="event-location" v-model="location" placeholder="e.g. Pretoria, Online" />
        </div>

        <div class="input-group">
          <span class="label-text">Visibility</span>
          <div class="toggle-row">
            <button 
              :class="['toggle-btn', { active: visibility === 'Public' }]" 
              @click="visibility = 'Public'"
            >
              Public
            </button>
            <button 
              :class="['toggle-btn', { active: visibility === 'Private' }]" 
              @click="visibility = 'Private'"
            >
              Private
            </button>
          </div>
        </div>
      </div>

      <div class="actions">
        <BaseButton variant="secondary" @click="closeModal">Cancel</BaseButton>
        <BaseButton @click="handleCreate">Create Event</BaseButton>
      </div>
    </div>
  </BaseModal>
</template>

<script setup>
import { ref, watch } from 'vue'
import BaseModal from '~/components/ui/BaseModal.vue'
import BaseInput from '~/components/ui/BaseInput.vue'
import BaseButton from '~/components/ui/BaseButton.vue'

const props = defineProps({
  modelValue: Boolean,
  communityId: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'confirm'])

const open = ref(props.modelValue)
const name = ref('')
const game = ref('')
const date = ref('')
const time = ref('')
const location = ref('')
const visibility = ref('Public')

watch(() => props.modelValue, (val) => {
  open.value = val
})

watch(open, (val) => {
  emit('update:modelValue', val)
})

const closeModal = () => {
  open.value = false
  name.value = ''
  game.value = ''
  date.value = ''
  time.value = ''
  location.value = ''
  visibility.value = 'Public'
}

const handleCreate = () => {
  if (!name.value.trim() || !date.value) return
  emit('confirm', {
    id: Date.now(),
    name: name.value.trim(),
    game: game.value.trim() || 'Board Game',
    date: date.value,
    time: time.value || '18:00',
    location: location.value.trim() || 'TBD',
    visibility: visibility.value,
    communityId: props.communityId,
    organiser: 'You',
    attendees: ['You'],
    rsvped: true
  })
  closeModal()
}
</script>

<style scoped>
.content {
  display: flex;
  flex-direction: column;
  gap: var(--space-5);
}

.content h2 {
  margin: 0;
  font-size: var(--fs-h2);
  font-weight: var(--fw-bold);
  color: var(--color-secondary);
}

.form {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: var(--space-1);
}

.input-group label {
  font-size: var(--fs-small);
  font-weight: var(--fw-bold);
  color: var(--color-text);
}

.date-input {
  padding: var(--space-2) var(--space-3);
  border: 1px solid var(--color-border-strong);
  border-radius: var(--radius-md);
  font-size: var(--fs-body);
  background: var(--color-surface);
  color: var(--color-text);
}

.date-input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(109, 0, 55, 0.15);
}

.toggle-row {
  display: flex;
  gap: var(--space-3);
}

.toggle-btn {
  flex: 1;
  padding: var(--space-2) var(--space-4);
  border: 2px solid var(--color-border-strong);
  border-radius: var(--radius-md);
  background: var(--color-surface);
  cursor: pointer;
  font-size: var(--fs-body);
  font-weight: var(--fw-medium);
  color: var(--color-text-muted);
  transition: all var(--transition-base);
}

.toggle-btn.active {
  border-color: var(--color-primary);
  background: rgba(109, 0, 55, 0.06);
  color: var(--color-primary);
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  padding-top: var(--space-4);
  border-top: 1px solid var(--color-border);
}
</style>