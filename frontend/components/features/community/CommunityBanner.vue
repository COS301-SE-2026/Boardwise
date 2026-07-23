<template>
  <BaseCard class="pa-8">

    <div class="d-flex justify-space-between align-center flex-wrap ga-6">

      <div class="d-flex align-center ga-6 flex-wrap">
          <BaseImage
            :src="community.imageUrl" 
            :alt="community.name"
            width="100"
            height="100"
            class="rounded-lg"
          />

        <div class="d-flex flex-column ga-3">
          <h1 class="mb-0">
            {{ community.name }}
          </h1>
        
          <BaseBadge
            class="badge"
            :variant="community.visibility"
          >
            {{ community.visibility }}
          </BaseBadge>
          
          
          <p class="text-body-2 text-medium-emphasis mb-0">
            {{ community.description }}
          </p>

          <div class="d-flex flex-wrap ga-2">
            
            <BaseButton variant="secondary" @click="$emit('members')">
              Members ({{ community.memberCount }})
            </BaseButton>

            <BaseButton variant="secondary" @click="$emit('events')">
              Events 
            </BaseButton>
          </div>
        </div>
      </div>


      <div v-if="community.isOwner" class="d-flex ga-3">

        <BaseButton @click="showEdit = true">
          Edit community
        </BaseButton>
      </div>
    </div>

    <CommunityEditModal
      v-model="showEdit"
      :community="community"
      @save="emit('updated', $event)"
    />
  </BaseCard>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import CommunityEditModal from './CommunityEditModal.vue'
import BaseImage from '~/components/ui/BaseImage.vue'

import BaseBadge from '~/components/ui/BaseBadge.vue'

const emit = defineEmits([
  'members',
  'events',
  'updated'
])

const props = defineProps({
  community: { type: Object, required: true }
})

const showEdit = ref(false)
console.log(props.community.visibility)

</script>

