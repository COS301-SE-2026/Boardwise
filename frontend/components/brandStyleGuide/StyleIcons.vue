<template> 
    <section class="py-8 section-border" id="icons">
        <SectionTitle
            title="06 - Icons"
            subtitle="Material Design Icons (mdi) bundled with Vuetify. Use a semantic colour token rather than a raw hex value."
        />

        <div class="d-flex flex-column ga-6 mt-4">
            <div class="spec-block pb-4">
                <span class="spec-label mb-2">Sizes</span>
                <div class="d-flex flex-wrap ga-6 mt-2">
                    <div v-for="size in sizeTokens" :key="size.label" class="text-center">
                        <v-icon icon="mdi-dice-multiple" :size="size.px" color="primary" />
                        <div class="token-value mt-2">{{  size.label  }} · {{ size.px }}px</div>
                    </div>
                </div>
            </div>

            <div class="spec-block pb-4">
                <span class="spec-label mb-2">Colour Variants</span>
                <div class="d-flex flex-wrap ga-6 mt-2">
                    <div v-for="variant in colorVariants" :key="variant.label" class="text-center">
                        <v-icon icon="mdi-cards-playing" size="28" :style="{ color: variant.color }" />
                        <div class="token-value mt-2">{{  variant.label  }}</div>
                    </div>
                </div>
            </div>

            <div class="spec-block pb-4">
                <span class="spec-label mb-2">Common Set</span>
                <v-row class="mt-2">
                    <v-col v-for="item in commonIcons" :key="item.icon" cols="6" sm="3" class="text-center">
                        <div class="icon-title">
                            <v-icon :icon="item.icon" size="24" color="secondary" />
                        </div>

                        <div class="token-value mt-2">{{  item.label  }}</div>
                    </v-col>
                </v-row>
            </div>

        </div>
    </section>

</template>

<script setup>
import SectionTitle from '../ui/SectionTitle.vue';

const sizeTokens = [
  { label: 'sm', px: 16 },
  { label: 'md', px: 20 },
  { label: 'lg', px: 24 },
  { label: 'xl', px: 32 },
  { label: '2xl', px: 48 },
]
 
const colorVariants = [
  { label: 'primary', color: 'var(--color-primary)' },
  { label: 'secondary', color: 'var(--color-secondary)' },
  { label: 'accent', color: 'var(--color-accent)' },
  { label: 'muted', color: 'var(--color-text-muted)' },
  { label: 'success', color: 'var(--color-success)' },
  { label: 'warning', color: 'var(--color-warning)' },
  { label: 'error', color: 'var(--color-error)' },
]
 
const commonIcons = [
  { icon: 'mdi-cards-playing', label: 'Games' },
  { icon: 'mdi-dice-multiple', label: 'Dice' },
  { icon: 'mdi-chess-knight', label: 'Strategy' },
  { icon: 'mdi-puzzle', label: 'Puzzle' },
  { icon: 'mdi-account-group', label: 'Players' },
  { icon: 'mdi-timer-sand', label: 'Duration' },
  { icon: 'mdi-map-marker', label: 'Location' },
  { icon: 'mdi-share-variant', label: 'Share' },
]
</script>

<style scoped>
.section-border {
  border-top: 1px solid var(--color-border);
}
 
.spec-block {
  border-bottom: 1px dashed var(--color-border);
}
 
.spec-label {
  display: block;
  color: var(--color-primary);
  font-weight: var(--fw-bold);
}

.token-value {
  font-size: var(--fs-small);
  color: var(--color-text-muted);
}
 
.icon-title {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: var(--space-4);
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
</style>