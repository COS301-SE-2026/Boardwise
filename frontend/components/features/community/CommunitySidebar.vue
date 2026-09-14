<template>
  <BaseCard class="community-sidebar">
    <header class="community-sidebar__header">
      <div>
        <h1 class="community-sidebar__title">
          Communities
        </h1>

        <p class="community-sidebar__subtitle">
          Choose a community to view its conversation.
        </p>
      </div>
    </header>

    <BaseSearch
      v-model="search"
      class="community-sidebar__search"
      placeholder="Search communities"
      aria-label="Search communities"
    />

    <div class="community-sidebar__filters">
      <span
        id="community-list-filter-label"
        class="community-sidebar__filter-label"
      >
        Show
      </span>

      <v-chip-group
        v-model="activeFilter"
        mandatory
        aria-labelledby="community-list-filter-label"
      >
        <v-chip
          value="all"
          filter
          variant="tonal"
        >
          All
        </v-chip>

        <v-chip
          value="public"
          filter
          variant="tonal"
        >
          Public
        </v-chip>

        <v-chip
          value="private"
          filter
          variant="tonal"
        >
          Private
        </v-chip>
      </v-chip-group>
    </div>

    <v-divider />

    <div class="community-sidebar__results">
      <output
        v-if="loading"
        class="community-sidebar__loading"
        aria-live="polite"
        aria-label="Loading communities"
      >
        <v-progress-circular
          indeterminate
          color="primary"
          size="40"
        />
    </output>

      <ul
        v-else-if="filteredCommunities.length"
        class="community-sidebar__list"
        aria-label="Community list"
      >
        <li
          v-for="community in filteredCommunities"
          :key="communityId(community)"
          class="community-sidebar__list-item"
        >
          <button
            type="button"
            class="community-sidebar__community"
            :class="{
              'community-sidebar__community--active':
                isSelected(community)
            }"
            :aria-pressed="isSelected(community)"
            :aria-label="communityLabel(community)"
            @click="emit('select', communityId(community))"
          >
            <BaseImage
              :src="
                community.imageUrl ??
                  community.communityPfp ??
                  '/images/default-listing.png'
              "
              :alt="`${community.name} community image`"
              width="48"
              height="48"
              class="community-sidebar__image"
            />

            <div class="community-sidebar__content">
              <div class="community-sidebar__name-row">
                <span class="community-sidebar__name">
                  {{ community.name }}
                </span>

                <BaseBadge
                  class="community-sidebar__visibility"
                  :variant="community.visibility"
                >
                  {{ community.visibility }}
                </BaseBadge>
              </div>

              <p class="community-sidebar__description">
                {{
                  community.description ||
                    'No community description'
                }}
              </p>

              <span class="community-sidebar__members">
                <v-icon
                  icon="mdi-account-group-outline"
                  size="14"
                  aria-hidden="true"
                />

                {{ memberCount(community) }}
                {{
                  memberCount(community) === 1
                    ? 'member'
                    : 'members'
                }}
              </span>
            </div>

            <v-icon
              icon="mdi-chevron-right"
              class="community-sidebar__chevron"
              aria-hidden="true"
            />
          </button>
        </li>
      </ul>

      <BaseEmptyState
        v-else
        title="No communities found"
        :description="emptyDescription"
      />
    </div>
  </BaseCard>
</template>

<script setup>
import {
  computed,
  ref
} from 'vue'

import BaseBadge from '~/components/ui/BaseBadge.vue'
import BaseCard from '~/components/ui/BaseCard.vue'
import BaseEmptyState from '~/components/ui/BaseEmptyState.vue'
import BaseImage from '~/components/ui/BaseImage.vue'
import BaseSearch from '~/components/ui/BaseSearch.vue'

const props = defineProps({
  communities: {
    type: Array,
    default: () => []
  },

  selectedId: {
    type: [String, Number],
    default: null
  },

  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['select'])

const search = ref('')
const activeFilter = ref('all')

const communityId = community =>
  community.groupId ?? community.id

const memberCount = community =>
  community.memberCount ??
  community.members?.length ??
  0

const isSelected = community =>
  String(communityId(community)) ===
  String(props.selectedId)

const filteredCommunities = computed(() => {
  const query = search.value
    .trim()
    .toLowerCase()

  return props.communities.filter(community => {
    const visibility =
      community.visibility?.toLowerCase() ?? 'public'

    const matchesSearch =
      !query ||
      community.name
        ?.toLowerCase()
        .includes(query) ||
      community.description
        ?.toLowerCase()
        .includes(query)

    const matchesFilter =
      activeFilter.value === 'all' ||
      visibility === activeFilter.value

    return matchesSearch && matchesFilter
  })
})

const emptyDescription = computed(() => {
  if (search.value.trim()) {
    return `No communities match "${search.value.trim()}".`
  }

  if (activeFilter.value !== 'all') {
    return `No ${activeFilter.value} communities found.`
  }

  return 'Available communities will appear here.'
})

const communityLabel = community => {
  const count = memberCount(community)
  const visibility =
    community.visibility ?? 'public'

  return `${community.name}, ${visibility}, ${count} ${
    count === 1 ? 'member' : 'members'
  }`
}
</script>