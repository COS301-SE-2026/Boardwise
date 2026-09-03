<template>
  <PageContainer>
    <Navbar />

    <v-container
      v-if="community"
      class="community-detail-page"
    >
      <section
        class="community-chat-window"
        :aria-label="`${community.name} community chat`"
      >
        <CommunityBanner
          :community="community"
          @details="showDetails = true"
          @updated="handleUpdate"
        />

        <div class="community-chat-window__body">
          <CommunityChats
            :community="community"
            @join="handleJoin"
          />
        </div>
      </section>

      <v-dialog
        v-model="showDetails"
        max-width="860"
        scrollable
      >
        <v-card rounded="xl">
          <v-card-title class="d-flex align-center pa-5">
            <div>
              <p class="text-h6 font-weight-bold mb-0">
                {{ community.name }}
              </p>

              <p class="text-body-2 text-medium-emphasis mb-0">
                Community details
              </p>
            </div>

            <v-spacer />

            <v-btn
              icon="mdi-close"
              variant="text"
              aria-label="Close community details"
              @click="showDetails = false"
            />
          </v-card-title>

          <v-divider />

          <v-tabs
            v-model="detailsTab"
            color="primary"
            class="community-details-tabs"
          >
            <v-tab value="about">
              About
            </v-tab>

            <v-tab value="members">
              Members ({{ community.memberCount }})
            </v-tab>

            <v-tab value="events">
              Events
            </v-tab>
          </v-tabs>

          <v-divider />

          <v-card-text class="pa-5">
            <v-window v-model="detailsTab">
              <v-window-item value="about">
                <div class="community-details-about">
                  <h3 class="text-subtitle-1 font-weight-bold mb-2">
                    About this community
                  </h3>

                  <p class="text-body-2 text-medium-emphasis">
                    {{ community.description }}
                  </p>

                  <div class="d-flex flex-wrap ga-2 mt-5">
                    <BaseBadge :variant="community.visibility">
                      {{ community.visibility }}
                    </BaseBadge>

                    <v-chip
                      prepend-icon="mdi-account-group-outline"
                      variant="tonal"
                    >
                      {{ community.memberCount }} members
                    </v-chip>
                  </div>

                  <div class="mt-6">
                    <h3 class="text-subtitle-1 font-weight-bold mb-2">
                      Community rules
                    </h3>

                    <p class="text-body-2 text-medium-emphasis mb-0">
                      Be respectful, stay on topic and help keep the
                      community welcoming for everyone.
                    </p>
                  </div>
                </div>
              </v-window-item>

              <v-window-item value="members">
                <MemberList
                  :model-value="true"
                  :community="community"
                />
              </v-window-item>

              <v-window-item value="events">
                <CommunityEvents
                  :model-value="true"
                  :community="community"
                />
              </v-window-item>
            </v-window>
          </v-card-text>
        </v-card>
      </v-dialog>
    </v-container>

    <output
      v-if="!community && loading"
      class="community-detail-loading"
      aria-live="polite"
      aria-label="Loading community"
    >
      <v-progress-circular
        indeterminate
        color="primary"
        size="48"
      />
    </output>

    <BaseEmptyState
      v-if="!community && !loading"
      title="Community not found"
      message="This community may no longer be available."
    />
  </PageContainer>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'

import Navbar from '~/components/layout/Navbar.vue'
import PageContainer from '~/components/layout/PageContainer.vue'

import CommunityBanner from '~/components/features/community/CommunityBanner.vue'
import CommunityChats from '~/components/features/community/CommunityChats.vue'
import CommunityEvents from '~/components/features/community/CommunityEvents.vue'
import MemberList from '~/components/features/community/MemberList.vue'

import BaseBadge from '~/components/ui/BaseBadge.vue'
import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'

import { useCommunity } from '~/composables/useCommunity'
import { useSnackBar } from '~/composables/useSnackbar'

const route = useRoute()

const {
  getCommunityDetails,
  joinCommunity,
  error,
  loading
} = useCommunity()

const { show } = useSnackBar()

const community = ref(null)
const showDetails = ref(false)
const detailsTab = ref('about')

onMounted(async () => {
  community.value = await getCommunityDetails(route.params.id)
})

const handleJoin = async () => {
  try {
    const response = await joinCommunity(route.params.id)

    community.value.members = response.data.members
    community.value.memberCount = response.data.memberCount
    community.value.isMember = response.data.isMember

    show('Nice move! You joined the community.', 'success')
  } catch (err) {
    console.error('Failed to join community.', err)
    show(error.value, 'error')
  }
}

const handleUpdate = (newData) => {
  if (!newData || !community.value) return

  community.value.name = newData.name
  community.value.description = newData.description
  community.value.visibility = newData.visibility
  community.value.imageUrl = newData.imageUrl

  show('Nice move! Community details updated.', 'success')
}
</script>