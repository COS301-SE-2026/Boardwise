<template>
  <atricle class="rulebook-detail">

    <BaseImage 
      :src="rulebook.coverUrl" 
      :alt="rulebook.title" 
      height="200px"
    />

    <RulebookMeta :rulebook="rulebook" />
    <RulebookDescription :rulebook="rulebook" />
    <RulebookActions :rulebook="rulebook" />
    <RulebookYouMightLike 
      :rulebooks="rulebooks" 
      :current-id="rulebook.id"
      @select="$emit('select', $event)"
    />
  </atricle>
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