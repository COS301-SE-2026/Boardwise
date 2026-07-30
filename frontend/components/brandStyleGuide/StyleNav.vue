<template>
    <v-app-bar flat border class="nav-bar px-4">
        <div class="d-flex align-center ga-2 nav-brand">
            <span class="dot"></span>
            <span class="brand-title">Boardwise Brand</span>
        </div>

        <v-spacer></v-spacer>

        <div class="d-none d-md-flex align-center ga-6 nav-links">
            <a href="#colours">Colours</a>
            <a href="#typography">Typography</a>
            <a href="#spacing">Spacing & Radius</a>
            <a href="#components">Components</a>
            <a href="#icons">Icons</a>
        </div>

        <v-btn
            icon
            variant="text"
            class="ml-4"
            @click="$emit('toggle-theme')"
            title="Toggle dark mode"
        >
            <v-icon>{{ isDark ? 'mdi-weather-sunny' : 'mdi-weather-night' }}</v-icon>
        </v-btn>
  </v-app-bar>
</template>

<script setup>
defineProps({
  isDark: { type: Boolean, default: false }
})

defineEmits(['toggle-theme'])
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