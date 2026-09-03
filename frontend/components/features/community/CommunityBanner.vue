<template>
<header class="community-chat-header">
    <div class="community-chat-header__identity">
      <BaseImage
        :src="community.imageUrl"
        :alt="community.name"
        width="56"
        height="56"
        class="community-chat-header__image"
      />

      <div class="community-chat-header__content">
        <div class="d-flex align-center flex-wrap ga-2">
          <h1 class="community-chat-header__title">
            {{ community.name }}
          </h1>

          <BaseBadge :variant="community.visibility">
            {{ community.visibility }}
          </BaseBadge>
        </div>

        <p class="community-chat-header__meta">
          <v-icon
            icon="mdi-account-group-outline"
            size="16"
            aria-hidden="true"
          />

          {{ community.memberCount }} members
        </p>
      </div>
    </div>

    <div class="community-chat-header__actions">
      <BaseButton
        variant="secondary"
        @click="$emit('details')"
      >
        <v-icon
          icon="mdi-information-outline"
          class="me-2"
          aria-hidden="true"
        />

        Details
      </BaseButton>

      <BaseButton
        v-if="community.isOwner"
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

const props = defineProps({
  community: { type: Object, required: true }
})

const showEdit = ref(false)

</script>

