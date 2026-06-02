<template>
  <div>

    <ContentSection>
      <SpecificItemHero :listing="listing" />
    </ContentSection>

    <ContentSection>
      <SpecificItemGameInfo :rulebook="rulebook" />
    </ContentSection>

    <ContentSection v-if="rulebook">
      <SpecificItemRulebook :rulebook="rulebook" />
    </ContentSection>


  </div>
</template>

<script setup>
import { computed } from 'vue'
import { rulebooks } from '~/services/mockData/rulebooks'
import ContentSection from '~/components/layout/ContentSection.vue'
import SpecificItemHero from './SpecificItemHero.vue'
import SpecificItemGameInfo from './SpecificItemGameInfo.vue'
import SpecificItemRulebook from './SpecificItemRulebook.vue'


const props = defineProps ({
    listing: {
        type: Object,
        required: true
    }
})

const rulebook = computed(() =>
  rulebooks.find(r => r.id === props.listing.rulebookId) ?? null
)
</script>

<style scoped></style>