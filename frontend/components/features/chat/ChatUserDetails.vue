<template>
    <v-dialog
        v-model="dialog"
        :fullscreen="smAndDown"
        max-width="760"
        scrollable
    >
        <BaseCard class="chat-user-details">
            <header class="chat-user-details__header">
                <div class="d-flex align-center ga-4">
                    <BaseAvatar
                        :src="conversation.avatar"
                        :name="conversation.name"
                        size="xl"
                    />

                    <div class="flex-grow-1">
                        <h2 class="chat-user-details__name">
                            {{ conversation.name }}
                        </h2>

                        <p
                            v-if="username"
                            class="chat-user-details__username"
                        >
                            @{{ username }}
                        </p>

                        <div class="chat-user-details__status">
                            <span
                                class="chat-user-details__status-dot"
                                :class="{
                                    'chat-user-details__status-dot--online':
                                        conversation.online
                                }"
                                aria-hidden="true"
                            />

                            {{ conversation.online ? 'Online' : 'Offline' }}
                        </div>
                    </div>

                    <BaseButton
                        variant="secondary"
                        aria-label="Close conversation details"
                        @click="dialog = false"
                    >
                        <v-icon
                            icon="mdi-close"
                            aria-hidden="true"
                        />
                    </BaseButton>
                </div>
            </header>

            <v-divider />

            <!-- Tabs -->
            <v-tabs
                v-model="activeTab"
                color="primary"
                class="chat-user-details__tabs"
                grow
            >
                <v-tab value="overview">
                    Overview
                </v-tab>

                <v-tab value="media">
                    Media

                    <v-chip
                        v-if="media.length"
                        size="x-small"
                        class="ms-2"
                    >
                        {{ media.length }}
                    </v-chip>
                </v-tab>

                <v-tab value="events">
                    Events
                </v-tab>

                <v-tab value="communities">
                    Communities
                </v-tab>
            </v-tabs>

            <v-divider />

            <div class="chat-user-details__body">
                <v-window v-model="activeTab">
                    <!-- OVERVIEW -->
                    <v-window-item value="overview">
                        <div class="d-flex flex-column ga-6">
                            <section>
                                <h3 class="chat-user-details__section-title">
                                    About
                                </h3>

                                <p
                                    v-if="bio"
                                    class="text-body-2 text-medium-emphasis mb-0"
                                >
                                    {{ bio }}
                                </p>

                                <p
                                    v-else
                                    class="text-body-2 text-medium-emphasis mb-0"
                                >
                                    No bio has been added yet.
                                </p>
                            </section>

                            <v-divider />

                            <section>
                                <h3 class="chat-user-details__section-title">
                                    You both have
                                </h3>

                                <div class="chat-user-details__stats">
                                    <div class="chat-user-detail-stat">
                                        <v-icon
                                            icon="mdi-account-group-outline"
                                            color="primary"
                                            aria-hidden="true"
                                        />

                                        <div>
                                            <strong>
                                                {{ communities.length }}
                                            </strong>

                                            <span>
                                                Common communities
                                            </span>
                                        </div>
                                    </div>

                                    <div class="chat-user-detail-stat">
                                        <v-icon
                                            icon="mdi-calendar-outline"
                                            color="primary"
                                            aria-hidden="true"
                                        />

                                        <div>
                                            <strong>
                                                {{ events.length }}
                                            </strong>

                                            <span>
                                                Shared events
                                            </span>
                                        </div>
                                    </div>

                                    <div class="chat-user-detail-stat">
                                        <v-icon
                                            icon="mdi-image-multiple-outline"
                                            color="primary"
                                            aria-hidden="true"
                                        />

                                        <div>
                                            <strong>
                                                {{ media.length }}
                                            </strong>

                                            <span>
                                                Shared media
                                            </span>
                                        </div>
                                    </div>

                                    <div class="chat-user-detail-stat">
                                        <v-icon
                                            icon="mdi-dice-multiple-outline"
                                            color="primary"
                                            aria-hidden="true"
                                        />

                                        <div>
                                            <strong>
                                                {{ sharedGames.length }}
                                            </strong>

                                            <span>
                                                Games in common
                                            </span>
                                        </div>
                                    </div>
                                </div>
                            </section>

                            <template v-if="sharedGames.length">
                                <v-divider />

                                <section>
                                    <h3 class="chat-user-details__section-title">
                                        Games you both enjoy
                                    </h3>

                                    <div class="d-flex flex-wrap ga-2">
                                        <v-chip
                                            v-for="game in sharedGames"
                                            :key="game.id ?? game.title ?? game"
                                            color="primary"
                                            variant="tonal"
                                        >
                                            {{
                                                game.title ??
                                                game.name ??
                                                game
                                            }}
                                        </v-chip>
                                    </div>
                                </section>
                            </template>
                        </div>
                    </v-window-item>

                    <!-- MEDIA -->
                    <v-window-item value="media">
                        <section aria-labelledby="shared-media-heading">
                            <div class="mb-5">
                                <h3
                                    id="shared-media-heading"
                                    class="chat-user-details__section-title"
                                >
                                    Shared media
                                </h3>

                                <p class="text-body-2 text-medium-emphasis mb-0">
                                    Images shared in this conversation.
                                </p>
                            </div>

                            <div
                                v-if="media.length"
                                class="chat-media-grid"
                            >
                                <button
                                    v-for="(item, index) in media"
                                    :key="item.id ?? index"
                                    type="button"
                                    class="chat-media-item"
                                    :aria-label="
                                        `View ${
                                            item.alt ??
                                            item.title ??
                                            `shared image ${index + 1}`
                                        }`
                                    "
                                    @click="selectedMedia = item"
                                >
                                    <BaseImage
                                        :src="mediaSource(item)"
                                        :alt="
                                            item.alt ??
                                            item.title ??
                                            'Shared conversation image'
                                        "
                                        height="150"
                                        cover
                                    />
                                </button>
                            </div>

                            <BaseEmptyState
                                v-else
                                title="No shared media"
                                description="Photos and images shared in this conversation will appear here."
                            />
                        </section>
                    </v-window-item>

                    <!-- EVENTS -->
                    <v-window-item value="events">
                        <section aria-labelledby="shared-events-heading">
                            <div class="mb-5">
                                <h3
                                    id="shared-events-heading"
                                    class="chat-user-details__section-title"
                                >
                                    Events
                                </h3>

                                <p class="text-body-2 text-medium-emphasis mb-0">
                                    Boardwise events you have in common.
                                </p>
                            </div>

                            <div
                                v-if="events.length"
                                class="d-flex flex-column ga-3"
                            >
                                <NuxtLink
                                    v-for="event in events"
                                    :key="event.id"
                                    :to="`/events/detail/${event.id}`"
                                    class="chat-detail-link"
                                >
                                    <BaseCard class="chat-detail-row pa-4">
                                        <div class="d-flex align-center ga-4">
                                            <div class="chat-detail-row__icon">
                                                <v-icon
                                                    icon="mdi-calendar"
                                                    color="primary"
                                                    aria-hidden="true"
                                                />
                                            </div>

                                            <div class="flex-grow-1">
                                                <h4 class="chat-detail-row__title">
                                                    {{
                                                        event.name ??
                                                        event.title
                                                    }}
                                                </h4>

                                                <p
                                                    v-if="event.date"
                                                    class="chat-detail-row__meta"
                                                >
                                                    <v-icon
                                                        icon="mdi-calendar-outline"
                                                        size="15"
                                                        aria-hidden="true"
                                                    />

                                                    {{ event.date }}
                                                </p>

                                                <p
                                                    v-if="event.location"
                                                    class="chat-detail-row__meta"
                                                >
                                                    <v-icon
                                                        icon="mdi-map-marker-outline"
                                                        size="15"
                                                        aria-hidden="true"
                                                    />

                                                    {{ event.location }}
                                                </p>
                                            </div>

                                            <v-icon
                                                icon="mdi-chevron-right"
                                                aria-hidden="true"
                                            />
                                        </div>
                                    </BaseCard>
                                </NuxtLink>
                            </div>

                            <BaseEmptyState
                                v-else
                                title="No shared events"
                                description="Events you both attend will appear here."
                            />
                        </section>
                    </v-window-item>

                    <!-- COMMUNITIES -->
                    <v-window-item value="communities">
                        <section aria-labelledby="common-communities-heading">
                            <div class="mb-5">
                                <h3
                                    id="common-communities-heading"
                                    class="chat-user-details__section-title"
                                >
                                    Communities in common
                                </h3>

                                <p class="text-body-2 text-medium-emphasis mb-0">
                                    Communities that you both belong to.
                                </p>
                            </div>

                            <div
                                v-if="communities.length"
                                class="d-flex flex-column ga-3"
                            >
                                <NuxtLink
                                    v-for="community in communities"
                                    :key="community.id"
                                    :to="`/community/${community.id}`"
                                    class="chat-detail-link"
                                >
                                    <BaseCard class="chat-detail-row pa-4">
                                        <div class="d-flex align-center ga-4">
                                            <BaseAvatar
                                                :src="
                                                    community.imageUrl ??
                                                    community.avatar
                                                "
                                                :name="community.name"
                                                size="lg"
                                            />

                                            <div class="flex-grow-1">
                                                <h4 class="chat-detail-row__title">
                                                    {{ community.name }}
                                                </h4>

                                                <p
                                                    v-if="community.memberCount"
                                                    class="chat-detail-row__meta"
                                                >
                                                    {{
                                                        community.memberCount
                                                    }}
                                                    members
                                                </p>
                                            </div>

                                            <v-icon
                                                icon="mdi-chevron-right"
                                                aria-hidden="true"
                                            />
                                        </div>
                                    </BaseCard>
                                </NuxtLink>
                            </div>

                            <BaseEmptyState
                                v-else
                                title="No communities in common"
                                description="Communities you both join will appear here."
                            />
                        </section>
                    </v-window-item>
                </v-window>
            </div>
        </BaseCard>

        <!-- Media preview -->
        <v-dialog
            :model-value="Boolean(selectedMedia)"
            max-width="900"
            @update:model-value="
                !$event && (selectedMedia = null)
            "
        >
            <BaseCard
                v-if="selectedMedia"
                class="pa-3"
            >
                <div class="d-flex justify-end mb-2">
                    <BaseButton
                        variant="secondary"
                        aria-label="Close media preview"
                        @click="selectedMedia = null"
                    >
                        <v-icon
                            icon="mdi-close"
                            aria-hidden="true"
                        />
                    </BaseButton>
                </div>

                <BaseImage
                    :src="mediaSource(selectedMedia)"
                    :alt="
                        selectedMedia.alt ??
                        selectedMedia.title ??
                        'Shared conversation image'
                    "
                    contain
                />
            </BaseCard>
        </v-dialog>
    </v-dialog>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useDisplay } from 'vuetify'

