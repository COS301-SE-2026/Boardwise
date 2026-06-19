<template>
  <div class="d-flex flex-column ga-6">

    <BaseImage 
      :src="rulebook.coverImage" 
      :alt="rulebook.title" 
      width="200px"
      height="200px"
      fit="cover"
      style="border-radius: 16px;"
    />

    <RulebookMeta :rulebook="rulebook" />
    <RulebookDescription :rulebook="rulebook" />
    <RulebookActions :rulebook="rulebook" />
    <RulebookYouMightLike 
      :rulebooks="rulebooks" 
      :current-id="rulebook.id"
      @select="$emit('select', $event)"
    />
  </div>
</template>

<script setup>
import BaseImage from '~/components/ui/BaseImage.vue'
import RulebookYouMightLike from './RulebookYouMightLike.vue'
import RulebookMeta from './RulebookMeta.vue'
import RulebookDescription from './RulebookDescription.vue'
import RulebookActions from './RulebookActions.vue'

defineProps({
  rulebook: {
    type: Object,
    required: true
  },

  rulebooks: {
    type: Array,
    default: () => []
  }
})

defineEmits(['select'])
</script>