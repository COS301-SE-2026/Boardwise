<template>
<header class="hero text-center py-12">
    <div class="hero-eyebrow mb-2">Brand Style Guide</div>
    <h1 class="hero-title mb-3">Board<span class="accent">wise</span></h1>
    <p class="hero-sub mx-auto mb-8">
      Single source of truth for design tokens, component primitives, and visual language across Boardwise.
    </p>

    <v-row justify="center" class="hero-meta mb-6">

      <v-col cols="6" sm="3" md="2" class="meta-item">
        <span class="value">v2.0</span>
        <span class="label">Version</span>
      </v-col>

      <v-col cols="6" sm="3" md="2" class="meta-item">
        <span class="value">10</span>
        <span class="label">Brand Colours</span>
      </v-col>

      <v-col cols="6" sm="3" md="2" class="meta-item">
        <span class="value">3</span>
        <span class="label">Typefaces</span>
      </v-col>

      <!-- <v-col cols="6" sm="3" md="2" class="meta-item">
        <span class="value">AA+</span>
        <span class="label">WCAG Target</span>
      </v-col> -->
    </v-row>

    <div class="d-flex justify-center ga-3 flex-wrap">
      <BaseBadge variant="copper">Play More</BaseBadge>
      <BaseBadge variant="fire">Think Deeper</BaseBadge>
      <BaseBadge variant="obsidian">Connect Better</BaseBadge>
    </div>
  </header>
</template>

<script setup>
import BaseBadge from '~/components/ui/BaseBadge.vue'
</script>

<style scoped>
.hero-eyebrow {
    font-family: var(--font-display);
    font-size: var(--fs-small);
    color: var(--color-primary);
    text-transform: uppercase;
    letter-spacing: 0.1em;
}

.hero-title {
  font-size: clamp(3rem, 8vw, 5rem);
  font-family: var(--font-display);
}

.hero-title .accent {
  color: var(--color-primary);
}

.hero-sub {
  font-size: var(--fs-body-lg);
  color: var(--color-text-muted);
  max-width: 580px;
}

.meta-item {
  display: flex;
  flex-direction: column;
}

.meta-item .value {
  font-family: var(--font-display);
  font-size: var(--fs-h2);
  color: var(--color-secondary);
}

.meta-item .label {
  font-size: var(--fs-small);
  color: var(--color-text-muted);
}
</style>