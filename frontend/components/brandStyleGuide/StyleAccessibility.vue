<template>
  <section class="py-8 section-border" id="accessibility">
    <SectionTitle 
      title="07- Accessibility" 
      subtitle="Boardwise targets WCAG 2.2 level AA as a minimum. All interactive elements have visible focus states, all images have alt text, and all colour combinations meet contrast requirements"
    />

    <v-row class="mt-4">
        <v-col 
            v-for="stat in a11yStats" 
            :key="stat.label" 
            cols="12" 
            sm="4" 
        >
            <v-card flat border class="a11y-card pa-4 h-100">
                <div class="a11y-score">{{  stat.score  }}</div>
                <div class="a11y-label mb-1">{{  stat.label  }}</div>
                <div class="a11y-desc">{{  stat.desc  }}</div>
            </v-card>
        </v-col>
    </v-row>

    <h3 class="contrast-heading mt-8 mb-4">Contrast Ratios</h3>
    <v-card flat border class="pa-4">
        <div class="contrast-card-title mb-2">Text on Backgrounds -WCAG 2.2</div>

        <div
            v-for="row in contrastRows"
            :key="row.name"
            class="contrast-row"
            :class="{ 'no-border': row.last }"
        >
            <div class="contrast-swatch" :style="{ background: row.hex }">Aa</div>
            <div class="contrast-info">
                <div class="contrast-name">{{ row.name }}</div>
                <div class="contrast-ratio">{{ row.ratio }}</div>
            </div>

            <v-chip size="small" :color="row.badgeColor" variant="flat">
                {{  row.badgeLabel }}
            </v-chip>
        </div>
    </v-card>
  </section>
</template>

<script setup>
import SectionTitle from '../ui/SectionTitle.vue';

const a11yStats = [
  {
    score: 'AA',
    label: 'WCAG 2.2 Target',
    desc: 'Minimum standard for all text, interactive elements and UI components. Primary action colours (wildfire on white) meet the 4.5:1 ratio for normal text.',
  },
  {
    score: '3px',
    label: 'Focus Ring Width',
    desc: 'All focusable elements show a 3px rosewood outline with 2px offset. Visible in both light and dark mode. Never removed with outline: none.',
  },
  {
    score: '✓',
    label: 'Reduced Motion',
    desc: 'All animations and transitions respect prefers-reduced-motion. Duration drops to 0.01ms when enabled.',
  },
]
 
const contrastRows = [
  { name: 'Wildfire on Alabaster', hex: 'var(--wildfire)', ratio: '4.5 : 1', badgeColor: 'success', badgeLabel: 'AA' },
  { name: 'Obsidian on Alabaster', hex: 'var(--obsidian)', ratio: '10.2 : 1', badgeColor: 'success', badgeLabel: 'AAA' },
  { name: 'Shadowink on Scrollpaper', hex: 'var(--shadowink)', ratio: '14.5 : 1', badgeColor: 'success', badgeLabel: 'AAA' },
  { name: 'Ashquill on Scrollpaper', hex: 'var(--ashquill)', ratio: '5.9 : 1', badgeColor: 'success', badgeLabel: 'AA' },
  { name: 'Copper on Alabaster', hex: 'var(--copper)', ratio: '3.1 : 1', badgeColor: 'warning', badgeLabel: 'Use large text only' },
  { name: 'Rosewood on Alabaster', hex: 'var(--rosewood)', ratio: '2.1 : 1', badgeColor: 'error', badgeLabel: 'Decorative use only', last: true },
]
</script>

<style scoped>
.section-border {
  border-top: 1px solid var(--color-border);
}

.a11y-card {
  border-radius: var(--radius-md);
}
 
.a11y-score {
  font-family: var(--font-display);
  font-size: 1.75rem;
  font-weight: var(--fw-bold);
  color: var(--color-primary);
}
 
.a11y-label {
  font-weight: var(--fw-bold);
  color: var(--color-secondary);
}
 
.a11y-desc {
  font-size: var(--fs-small);
  color: var(--color-text-muted);
}

.contrast-heading {
  font-family: var(--font-display);
  font-size: 1.1rem;
  color: var(--color-secondary);
}
 
.contrast-card-title {
  font-weight: var(--fw-bold);
  color: var(--color-secondary);
}
 
.contrast-row {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: 12px 0;
  border-bottom: 1px solid var(--color-border);
}
 
.contrast-row.no-border {
  border-bottom: none;
}
 
.contrast-swatch {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--color-text-inverse);
  font-weight: var(--fw-bold);
  flex-shrink: 0;
}
 
.contrast-info {
  flex: 1;
}
 
.contrast-name {
  font-weight: var(--fw-bold);
  color: var(--color-text);
}
 
.contrast-ratio {
  font-size: var(--fs-small);
  color: var(--color-text-muted);
  font-family: ui-monospace, 'SF Mono', Menlo, monospace;
}
</style>