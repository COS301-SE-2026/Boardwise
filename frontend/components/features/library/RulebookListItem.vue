<template>
  <BaseButton
    type="button"
    class="rulebook-list-item"
    :aria-label="`Open ${rulebook.title}`"
    @click="$emit('select', rulebook)"
  >
    <div class="rulebook-list-item__cover">
      <BaseImage
        :src="rulebook.coverUrl"
        :alt="rulebook.title"
        height="92"
        width="72"
        cover
      />
    </div>

    <div class="rulebook-list-item__content">
      <h3 class="rulebook-list-item__title">
        {{ rulebook.title }}
      </h3>

      <p
        v-if="rulebook.genre"
        class="rulebook-list-item__meta"
      >
        {{ rulebook.genre }}
      </p>

      <p
        v-if="rulebook.description"
        class="rulebook-list-item__description"
      >
        {{ rulebook.description }}
      </p>
    </div>

    <v-icon
      icon="mdi-chevron-right"
      aria-hidden="true"
      class="rulebook-list-item__arrow"
    />
  </BaseButton>
</template>

<script setup>
import BaseButton from '~/components/ui/BaseButton.vue';
import BaseImage from '~/components/ui/BaseImage.vue'

defineProps({
  rulebook: {
    type: Object,
    required: true
  }
})

defineEmits(['select'])
</script>