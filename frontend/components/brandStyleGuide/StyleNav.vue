<template>
    <v-app-bar flat border class="nav-bar px-4">
        <div class="d-flex align-center ga-2 nav-brand">
            <span class="dot"></span>
            <span class="brand-title">Boardwise Brand</span>
        </div>

        <v-spacer></v-spacer>

        <div class="d-none d-md-flex align-center ga-6 nav-links">
            <a v-for="link in links" :key="link.href" :href="link.href">
              {{ link.label }}
            </a>
        </div>

        <v-menu v-model="mobileMenuOpen" class="d-md-none">
          <template #activator="{ props }">
            <v-btn
              icon 
              variant="text"
              class="d-flex d-md-none"
              v-bind="props"
              title="Open menu"
            >
              <v-icon>mdi-menu</v-icon>
            </v-btn>
          </template>

          <v-list>
            <v-list-item 
              v-for="link in links"
              :key="link.href"
              :href="link.href"
              @click="mobileMenuOpen = false"
            >
              <v-list-item-title>{{  link.label }}</v-list-item-title>
            </v-list-item>
          </v-list>
        </v-menu>

        <!-- <v-btn
            icon
            variant="text"
            class="ml-4"
            @click="$emit('toggle-theme')"
            title="Toggle dark mode"
        >
            <v-icon>{{ isDark ? 'mdi-weather-sunny' : 'mdi-weather-night' }}</v-icon>
        </v-btn> -->
  </v-app-bar>
</template>

<script setup>
import { ref } from 'vue'

defineProps({
  isDark: { type: Boolean, default: false }
})

defineEmits(['toggle-theme'])

const mobileMenuOpen = ref(false)

const links = [
  { href: '#colours', label: 'Colours' },
  { href: '#typography', label: 'Typography' },
  { href: '#spacing', label: 'Spacing & Radius' },
  { href: '#components', label: 'Components' },
  { href: '#icons', label: 'Icons' },
  { href: '#dos-donts', label: 'Dos & Donts'},
  { href: '#accessibility', label: 'Accessibility'},
  { href: '#voice', label:'Voice & Tone'},
  { href: '#changelog', label:'Changelog'}
]
</script>

<style scoped>
.nav-bar {
  background: var(--color-surface) !important;
  backdrop-filter: blur(8px);
}

.brand-title {
  font-family: var(--font-display);
  font-size: 1.1rem;
  font-weight: var(--fw-bold);
  color: var(--color-secondary);
}

.dot {
  width: 10px;
  height: 10px;
  background: var(--color-primary);
  border-radius: 50%;
}

.nav-links a {
  font-size: var(--fs-small);
  font-weight: var(--fw-bold);
  color: var(--color-text-muted);
  text-decoration: none;
  transition: color 0.2s ease;
}

.nav-links a:hover {
  color: var(--color-primary);
}
</style>