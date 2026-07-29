<template> 
    <section class="py-8 section-border" id="spacing">
        <SectionTitle
            title="03 - Spacing & Radius"
            subtitle="An 8px spacing scale and a small set of corner radii keep rhythm and shape consistent across the app."
        />

        <div class="d-flex flex-column ga-6 mt-4">

            <div class="spec-block pb-4">
                <span class="spec-label mb-2">Spacing Scale</span>

                <div
                    v-for="token in spacingTokens"
                    :key="token.name"
                    class="d-flex align-center spacing-row"
                >
                    <code class="token-code">--{{ token.name }}</code>
                    <span class="token-value">{{ token.value }}</span>
                    <div class="spacing-bar" :style="{ width: token.value }" />
                </div>
            </div>

            <div class="spec-block pb-4">
                <span class="spec-label mb-2">Radius</span>
                <v-row class="mt-2"> 
                    <v-col v-for="token in radiusTokens" :key="token.name" cols="6" sm="3">
                        <div class="raidus-swatch" :style="{ borderRadius: token.value }" />
                        <div class="mt-2">
                            <code clas="token-code">--{{  token.name  }}</code>
                            <div class="token-value"> {{  token.value  }}</div>
                        </div>
                    </v-col>
                </v-row>
            </div>

            <div class="spec-block pb-4">
                <span class="spec-label mb-2">Shadow</span>
                <v-row class="mt-2"> 
                    <v-col v-for="token in shadowTokens" :key="token.name" cols="6" sm="3">
                        <div class="shadow-swatch" :style="{ boxShadow: `var(--${token.name})` }">
                            {{  token.label  }}
                        </div>  
                        
                        <code clas="token-code d-block mt-2">--{{  token.name  }}</code>
                    </v-col>
                </v-row>
            </div>

            <div>
                <span class="spec-label mb-2">Layout</span>
                <div class="layout-diagram mt-2"> 
                    <div class="layout-header">--header-height (64px)</div>
                    <div class="layout-body">
                        <div class="layout-sidebar">--sidebar-width (248px)</div>
                        <div claas="layout-body">--container-max (1200px)</div>
                    </div>
                </div>
            </div>

        </div>
    </section>

</template>

<script setup>
import SectionTitle from '../ui/SectionTitle.vue';

const spacingTokens = [
  { name: 'space-1', value: '4px' },
  { name: 'space-2', value: '8px' },
  { name: 'space-3', value: '12px' },
  { name: 'space-4', value: '16px' },
  { name: 'space-5', value: '20px' },
  { name: 'space-6', value: '24px' },
  { name: 'space-8', value: '32px' },
  { name: 'space-10', value: '40px' },
  { name: 'space-12', value: '48px' },
  { name: 'space-16', value: '64px' },
]
 
const radiusTokens = [
  { name: 'radius-sm', value: '10px' },
  { name: 'radius-md', value: '16px' },
  { name: 'radius-lg', value: '24px' },
  { name: 'radius-pill', value: '999px' },
]
 
const shadowTokens = [
  { name: 'shadow-sm', label: 'sm' },
  { name: 'shadow-md', label: 'md' },
  { name: 'shadow-lg', label: 'lg' },
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
 
.token-code {
  font-family: monospace;
  font-size: var(--fs-small);
  color: var(--color-primary);
  background: var(--color-surface-alt);
  padding: 2px 8px;
  border-radius: 6px;
  min-width: 140px;
  display: inline-block;
}
 
.token-value {
  font-size: var(--fs-small);
  color: var(--color-text-muted);
  min-width: 60px;
  margin-left: var(--space-3);
}
 
.spacing-row {
  gap: var(--space-2);
  margin-bottom: var(--space-3);
}
 
.spacing-bar {
  height: 16px;
  background: var(--color-primary);
  border-radius: 4px;
  margin-left: var(--space-3);
}
 
.radius-swatch {
  width: 100%;
  aspect-ratio: 1;
  background: var(--color-primary);
}
 
.shadow-swatch {
  background: var(--color-surface);
  border-radius: var(--radius-md);
  padding: var(--space-6);
  text-align: center;
  font-family: var(--font-display);
  color: var(--color-text-muted);
}

.layout-diagram {
  border: 2px dashed var(--color-border-strong);
  border-radius: var(--radius-md);
  overflow: hidden;
}
 
.layout-header {
  background: var(--color-secondary);
  color: var(--color-text-inverse);
  font-size: var(--fs-small);
  padding: var(--space-3);
  text-align: center;
}
 
.layout-body {
  display: flex;
  min-height: 120px;
}
 
.layout-sidebar {
  flex: 0 0 200px;
  background: var(--color-surface-alt);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--fs-small);
  color: var(--color-text-muted);
  border-right: 2px dashed var(--color-border-strong);
  padding: var(--space-2);
  text-align: center;
}
 
.layout-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--fs-small);
  color: var(--color-text-muted);
}
</style>