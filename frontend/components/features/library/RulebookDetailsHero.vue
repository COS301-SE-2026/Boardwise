<template>
  <v-row class="mt-12" align="start">

    <v-col cols="12" md="4">
      <div style="position: sticky; top: 100px;">
        <BaseImage
          :src="rulebook.image"
          :alt="rulebook.title"
          height="480px"
          fit="cover"
          style="border-radius: 24px; box-shadow: 0 20px 40px rgba(0,0,0,0.15);"
        />
      </div>
    </v-col>

    <v-col cols="12" md="8">
      <div class="d-flex flex-column ga-7">
        <RulebookMeta :rulebook="rulebook" />
        <RulebookDescription :rulebook="rulebook" />
        <RulebookActions :rulebook="rulebook" />
      </div>
    </v-col>

  </v-row>
</template>

<script setup>
import BaseImage from '~/components/ui/BaseImage.vue'
import RulebookMeta from './RulebookMeta.vue'
import RulebookDescription from './RulebookDescription.vue'
import RulebookActions from './RulebookActions.vue'

defineProps({
  rulebook: {
    type: Object,
    required: true
  }
})
</script>