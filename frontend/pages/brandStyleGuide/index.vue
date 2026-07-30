<template>
    <v-app>
        <StyleNav :is-dark="isDark" @toggle-theme="toggleTheme" />

        <v-main>
            <v-container class="max-width-container">
                <StyleHero />
                <StyleColours />
                <StyleTypography />
                <StyleTokens />
                <StyleComponents />
                <StyleIcons />

            </v-container>

            <footer class="py-8 section-border text-center">
                <v-container>
                    <div class="footer-brand mb-1">Boardwise</div>
                    <div class="text-caption text-grey">Brand Design System v2.0</div>
                </v-container>
            </footer>

        </v-main>
    </v-app>
</template>

<script setup>
import { ref, onMounted } from 'vue'

import StyleTypography from '~/components/brandStyleGuide/StyleTypography.vue';
import StyleColours from '~/components/brandStyleGuide/StyleColours.vue';
import StyleHero from '~/components/brandStyleGuide/StyleHero.vue';
import StyleNav from '~/components/brandStyleGuide/StyleNav.vue';
import StyleTokens from '~/components/brandStyleGuide/StyleTokens.vue';
import StyleComponents from '~/components/brandStyleGuide/StyleComponents.vue';
import StyleIcons from '~/components/brandStyleGuide/StyleIcons.vue';

const isDark = ref(false)

const toggleTheme = () => {
  isDark.value = !isDark.value
  const theme = isDark.value ? 'dark' : 'light'
  document.documentElement.setAttribute('data-theme', theme)
}

onMounted(() => {
  isDark.value = document.documentElement.getAttribute('data-theme') === 'dark'
})
</script>

<style scoped>
.max-width-container {
    max-width: var(--container-max, 1200px) !important;
}

.section-border {
    border-top: 1px solid var(--color-border);
}

.footer-brand {
    font-family: var(--font-display);
    font-size: var(--fs-h3);
    color: var(--color-secondary);
}
</style>