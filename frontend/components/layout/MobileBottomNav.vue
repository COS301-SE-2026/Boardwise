<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useDisplay } from 'vuetify'

const route = useRoute()
const { smAndDown } = useDisplay()

const matches = (path: string) =>
  route.path === path || route.path.startsWith(`${path}/`)

const visible = computed(() =>
  smAndDown.value &&
  route.path !== '/' &&
  !matches('/landing') &&
  !matches('/auth') &&
  !matches('/library/read')
)

const tabs = [
  { label: 'Library', icon: 'mdi-bookshelf', to: '/library' },
  { label: 'Communities', icon: 'mdi-account-group', to: '/community' },
  { label: 'Chats', icon: 'mdi-chat-outline', to: '/chats' }
]

const moreLinks = [
  { label: 'Events', icon: 'mdi-calendar', to: '/events' },
  { label: 'Marketplace', icon: 'mdi-store-outline', to: '/marketplace' },
  { label: 'Profile', icon: 'mdi-account-outline', to: '/profile' },
  { label: 'Settings', icon: 'mdi-cog-outline', to: '/settings' },
  { label: 'Help', icon: 'mdi-help-circle-outline', to: '/help' }
]

const moreActive = computed(() =>
  moreLinks.some(link => matches(link.to))
)
</script>

<template>
  <template v-if="visible">
    <div class="mobile-bottom-nav__spacer" aria-hidden="true" />

    <nav class="mobile-bottom-nav" aria-label="Main navigation">
      <NuxtLink
        v-for="tab in tabs"
        :key="tab.to"
        :to="tab.to"
        class="mobile-bottom-nav__item"
        :class="{ 'is-active': matches(tab.to) }"
        :aria-current="matches(tab.to) ? 'page' : undefined"
      >
        <v-icon :icon="tab.icon" aria-hidden="true" />
        <span>{{ tab.label }}</span>
      </NuxtLink>

      <v-menu location="top end">
        <template #activator="{ props }">
          <button
            v-bind="props"
            type="button"
            class="mobile-bottom-nav__item"
            :class="{ 'is-active': moreActive }"
          >
            <v-icon icon="mdi-menu" aria-hidden="true" />
            <span>More</span>
          </button>
        </template>

        <v-list aria-label="More destinations">
          <v-list-item
            v-for="link in moreLinks"
            :key="link.to"
            :to="link.to"
            :title="link.label"
            :prepend-icon="link.icon"
          />
        </v-list>
      </v-menu>
    </nav>
  </template>
</template>