import BaseAvatar from '~/components/ui/BaseAvatar.vue'
import BaseButton from '~/components/ui/BaseButton.vue'
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'
import BaseImage from '~/components/ui/BaseImage.vue'

const props = defineProps({
    modelValue: {
        type: Boolean,
        default: false
    },

    conversation: {
        type: Object,
        required: true
    }
})

const emit = defineEmits([
    'update:modelValue'
])

const { smAndDown } = useDisplay()

const activeTab = ref('overview')
const selectedMedia = ref(null)

const dialog = computed({
    get: () => props.modelValue,

    set: (value) => {
        emit('update:modelValue', value)
    }
})

const details = computed(() => {
    return props.conversation.details ?? props.conversation
})

const username = computed(() =>
    details.value.username ?? ''
)

const bio = computed(() =>
    details.value.bio ?? ''
)

const media = computed(() =>
    details.value.media ?? []
)

const events = computed(() =>
    details.value.sharedEvents ??
    details.value.events ??
    []
)

const communities = computed(() =>
    details.value.commonCommunities ??
    details.value.sharedCommunities ??
    details.value.communities ??
    []
)

const sharedGames = computed(() =>
    details.value.sharedGames ??
    details.value.commonGames ??
    []
)

const mediaSource = (item) => {
    return (
        item.url ??
        item.src ??
        item.imageUrl ??
        ''
    )
}
</script>