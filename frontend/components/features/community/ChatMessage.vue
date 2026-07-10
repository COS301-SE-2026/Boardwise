<template>
  <div class="d-flex ga-3 align-start" 
  :class="{ 'flex-row-reverse': message.isOwn }"
  >

    <BaseAvatar 
    :src="message.avatar"
    :name="message.name"
     size="md"/>

    <div class="d-flex flex-column ga-1"
    :class="{ 'align-end':message.isOwn }"
    style="max-width: 60%;"
    >

      <div class="d-flex ga-2 align-baseline" 
        :class="{ 'flex-row-reverse': message.isOwn }"
        >

        <span class="message__name">{{ message.name }}</span>
        <span class="message__time">{{ message.time }}</span>
      </div>

      <v-sheet
        rounded="lg"
        :border
        class="pa-2 px-4"
        :color="message.isOwn ? 'primary' : 'surface'"
      >

      <p
        class="ma-0"
        :class="message.isOwn ? 'text-white' : 'text-medium-emphasis'"
       >
        {{ message.text }}
      </p>
      </v-sheet>

    </div>

  </div>
</template>

<script setup>
import BaseAvatar from '~/components/ui/BaseAvatar.vue'

defineProps({
  message: {
    type: Object,
    required: true
  }
})
</script>

<style scoped>
.align-end {
  align-items: flex-start;
}

.message-name {
  font-size: var(--fs-small);
  font-weight: var(--fw-bold);
  color: var(--color-text);
}

.message-time {
  font-size: var(--fs-small);
  color: var(--color-text-muted);
}
</style>