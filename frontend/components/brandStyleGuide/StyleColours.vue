<template>
    <SectionTitle 
      title="01 - Brand Palette" 
      subtitle="Material-inspired game tokens. Each core brand color maps directly to functional semantic tokens."
    />

    <v-row class="mt-4">
        <v-col 
            v-for="color in brandColors" 
            :key="color.name" 
            cols="12" 
            sm="6" 
            md="4" 
            lg="2.4"
        >
            <v-card flat border class="swatch-card rounded-lg overflow-hidden h-100">
                <div 
                    class="swatch-chip" 
                    :style="{ background: color.hex, borderBottom: color.border ? '1px solid var(--color-border)' : 'none' }"
                >
                    <span class="wcag" :style="{ color: color.badgeTextColor || '#FFF' }">
                    {{ color.wcag }}
                    </span>
                </div>

                <v-card-text class="pa-3">
                    <div class="swatch-name">{{ color.name }}</div>
                    <div class="swatch-role mb-2">{{ color.role }}</div>
                    <div class="swatch-values">
                        <code>{{ color.variable }}</code>
                        <code>{{ color.hex }}</code>
                    </div>
                </v-card-text>
            </v-card>
        </v-col>
    </v-row>
</template>

<script setup>
import SectionTitle from '~/components/ui/SectionTitle.vue'

const brandColors = [
  { name: 'Obsidian', role: 'Headings / Secondary', variable: '--obsidian', hex: '#4E1E5C', wcag: '11.2:1 AAA' },
  { name: 'Merlot', role: 'Primary Hover', variable: '--merlot', hex: '#8E1B4E', wcag: '7.4:1 AAA' },
  { name: 'Wildfire', role: 'Primary Action', variable: '--wildfire', hex: '#C7286E', wcag: '4.6:1 AA' },
  { name: 'Copper', role: 'Accent / Badges', variable: '--copper', hex: '#EF5B27', wcag: '3.2:1 AA' },
  { name: 'Rosewood', role: 'Tints / Soft Accents', variable: '--rosewood', hex: '#E58BB4', wcag: '2.3:1', badgeTextColor: '#2E1430' },
  { name: 'Scrollpaper', role: 'Page Background', variable: '--scrollpaper', hex: '#FBF6F0', wcag: '1.0:1', badgeTextColor: '#2E1430', border: true },
  { name: 'Alabaster', role: 'Surfaces / Cards', variable: '--alabaster', hex: '#FFFFFF', wcag: '1.0:1', badgeTextColor: '#2E1430', border: true },
  { name: 'Feltboard', role: 'Alt Surface / Borders', variable: '--feltboard', hex: '#F3E9E2', wcag: '1.1:1', badgeTextColor: '#2E1430', border: true },
  { name: 'Shadowink', role: 'Body Text', variable: '--shadowink', hex: '#2E1430', wcag: '15.2:1 AAA' },
  { name: 'Ashquill', role: 'Muted Text', variable: '--ashquill', hex: '#6B5563', wcag: '6.1:1 AA' }
]
</script>

<style scoped>
.section-border {
  border-top: 1px solid var(--color-border);
}

.swatch-chip {
  height: 90px;
  position: relative;
}

.swatch-chip .wcag {
  position: absolute;
  bottom: var(--space-2);
  right: var(--space-2);
  font-size: 10px;
  font-family: monospace;
  padding: 2px 6px;
  border-radius: var(--radius-pill, 9999px);
  background: rgba(0, 0, 0, 0.25);
}

.swatch-name {
  font-family: var(--font-display);
  font-size: var(--fs-h4);
}

.swatch-role {
  font-size: var(--fs-small);
  color: var(--color-text-muted);
}

.swatch-values code {
  display: block;
  font-size: 11px;
  color: var(--color-text-muted);
}
</style>