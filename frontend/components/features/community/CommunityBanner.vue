<template>
  <header class="community-chat-header">
    <button
      type="button"
      class="community-chat-header__identity community-chat-header__details-trigger"
      :aria-label="`View details for ${community.name}`"
      @click="$emit('details')"
    >
      <BaseImage
        :src="community.imageUrl"
        :alt="`${community.name} community image`"
        width="56"
        height="56"
        class="community-chat-header__image"
      />

      <div class="community-chat-header__content">
        <div class="community-chat-header__title-row">
          <h1 class="community-chat-header__title">
            {{ community.name }}
          </h1>

          <BaseBadge
            class="community-chat-header__visibility"
            :variant="community.visibility"
          >
            {{ community.visibility }}
          </BaseBadge>
        </div>

        <p class="community-chat-header__meta">
          <v-icon
            icon="mdi-account-group-outline"
            size="16"
            aria-hidden="true"
          />

          {{ community.memberCount }}
          {{ community.memberCount === 1 ? 'member' : 'members' }}
        </p>
      </div>
      <v-icon
        icon="mdi-chevron-right"
        class="community-chat-header__chevron"
        aria-hidden="true"
      />
    </button>

      <div v-if="community.isOwner" class="community-chat-header__actions">
      <BaseButton
        variant="secondary"
        @click="showEdit = true"
      >
        <v-icon
          icon="mdi-pencil-outline"
          class="me-2"
          aria-hidden="true"
        />

        Edit
      </BaseButton>
      </div>

    <CommunityEditModal
      v-model="showEdit"
      :community="community"
      @save="emit('updated', $event)"
    />
  </header>
</template>

<script setup>
import { ref } from 'vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import CommunityEditModal from './CommunityEditModal.vue'
import BaseImage from '~/components/ui/BaseImage.vue'

import BaseBadge from '~/components/ui/BaseBadge.vue'

const emit = defineEmits([
  'details',
  'updated'
])

defineProps({
  community: { type: Object, required: true }
})

const showEdit = ref(false)

</script>

