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

        <span class="text-subtitle-2 font-weight-bold">{{ message.name }}</span>
        <span class="text-caption text-medium-emphasis">{{ message.time }}</span>
      </div>

      <v-sheet
        rounded="lg"
        :border
        class="pa-2 px-4"
        :color="message.isOwn ? 'primary' : 'surface'"
      >

      <p
        class="d-flex flex-column ga-1"
        :style="{  alignItems: message.isOwn ? 'flex-end' : 'flex-start' }"
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
