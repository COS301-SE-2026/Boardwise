<template>
    <v-dialog
        v-model="dialog"
        :fullscreen="smAndDown"
        max-width="760"
        scrollable
    >
        <BaseCard
            class="community-more-details"
            role="dialog"
            aria-labelledby="community-details-title"
            >
            <header class="community-more-details__header">
                <div class="community-more-details__identity">
                    <BaseImage
                        :src="community.imageUrl"
                        :alt="`${community.name} community image`"
                        width="72"
                        height="72"
                        class="community-more-details__image"
                    />

                   <div class="community-more-details__heading">
                        <div class="community-more-details__title-row">
                        <h2
                            id="community-details-title"
                            class="community-more-details__name"
                        >
                            {{ community.name }}
                        </h2>

                        <BaseBadge :variant="community.visibility">
                            {{ visibilityLabel }}
                        </BaseBadge>
                        </div>

                        <p class="community-more-details__meta">
                        <v-icon
                            icon="mdi-account-group-outline"
                            size="17"
                            aria-hidden="true"
                        />

                        {{ memberCount }}
                        {{ memberCount === 1 ? 'member' : 'members' }}
                        </p>
                    </div>
                    </div>

                    <BaseButton
                        variant="secondary"
                        class="community-more-details__close"
                        aria-label="Close community details"
                        @click="dialog = false"
                    >
                        <v-icon
                            icon="mdi-close"
                            aria-hidden="true"
               />
                    </BaseButton>
            </header>

            <v-divider />

            <v-tabs
                v-model="activeTab"
                color="primary"
                class="community-more-details__tabs"
                grow
                show-arrows
            >
                <v-tab value="overview">
                    Overview
                </v-tab>

                <v-tab value="members">
                    Members

                    <v-chip
                        v-if="memberCount"
                        size="x-small"
                        class="ms-2"
                    >
                        {{ memberCount }}
              </v-chip>
                </v-tab>

            <v-tab value="events">
                    Events
                </v-tab>
            </v-tabs>

            <v-divider />

            <div class="community-more-details__body">
                <output
                    v-if="loading"
                    class="community-more-details__loading"
                    aria-live="polite"
                    aria-label="Loading community details"
                    >

                    <v-progress-circular
                        indeterminate
                        color="primary"
                        size="48"
                    />
            </output>

                <v-window
                    v-else
                    v-model="activeTab">

                    <v-window-item value="overview">
                        <section
                            class="community-more-details__section"
                            aria-labelledby="community-overview-heading"
                            >
                            <h3
                                id="community-overview-heading"
                                class="community-more-details__section-title"
                            >
                                About this community
                            </h3>
                            <p class="community-more-details__description">
                                {{
                                community.description ||
                                    'This community does not have a description yet.'
                                }}
                            </p>

                        <div class="community-more-details__stats">
                         <div class="community-more-details__stat">
                            <v-icon
                                icon="mdi-account-group-outline"
                                color="primary"
                                aria-hidden="true"
                            />

                            <div>
                               <strong>
                                    {{ memberCount }}
                                </strong>

                                <span>
                                    {{ memberCount === 1 ? 'Member' : 'Members' }}
                                </span>
                        </div>
                    </div>

                        <div class="community-more-details__stat">
                            <v-icon
                                :icon="visibilityIcon"
                                color="primary"
                                aria-hidden="true"
                            />

                            <div>
                                <strong>
                                    {{ visibilityLabel }}
                                </strong>

                                <span>
                                    Visibility
                                </span>
                            </div>
                        </div>

                    <div class="community-more-details__stat">
                        <v-icon
                            icon="mdi-account-star-outline"
                            color="primary"
                            aria-hidden="true"
                        />

                        <div>
                            <strong>{{ ownerName }}</strong>
                            <span> Community Owner</span>
                        </div>
                    </div>

                    <div class="community-more-details__stat">
                        <v-icon
                            icon="mdi-calendar-outline"
                            color="primary"
                            aria-hidden="true"
                        />

                             <div>
                                <strong>{{ createdDate }}</strong>
                               <span> Created</span>
                            </div>
                        </div>
                    </div>
                  </section>

                            <template v-if="community.rules">
                                <v-divider class="my-6" />

                                <section
                                    class="community-more-details__section"
                                    aria-labelledby="community-rules-heading"
                                >
                                    <h3
                                        id="community-rules-heading"
                                        class="community-more-details__section-title"
                                    >
                                        Community guidelines
                                    </h3>

                                    <p class="community-more-details__description">
                                        {{ community.rules }}
                                    </p>
                                </section>
                            </template>
                    </v-window-item>

                    <v-window-item value="members">
                    <section
                        class="community-more-details__section"
                        aria-labelledby="community-members-heading"
                    >
                        <div class="community-more-details__section-heading">
                        <h3
                            id="community-members-heading"
                            class="community-more-details__section-title"
                        >
                            Community members
                        </h3>

                        <p class="community-more-details__section-description">
                            View the people who belong to this community.
                        </p>
                        </div>

                        <MemberList
                        :community="community"
                        :model-value="true"
                        />
                    </section>
                    </v-window-item>

        </v-window>
    </div>
      <template v-if="community.isMember && !community.isOwner">
        <v-divider />

        <footer class="community-more-details__footer">
          <BaseButton
            variant="secondary"
            @click="emit('leave')"
          >
            <v-icon
              icon="mdi-logout"
              class="me-2"
              aria-hidden="true"
            />

            Leave community
          </BaseButton>
        </footer>
      </template>
    </BaseCard>
  </v-dialog>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useDisplay } from 'vuetify'

import BaseButton from '~/components/ui/BaseButton.vue'
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseBadge from '~/components/ui/BaseBadge.vue'
import BaseImage from '~/components/ui/BaseImage.vue'

import MemberList from './MemberList.vue'

const props = defineProps({
    modelValue: {
        type: Boolean,
        default: false
    },

    community: {
        type: Object,
        required: true
    },
    loading: {
        type: Boolean,
        default: false
    }
})

const emit = defineEmits([
    'update:modelValue','leave'
])

const { smAndDown } = useDisplay()

const activeTab = ref('overview')

const dialog = computed({
    get: () => props.modelValue,

    set: (value) => {
        emit('update:modelValue', value)
    }
})

const memberCount = computed(() => {
    return props.community.memberCount ?? members.value.length
})

const members = computed(() =>
    Array.isArray(props.community.members)
    ? props.community.members
    : []
)

const visibilityLabel = computed(() => {
  const visibility = props.community.visibility ?? 'public'

  return visibility.charAt(0).toUpperCase() +
    visibility.slice(1).toLowerCase()
})

const visibilityIcon = computed(() =>
  visibilityLabel.value.toLowerCase() === 'private'
    ? 'mdi-lock-outline'
    : 'mdi-earth'
)

const ownerName = computed(() =>
  props.community.owner?.username ??
  props.community.owner?.name ??
  props.community.ownerUsername ??
  props.community.createdBy ??
  'Not available'
)

const createdDate = computed(() => {
  const value =
    props.community.createdAt ??
    props.community.creationDate

  if (!value) {
    return 'Not available'
  }

  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return 'Not available'
  }

  return new Intl.DateTimeFormat('en-ZA', {
    day: 'numeric',
    month: 'short',
    year: 'numeric'
  }).format(date)
})

watch(dialog, isOpen => {
  if (isOpen) {
    activeTab.value = 'overview'
  }
})
</script>